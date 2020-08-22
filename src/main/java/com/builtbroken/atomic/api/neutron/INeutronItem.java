package com.builtbroken.atomic.api.neutron;

import net.minecraft.item.ItemStack;

/**
 * Applied to an item to track how much neutron radiation it will produced based on its neutron strength value.
 * <p>
 * Will be wrapped by {@link INeutronSource} for logic in the actual world.
 *
 *
 * Created by Pu-238 on 8/22/2020.
 */
@Deprecated //Being replaced with capability system
public interface INeutronItem
{
    /**
     * Gets the amount of radioactive material
     * this item represents.
     * <p>
     * Used to calculate amount of radiation to emit in terms
     * of NEUs.
     *
     * @return material value
     */
    int getNeutronStrength(ItemStack stack);

    /**
     * Is the item still a neutron emitter at the time
     * this was called. Used as an isAlive() and isValid()
     * check to see if the source needs to be removed.
     *
     * @return true if is still emitting neutrons.
     */
    default boolean isNeutronEmitter(ItemStack stack)
    {
        return true;
    }

}
