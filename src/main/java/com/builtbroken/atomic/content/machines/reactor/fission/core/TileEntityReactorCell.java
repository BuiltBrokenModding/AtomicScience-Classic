package com.builtbroken.atomic.content.machines.reactor.fission.core;

import com.builtbroken.atomic.AtomicScience;
import com.builtbroken.atomic.api.AtomicScienceAPI;
import com.builtbroken.atomic.api.item.IFuelRodItem;
import com.builtbroken.atomic.api.neutron.INeutronSource;
import com.builtbroken.atomic.api.radiation.IRadiationSource;
import com.builtbroken.atomic.api.reactor.IFissionReactor;
import com.builtbroken.atomic.api.thermal.IThermalSource;
import com.builtbroken.atomic.client.EffectRefs;
import com.builtbroken.atomic.content.ASBlocks;
import com.builtbroken.atomic.content.ASItems;
import com.builtbroken.atomic.content.machines.TileEntityInventoryMachine;
import com.builtbroken.atomic.content.machines.reactor.fission.controller.TileEntityReactorController;
import com.builtbroken.atomic.content.machines.pipe.reactor.pass.TileEntityRodPipe;
import com.builtbroken.atomic.lib.inventory.ItemStackHandlerWrapper;
import com.builtbroken.atomic.map.MapHandler;
import com.builtbroken.atomic.map.data.node.MapDataSources;
import com.builtbroken.atomic.map.exposure.node.RadSourceTile;
import com.builtbroken.atomic.map.neutron.node.NeutronSourceTile;
import com.builtbroken.atomic.map.thermal.node.ThermalSource;
import com.builtbroken.atomic.map.thermal.node.ThermalSourceTile;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 5/7/2018.
 */
public class TileEntityReactorCell extends TileEntityInventoryMachine<IItemHandlerModifiable> implements IFissionReactor
{
    public static final int SLOT_FUEL_ROD = 0;

    /** Client side */
    public boolean _renderFuel = false;
    public float _renderFuelLevel = 0f;

    /** Is the reactor currently running this tick */
    public boolean isRunning = false;

    /** is the reactor enabled */
    public boolean enabled = true; ///TODO add a spin up and down time, prevent instant enable/disable of reactors

    private int heatCache;
    private int radCache;
    private int neutronCache;

    private final NeutronSourceTile<TileEntityReactorCell> neutronSource = new NeutronSourceTile(this, () -> neutronCache, () -> isRunning);
    private final RadSourceTile<TileEntityReactorCell> radiationSource = new RadSourceTile(this, () -> radCache, () -> isRunning);
    private final ThermalSource<TileEntityReactorCell> thermalSource = new ThermalSourceTile(this, () -> heatCache, () -> isRunning);

    @Override
    protected void firstTick(boolean isClient)
    {
        super.firstTick(isClient);
        updateStructureType();
        if (!isClient)
        {
        	MapDataSources.addSource(getNeutronSource());
            MapDataSources.addSource(getRadiationSource());
            MapDataSources.addSource(getHeatSource());
        }
    }

    @Override
    protected IItemHandlerModifiable createInternalInventory()
    {
        return new ItemStackHandler(inventorySize())
        {
            @Override
            public void setStackInSlot(int slot, @Nonnull ItemStack stack)
            {
                validateSlotIndex(slot);

                ItemStack prev = getStackInSlot(slot);
                this.stacks.set(slot, stack);

                //Disable if not fuel rod
                if (!(stack.getItem() instanceof IFuelRodItem))
                {
                    isRunning = false;
                }

                //If not the same item, trigger update
                if (!ItemStack.areItemStacksEqual(prev, stack))
                {
                    onSlotStackChanged(slot, prev, stack);
                    onContentsChanged(slot);
                }
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack)
            {
                return stack.getItem() instanceof IFuelRodItem;
            }

            @Override
            public int getSlotLimit(int slot)
            {
                return 1;
            }
        };
    }

