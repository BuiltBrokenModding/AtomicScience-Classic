package com.builtbroken.atomic.content.items;

import com.builtbroken.atomic.api.item.IFuelRodItem;
import com.builtbroken.atomic.api.reactor.IReactor;
import com.builtbroken.atomic.lib.LanguageUtility;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.IntSupplier;

/**
 * Simple fuel rod
 *
 *
 * Created by Dark(DarkGuardsman, Robert) on 5/8/2018.
 */
public class ItemFuelRod extends ItemRadioactive implements IFuelRodItem
{
    public static final String NBT_FUEL_TIME = "fuelTimer";

    /** Time in ticks the fuel can run, when full */
    public final IntSupplier maxFuelRuntime;
    /** Radioactivity of the fuel rod when the reactor is active */
    public final IntSupplier reactorRadioactivity;
    /** Heat of the fuel rod when the reactor is active */
    public final IntSupplier reactorHeatOutput;
    /** Neutron emissions of the fuel rod when the reactor is active */
    public final IntSupplier reactorNeutronStrength;


    public ItemFuelRod(String key, String name, IntSupplier maxFuelRuntime, IntSupplier radioactiveMaterialValue, IntSupplier reactorRadioactivity, IntSupplier reactorHeatOutput, IntSupplier reactorNeutronStrength)
    {
        super(key, name, radioactiveMaterialValue);
        this.maxFuelRuntime = maxFuelRuntime;
        this.reactorRadioactivity = reactorRadioactivity;
        this.reactorHeatOutput = reactorHeatOutput;
        this.reactorNeutronStrength = reactorNeutronStrength;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> lines, ITooltipFlag flagIn)
    {
        int time = getFuelRodRuntime(stack, null);
        int maxTime = getMaxFuelRodRuntime(stack, null);

        String translation = LanguageUtility.getLocal(getTranslationKey() + ".info.fuel");
        translation = translation.replace("-time-", "" + time);
        translation = translation.replace("-maxTime-", "" + maxTime);
        lines.add(translation);
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack)
    {
        return Math.min(1, Math.max(0, 1 - (getFuelRodRuntime(stack, null) / (double) getMaxFuelRodRuntime(stack, null))));
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack)
    {
        return getFuelRodRuntime(stack, null) != getMaxFuelRodRuntime(stack, null);
    }

    @Override
    public int getFuelRodRuntime(ItemStack stack, IReactor reactor)
    {
        if (stack.getTagCompound() == null)
        {
            stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setInteger(NBT_FUEL_TIME, getMaxFuelRodRuntime(stack, reactor));
        }
        return stack.getTagCompound().getInteger(NBT_FUEL_TIME);
    }

    @Override
    public int getMaxFuelRodRuntime(ItemStack stack, IReactor reactor)
    {
        return maxFuelRuntime.getAsInt();
    }

    @Override
    public int getHeatOutput(ItemStack stack, IReactor reactor)
    {
        return reactorHeatOutput.getAsInt();
    }

    @Override
    public int getRadioactiveMaterial(ItemStack stack, IReactor reactor)
    {
        return reactorRadioactivity.getAsInt();
    }

    @Override
    public ItemStack onReactorTick(IReactor reactor, ItemStack stack, int tick, int fuelTick)
    {
        setFuelTime(stack, Math.max(0, fuelTick - 1));
        return stack;
    }

    public ItemStack setFuelTime(ItemStack stack, int time)
    {
        if (stack.getTagCompound() == null)
        {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setInteger(NBT_FUEL_TIME, time);
        return stack;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
    {
        if (this.isInCreativeTab(tab))
        {
            items.add(new ItemStack(this));
            items.add(setFuelTime(new ItemStack(this), 0));
        }
    }
	@Override
	public int getNeutronStrength(ItemStack stack, IReactor reactor) {
		return reactorNeutronStrength.getAsInt();
	}

}
