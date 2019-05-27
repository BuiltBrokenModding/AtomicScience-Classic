package com.builtbroken.atomic.content.machines.accelerator.graph;

import com.builtbroken.atomic.api.accelerator.IAcceleratorNode;
import com.builtbroken.atomic.api.accelerator.IAcceleratorParticle;
import com.builtbroken.atomic.lib.math.MathConstF;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

/**
 * Created by Dark(DarkGuardsman, Robert) on 4/7/2019.
 */
public class AcceleratorParticle implements IAcceleratorParticle
{
    public static final String NBT_ID = "id";
    public static final String NBT_DIM = "dim";
    public static final String NBT_VELOCITY = "velocity";
    public static final String NBT_ENERGY = "energy";
    public static final String NBT_X = "x";
    public static final String NBT_Y = "y";
    public static final String NBT_Z = "z";
    public static final String NBT_DIR = "direction";
    public static final String NBT_STACK = "item";
    public static final String NBT_IN_TUBE = "in_tube";
    public static final String NBT_ALIVE = "alive";

    public final UUID unique_id;

    //How far in meters/blocks can be move per tick of the game
    private float velocity;

    //Energy stored
    private float energy;

    //Position data
    private int dim;
    private float x;
    private float y;
    private float z;

    //Movement direction
    private EnumFacing moveDirection;

    //Current pathing node, can be null if we are not in a accelerator
    private IAcceleratorNode node;

    //Stack that was used to make the particle, used for mass and recipes
    private ItemStack itemStack = ItemStack.EMPTY;

    //True if we are not in a tube
    private boolean notInTube = false;

    private boolean isAlive = true;

    public AcceleratorParticle(NBTTagCompound nbt)
    {
        unique_id = nbt.hasKey(NBT_ID) ? NBTUtil.getUUIDFromTag(nbt.getCompoundTag(NBT_ID)) : UUID.randomUUID();
        load(nbt);
    }

    public AcceleratorParticle(int dim, BlockPos start, EnumFacing moveDirection, float energy)
    {
        this.unique_id = UUID.randomUUID();
        this.dim = dim;
        this.x = start.getX() + 0.5f;
        this.y = start.getY() + 0.5f;
        this.z = start.getZ() + 0.5f;
        this.moveDirection = moveDirection;
        this.energy = energy;
        this.velocity = .1f; //TODO calculate speed from energy and mass of the itemstack
    }

    public void update(int worldTick)
    {
        move();
        consumeEnergy();
    }

    protected void move()
    {
        //TODO check if we need to turn
        //TODO update position
        //TODO center on tube

        //How much we can move in a single go
        float distanceToMove = velocity;

        //Get current node we are pathing
        IAcceleratorNode currentNode = getCurrentNode();
        if (currentNode != null)
        {
            //WE can move through several nodes, so loop until done TODO replace with path so we can % locate position and node
            while (distanceToMove > MathConstF.ZERO_CUT && currentNode != null)
            {
                final EnumFacing prevDirection = moveDirection;

                //Move forward consuming distance
                float distanceMoved = currentNode.move(this, distanceToMove);
                distanceToMove -= distanceMoved;

                //Exit condition if we didn't move, prevents infinite loops
                if (Math.abs(distanceMoved) <= 0.0001)
                {
                    break;
                }

                //Check if our direction changed
                if (prevDirection != moveDirection)
                {
                    //System.out.println(this + ": Has turned from " + prevDirection + " to " + moveDirection);

                    //Consume extra energy for turn TODO figure out how much rather than just x2
                    consumeEnergy();

                    //TODO track when we make a turn
                    //TODO send turn point to client for smooth animation
                    //      Client will need to lerp between points
                    //      A -> B -> C -> D
                    //      Each time it gets to a point it will clear the last
                    //      This mean it only has a current pos and target pos
                    //      If it has no target then its goal is the server's position
                    //TODO long term send path to client so we don't need to send every turn change
                    //TODO render points ahead and behind of the particle for debug
                }

                //Reset for next loop
                currentNode = getCurrentNode();
            }
        }
        else
        {
            //TODO destroy or fire off into world
            notInTube = true;
        }

        //Fix precision problems
        normalizePosition();
    }

