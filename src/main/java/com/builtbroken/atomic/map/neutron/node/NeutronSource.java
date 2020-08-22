package com.builtbroken.atomic.map.neutron.node;

import com.builtbroken.atomic.api.map.DataMapType;
import com.builtbroken.atomic.api.neutron.INeutronNode;
import com.builtbroken.atomic.api.neutron.INeutronSource;
import com.builtbroken.atomic.map.data.node.MapNodeSource;
import net.minecraft.nbt.NBTTagCompound;

/**
 *
 * Created by Pu-238 on 8/22/2020.
 */
public abstract class NeutronSource<E> extends MapNodeSource<E, INeutronNode> implements INeutronSource
{
    public static final String NBT_NEUTRON = "neutron";

    @Override
    public boolean isNeutronEmitter()
    {
        return getNeutronStrength() > 0;
    }

    @Override
    public boolean isStillValid()
    {
        return super.isStillValid() && isNeutronEmitter();
    }

    @Override
    public DataMapType getType()
    {
        return DataMapType.NEUTRON;
    }

    @Override
    public NBTTagCompound getSaveState()
    {
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setInteger(NBT_NEUTRON, getNeutronStrength());
        return tagCompound;
    }

    @Override
    public boolean shouldQueueForUpdate(NBTTagCompound saveState)
    {
        final int material = getNeutronStrength();
        if (material > 0 && !hasNodes())
        {
            return true;
        }
        return saveState == null || saveState.getInteger(NBT_NEUTRON) != material;
    }

    @Override
    protected String addDebugInfo()
    {
        return "NEUT: " + isNeutronEmitter() + "-" + getNeutronStrength() + ", ";
    }

    @Override
    protected String getDebugName()
    {
        return "NeutronSource";
    }
}
