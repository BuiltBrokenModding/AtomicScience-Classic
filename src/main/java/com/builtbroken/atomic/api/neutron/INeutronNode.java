package com.builtbroken.atomic.api.neutron;

import com.builtbroken.atomic.api.map.IDataMapNode;

/**
 *
 * Created by Pu-238 on 9/21/2018.
 */
public interface INeutronNode extends IDataMapNode
{
    /**
     * Gets the current value
     *
     * @return
     */
    int getNeutronValue();

    /**
     * Updates the current value
     *
     * @param value
     */
    void setNeutronValue(int value);
}