    @Nonnull
    @Override
    protected IItemHandlerModifiable createInventory()
    {
        return new ItemStackHandlerWrapper(getInventory())
        {
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate)
            {
                if (slot == SLOT_FUEL_ROD)
                {
                    ItemStack slot_stack = getStackInSlot(SLOT_FUEL_ROD);
                    if (!(slot_stack.getItem() instanceof IFuelRodItem) || ((IFuelRodItem) slot_stack.getItem()).getFuelRodRuntime(slot_stack, TileEntityReactorCell.this) <= 0)
                    {
                        return inventory.extractItem(slot, amount, simulate);
                    }
                }
                return inventory.extractItem(slot, amount, simulate);
            }
        };
    }

    @Override
    protected int inventorySize()
    {
        return 1;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        return capability == AtomicScienceAPI.THERMAL_CAPABILITY || capability == AtomicScienceAPI.RADIATION_CAPABILITY || capability == AtomicScienceAPI.NEUTRON_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capability == AtomicScienceAPI.THERMAL_CAPABILITY)
        {
            return (T) thermalSource;
        }
        else if (capability == AtomicScienceAPI.RADIATION_CAPABILITY)
        {
            return (T) radiationSource;
        }
        else if (capability == AtomicScienceAPI.NEUTRON_CAPABILITY)
        {
            return (T) neutronSource;
        }
        return super.getCapability(capability, facing);
    }

    //-----------------------------------------------
    //--------Runtime logic -------------------------
    //-----------------------------------------------

    @Override
    public void update(int ticks, boolean isClient)
    {
        super.update(ticks, isClient);
        if (!isClient)
        {
            doRodMovement(ticks);
            doRunChecks(ticks);

            //Cache values for faster runtime of threads
            heatCache = getHeatGenerated();
            radCache = getRadioactiveMaterial();
            neutronCache = getNeutronStrength();
        }
        else if (isRunning)
        {
            AtomicScience.sideProxy.spawnParticle(EffectRefs.REACTOR_RUNNING, xi() + 0.5, yi() + 0.5, zi() + 0.5, 0, 0, 0);
        }
    }

    protected void doRunChecks(int ticks)
    {
        //Track previous state
        final boolean prev_running = isRunning;

        //Check if we can operate
        if (canOperate())
        {
            //Set run status
            isRunning = true;

            //Consume fuel
            consumeFuel(ticks);

            //Every 1 second, do operation tick
            if (ticks % 20 == 0)
            {
                doOperationTick();
            }
        }
        //if not in operation status, set running to false
        else
        {
            isRunning = false;
        }

        //If state changed cycle grid
        if (prev_running != isRunning)
        {
            onRunStateChanged();
        }

        //If state changes or every so often sync data to client
        if (prev_running != isRunning || ticks % 20 == 0) //TODO see if %20 is needed
        {
            syncClientNextTick();
        }
    }

    protected void onRunStateChanged()
    {

    }

    /**
     * Do logic for rod movement between tiles
     * <p>
     * Does not always run every tick but calls logic to check if can/should run
     *
     * @param ticks - current tile ticks
     */
    protected void doRodMovement(int ticks)
    {
        //Every 5 seconds, Check if we need to move rods (works like a hopper)
        if (ticks % 100 == 0)
        {
            IFuelRodItem fuelRod = getFuelRod();
            if (fuelRod != null)
            {
                int runtime = fuelRod.getFuelRodRuntime(getFuelRodStack(), this);
                //Try to move rod to cell below reactor
                TileEntity tile = world.getTileEntity(getPos().down());
                if (tile instanceof TileEntityReactorCell)
                {
                    tryToMoveRod((TileEntityReactorCell) tile);
                }
                //Skip over reactor trying to insert into next reactor
                else if (isValidRodRelay(tile) && runtime > 0)
                {
                    //Try to find the next cell in the path
                    BlockPos pos = getPos().down(2);
                    while (isValidRodRelay(tile))
                    {
                        tile = world.getTileEntity(pos);
                        if (tile instanceof TileEntityReactorCell)
                        {
                            tryToMoveRod((TileEntityReactorCell) tile);
                            break;
                        }
                        else
                        {
                            pos = pos.down();
                        }
                    }
                }
                //Rod is dead try to eject to inventory
                else if (tile != null && runtime <= 0)
                {
                    if (tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP))
                    {
                        IItemHandler inventory = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
                        if (inventory != null)
                        {
                            for (int slot = 0; slot < inventory.getSlots() && !getFuelRodStack().isEmpty(); slot++)
                            {
                                if (inventory.isItemValid(slot, getFuelRodStack()))
                                {
                                    setFuelRod(inventory.insertItem(slot, getFuelRodStack(), false));
                                }
                            }
                        }
                    }

                    if (getFuelRod() == null && AtomicScience.runningAsDev)
                    {
                        AtomicScience.logger.info(this + " ejected spent rod");
                    }
                }
            }
        }
    }

    /**
     * Checks if a rod can be passed from this cell through
     * the tile into another cell.
     *
     * @param tile
     * @return
     */
    protected boolean isValidRodRelay(TileEntity tile)
    {
        return tile instanceof TileEntityReactorController || tile instanceof TileEntityRodPipe; //TODO use capability or interface for allow
    }

    /**
     * Do reactor cell movement
     *
     * @param cell - reactor cell to move item into
     */
    protected void tryToMoveRod(TileEntityReactorCell cell)
    {
        //always move lowest rod to bottom of stack (ensures dead rods exit core)
        if (cell.getFuelRod() != null)
        {
            int runTime = getFuelRuntime();
            int otherRunTime = cell.getFuelRuntime();

            if (runTime < otherRunTime)
            {
                ItemStack stack = cell.getFuelRodStack();
                cell.setFuelRod(getFuelRodStack());
                setFuelRod(stack);
                if (AtomicScience.runningAsDev)
                {
                    AtomicScience.logger.info(this + " switched rods with lower reactor");
                }
            }
        }
        //If not rod in lower core, move cell
        else
        {
            cell.setFuelRod(getFuelRodStack());
            setFuelRod(ItemStack.EMPTY);
            if (AtomicScience.runningAsDev)
            {
                AtomicScience.logger.info(this + " moved rod to lower reactor");
            }
        }
    }

    protected void doOperationTick()
    {
        //TODO calculate radioactive material leaking
        //TODO dump radioactive material to area or drains
    }

    protected int getActualHeat(int heat)
    {
        //TODO figure out bonus and negative to heat generation (control rods decrease, reactors nearby increase)
        return heat;
    }

    protected void consumeFuel(int ticks)
    {
        IFuelRodItem fuelRodItem = getFuelRod();
        if (fuelRodItem != null)
        {
            getInventory().setStackInSlot(0, fuelRodItem.onReactorTick(this, getFuelRodStack(), ticks, getFuelRuntime()));
        }
    }

    protected boolean canOperate()
    {
    	ItemStack stack = getInventory().getStackInSlot(0);
    	if(stack.getItem()==ASItems.itemBreederFuelCell)
    	{
    		int neutron = MapHandler.NEUTRON_MAP.getNeutronLevel(world, pos);
    		if(neutron >=1000)
    		{
    			return enabled && hasFuel() && getFuelRuntime() >= 0;
    		}
    		return false;
    	}
        //TODO check for safety (water, temp, etc)
        //TODO check if can generate neutrons (controls rods can force off)
        return enabled && hasFuel() && getFuelRuntime() > 0;
    }

    protected void onSlotStackChanged(int slot, ItemStack prev_stack, ItemStack new_stack)
    {
        this.markDirty();
        if (!canOperate())
        {
            isRunning = false;
            _renderFuel = false;
            _renderFuelLevel = 0f;
        }
    }

    @Override
    public void invalidate()
    {
        super.invalidate();
        MapDataSources.removeSource(getRadiationSource());
        MapDataSources.removeSource(getHeatSource());
    }

    @Override
    public void onChunkUnload()
    {
        resetSources();
    }

    public void resetSources()
    {
        getRadiationSource().disconnectMapData();
        getRadiationSource().clearMapData();

        getHeatSource().disconnectMapData();
        getHeatSource().clearMapData();
    }

    //-----------------------------------------------
    //--------Accessors -----------------------------
    //-----------------------------------------------

    public boolean hasFuel()
    {
        return getFuelRod() != null;
    }

    public int getFuelRuntime()
    {
        IFuelRodItem fuelRod = getFuelRod();
        if (fuelRod != null)
        {
            return fuelRod.getFuelRodRuntime(getFuelRodStack(), this);
        }
        return 0;
    }

    public int getMaxFuelRuntime()
    {
        IFuelRodItem fuelRod = getFuelRod();
        if (fuelRod != null)
        {
            return fuelRod.getMaxFuelRodRuntime(getFuelRodStack(), this);
        }
        return 0;
    }

    public IFuelRodItem getFuelRod()
    {
        ItemStack stack = getFuelRodStack();
        return !stack.isEmpty() && stack.getItem() instanceof IFuelRodItem ? (IFuelRodItem) stack.getItem() : null;
    }

    public void setFuelRod(ItemStack stack)
    {
        getInventory().setStackInSlot(SLOT_FUEL_ROD, stack);
    }

    @Override
    public ItemStack getFuelRodStack()
    {
        return getInventory().getStackInSlot(SLOT_FUEL_ROD);
    }

    public int getRadioactiveMaterial()
    {
        IFuelRodItem fuelRod = getFuelRod();
        if (fuelRod != null)
        {
            return fuelRod.getRadioactiveMaterial(getFuelRodStack(), this);
        }
        return 0;
    }
    
    public int getNeutronStrength()
    {
        IFuelRodItem fuelRod = getFuelRod();
        if (fuelRod != null)
        {
        	return fuelRod.getNeutronStrength(getFuelRodStack(), this);
        }
        return 0;
    }

    public int getHeatGenerated()
    {
        IFuelRodItem fuelRodItem = getFuelRod();
        if (fuelRodItem != null)
        {
            return getActualHeat(fuelRodItem.getHeatOutput(getFuelRodStack(), this));
        }
        return 0;
    }

    //@Override TODO
    public boolean isItemValidForSlot(int slot, ItemStack stack)
    {
        return slot == SLOT_FUEL_ROD && stack.getItem() instanceof IFuelRodItem;
    }

    //@Override
    public boolean canExtractItem(int slot, ItemStack stack, int side)
    {
        if (SLOT_FUEL_ROD == slot)
        {
            if (stack.getItem() instanceof IFuelRodItem)
            {
                return ((IFuelRodItem) stack.getItem()).getFuelRodRuntime(stack, this) <= 0;
            }
            return true;
        }
        return false;
    }

    //-----------------------------------------------
    //--------Rendering props -----------------------
    //-----------------------------------------------

    public float getFuelRenderLevel()
    {
        if (isServer())
        {
            return (float) getFuelRuntime() / (float) getMaxFuelRuntime();
        }
        return _renderFuelLevel;
    }

    //-----------------------------------------------
    //--------Network code -------------------------
    //-----------------------------------------------

    @Override
    protected void writeDescPacket(List<Object> dataList, EntityPlayer player)
    {
        super.writeDescPacket(dataList, player);
        dataList.add(isRunning || canOperate());
        dataList.add(hasFuel());
        dataList.add(getFuelRenderLevel());
    }

    @Override
    protected void readDescPacket(ByteBuf buf, EntityPlayer player)
    {
        super.readDescPacket(buf, player);
        isRunning = buf.readBoolean();
        _renderFuel = buf.readBoolean();
        _renderFuelLevel = buf.readFloat();
    }

    //-----------------------------------------------
    //--------Structure code -------------------------
    //-----------------------------------------------

    public void updateStructureType()
    {
        IBlockState blockAbove = world.getBlockState(getPos().up());
        IBlockState blockBelow = world.getBlockState(getPos().down());

        if (canConnect(blockAbove) && canConnect(blockBelow))
        {
            setStructureType(ReactorStructureType.CORE_MIDDLE);
        }
        else if (canConnect(blockBelow))
        {
            setStructureType(ReactorStructureType.CORE_TOP);
        }
        else if (canConnect(blockAbove))
        {
            setStructureType(ReactorStructureType.CORE_BOTTOM);
        }
        else
        {
            setStructureType(ReactorStructureType.CORE);
        }
    }

    public void setStructureType(ReactorStructureType structureType)
    {
        IBlockState blockState = world.getBlockState(getPos());
        if (blockState.getProperties().containsKey(BlockReactorCell.REACTOR_STRUCTURE_TYPE))
        {
            ReactorStructureType type = blockState.getValue(BlockReactorCell.REACTOR_STRUCTURE_TYPE);
            if (type != structureType)
            {
                world.setBlockState(pos, blockState.withProperty(BlockReactorCell.REACTOR_STRUCTURE_TYPE, structureType));
            }
        }
    }

    public ReactorStructureType getStructureType()
    {
        IBlockState blockState = world.getBlockState(getPos());
        if (blockState.getProperties().containsKey(BlockReactorCell.REACTOR_STRUCTURE_TYPE))
        {
            return world.getBlockState(getPos()).getValue(BlockReactorCell.REACTOR_STRUCTURE_TYPE);
        }
        return ReactorStructureType.CORE;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
    {
        return oldState.getBlock() != newSate.getBlock();
    }

    private boolean canConnect(IBlockState block)
    {
        return block.getBlock() == ASBlocks.blockReactorCell
                || block.getBlock() == ASBlocks.blockReactorController
                || block.getBlock() == ASBlocks.blockRodPipe
                || block.getBlock() == ASBlocks.blockRodPipeInv;
    }

    @Override
    public IRadiationSource getRadiationSource()
    {
        return radiationSource;
    }
    
    @Override
    public INeutronSource getNeutronSource()
    {
        return neutronSource;
    }

    @Override
    public IThermalSource getHeatSource()
    {
        return thermalSource;
    }

    @Override
    public String toString()
    {
        return "ReactorCell[W: " + worldName() + " | D: " + dim() + " | Pos(" + xi() + ", " + yi() + ", " + zi() + ")]@" + hashCode();
    }

    @Override
    public boolean hasFastRenderer()
    {
        return true;
    }
}
