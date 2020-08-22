package com.builtbroken.atomic.api.item;

import com.builtbroken.atomic.api.neutron.INeutronItem;
import com.builtbroken.atomic.api.radiation.IRadioactiveItem;
import com.builtbroken.atomic.api.reactor.IReactor;
import net.minecraft.item.ItemStack;

/**
 * Applied to an item that acts as a fuel rod for reactors
 *
 *
 * Created by Dark(DarkGuardsman, Robert) on 5/8/2018.
 */
public interface IFuelRodItem extends IRadioactiveItem
{
    /**
     * Amount of time in ticks the fuel will run
     *
     * @param reactor - reactor consuming the fuel, may be null
     * @param stack   - fuel rod
     * @return ticks (20 ticks a second)
     */
    int getFuelRodRuntime(ItemStack stack, IReactor reactor);

    /**
     * Amount of time in ticks the fuel will run
     * when fully charged.
     *
     * @param reactor - reactor consuming the fuel, may be null
     * @param stack   - fuel rod
     * @return ticks (20 ticks a second)
     */
    int getMaxFuelRodRuntime(ItemStack stack, IReactor reactor);

    /**
     * Amount of heat this rod would produce
     * in a reactor with no limits.
     *
     * @param reactor - reactor consuming the fuel, may be null
     * @param stack   - fuel rod
     * @return heat value
     */
    int getHeatOutput(ItemStack stack, IReactor reactor);

    /**
     * Amount of radioactive material the rod represents when active
     *
     * @param reactor - reactor consuming the fuel, may be null
     * @param stack   - fuel rod
     * @return radioactive value
     */
    int getRadioactiveMaterial(ItemStack stack, IReactor reactor);

    /**
     * Called each tick the fuel runs
     * <p>
     * Use this to decrease the fuel runtime
     *
     * @param reactor  - reactor consuming the fuel
     * @param stack    - fuel rod, use a copy if you don't want changes applied
     * @param tick     - current tick of the reactor (counted from area loaded)
     * @param fuelTick - current tick of the fuel rod
     * @return modified stack
     */
    ItemStack onReactorTick(IReactor reactor, ItemStack stack, int tick, int fuelTick);
    
    /**
     * Strength of neutron radiation the rod represents when active
     *
     * @param reactor - reactor consuming the fuel, may be null
     * @param stack   - fuel rod
     * @return neutron value
     */
    int getNeutronStrength(ItemStack stack, IReactor reactor);
}
