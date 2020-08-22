package com.builtbroken.atomic.api.reactor;

import com.builtbroken.atomic.api.neutron.INeutronSource;
import com.builtbroken.atomic.api.radiation.IRadiationSource;

/**
 * Applied to fission reactors
 *
 *
 * Created by Dark(DarkGuardsman, Robert) on 5/8/2018.
 */
public interface IFissionReactor extends IReactor
{
    /**
     * Called to get the object that handles
     * radiation for the reactor. This should be a
     * capability on the tile itself.
     *
     * @return
     */
    IRadiationSource getRadiationSource();
    
    /**
     * Called to get the object that handles
     * neutron radiation for the reactor. This should be a
     * capability on the tile itself.
     *
     * @return
     */
    INeutronSource getNeutronSource();

}