    public void move(float x, float y, float z)
    {
        this.x += x;
        this.y += y;
        this.z += z;
    }

    public void normalizePosition()
    {
        x = Math.round(x * 1000) / 1000f;
        y = Math.round(y * 1000) / 1000f;
        z = Math.round(z * 1000) / 1000f;
    }

    public void move(float moveAmount, EnumFacing direction)
    {
        move(moveAmount * direction.getXOffset(),
                moveAmount * direction.getYOffset(),
                moveAmount * direction.getZOffset());
    }

    protected void consumeEnergy()
    {
        //TODO slowly eat energy
        energy -= 1;
    }

    public void addEnergy(float magnetPower)
    {
        energy += magnetPower;
    }

    public void setMoveDirection(EnumFacing direction)
    {
        this.moveDirection = direction;
    }

    public void setVelocity(float v)
    {
        this.velocity = v;
    }

    public void addVelocity(float acceleration)
    {
        setVelocity(velocity + acceleration);
    }

    public float getVelocity()
    {
        return velocity;
    }

    public int dim()
    {
        return dim;
    }

    @Override
    public double z()
    {
        return z;
    }

    @Override
    public double x()
    {
        return x;
    }

    @Override
    public double y()
    {
        return y;
    }

    @Override
    public float zf()
    {
        return z;
    }

    @Override
    public float xf()
    {
        return x;
    }

    @Override
    public float yf()
    {
        return y;
    }

    public void setPos(float x, float y, float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public IAcceleratorNode getCurrentNode()
    {
        return node;
    }

    public AcceleratorParticle setCurrentNode(AcceleratorNode node)
    {
        this.node = node;
        return this;
    }

    public ItemStack getItem()
    {
        return itemStack;
    }

    public AcceleratorParticle setItem(ItemStack stack)
    {
        this.itemStack = stack;
        if (stack == null)
        {
            this.itemStack = ItemStack.EMPTY;
        }
        return this;
    }

    public EnumFacing getMoveDirection()
    {
        if(moveDirection == null)
        {
            moveDirection = EnumFacing.NORTH;
        }
        return moveDirection;
    }

    public boolean isInvalid()
    {
        return notInTube || !isAlive;
    }

    public void setDead()
    {
        this.isAlive = false;
    }

    //TODO entity version

    @Override
    public String toString()
    {
        return String.format("AcceleratorParticle[Pos: %.2f, %.2f, %.2f", x, y, z) + " Dir:" + moveDirection + "]@" + hashCode();
    }

    @Override
    public NBTTagCompound save(NBTTagCompound nbt)
    {
        //Immutable data
        nbt.setTag(NBT_ID, NBTUtil.createUUIDTag(unique_id));

        //Mutable data
        nbt.setFloat(NBT_VELOCITY, velocity);
        nbt.setFloat(NBT_ENERGY, energy);
        nbt.setInteger(NBT_DIM, dim);
        nbt.setFloat(NBT_X, x);
        nbt.setFloat(NBT_Y, y);
        nbt.setFloat(NBT_Z, z);
        nbt.setByte(NBT_DIR, (byte)getMoveDirection().ordinal());
        nbt.setTag(NBT_STACK, itemStack.serializeNBT());
        nbt.setBoolean(NBT_IN_TUBE, notInTube);
        nbt.setBoolean(NBT_ALIVE, isAlive);
        return nbt;
    }

    @Override
    public void load(NBTTagCompound nbt)
    {
        //Constructor reads dim and UUID
        velocity = nbt.getFloat(NBT_VELOCITY);
        energy = nbt.getFloat(NBT_ENERGY);
        dim = nbt.getInteger(NBT_DIM);
        x = nbt.getFloat(NBT_X);
        y = nbt.getFloat(NBT_Y);
        z = nbt.getFloat(NBT_Z);
        moveDirection = EnumFacing.byIndex(nbt.getByte(NBT_DIR));
        itemStack = new ItemStack(nbt.getCompoundTag(NBT_STACK));
        notInTube = nbt.getBoolean(NBT_IN_TUBE);
        isAlive = nbt.getBoolean(NBT_ALIVE);
    }
}
