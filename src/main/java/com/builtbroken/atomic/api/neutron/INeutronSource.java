package com.builtbroken.atomic.api.neutron;

import com.builtbroken.atomic.api.map.IDataMapSource;
import com.builtbroken.atomic.lib.transform.IPosWorld;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;

/**
 * Applied to objects that emit neutrons into the environment. Will
 * be tracked by the exposure thread and updated each tick.
 * <p>
 * Source has to be registered to the neutron exposure system in order to be tracked.
 * <p>
 * If the source stops emitting neutrons it will unregister. As this is considered
 * to mean the object is gone or no longer present in the map.
 * <p>
 * If the source
 * starts emitting neutrons again, it will need to be registered again to the map to work.
 *
 *
 * Created by Pu-238 on 8/22/2020.
 */
public interface INeutronSource extends IPosWorld, IDataMapSource
{
    /**
     * Current map of positions to nodes
     *
     * @return
     */
    HashMap<BlockPos, INeutronNode> getCurrentNodes();

    /**
     * Sets current map of positions to nodes
     * <p>
     * Should only be called from pathing thread
     */
    void setCurrentNodes(HashMap<BlockPos, INeutronNode> map);

    /**
     * Gets the amount of neutron radiation
     * this source represents.
     * <p>
     * Used to calculate amount of radiation to emit in terms
     * of NEUs.
     * <p>
     * This value should not change each time the method is called. If
     * the value is randomized it should be cached until next tick.
     *
     * @return material value
     */
    int getNeutronStrength();

    /**
     * Is the source emitting neutrons at the time
     * this was called.
     * <p>
     * {@link #isStillValid()} is to be used as a validation
     * check instead of this method.
     *
     * @return true if is still radioactive
     */
    default boolean isNeutronEmitter()
    {
        return true;
    }
}
