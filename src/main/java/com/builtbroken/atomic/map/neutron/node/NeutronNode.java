package com.builtbroken.atomic.map.neutron.node;

import com.builtbroken.atomic.api.map.DataMapType;
import com.builtbroken.atomic.api.map.IDataMapSource;
import com.builtbroken.atomic.api.neutron.INeutronNode;
import com.builtbroken.atomic.api.neutron.INeutronSource;
import com.builtbroken.atomic.map.data.DataPool;
import com.builtbroken.atomic.map.data.IDataPoolObject;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

/**
 * Created by Pu-238 on 8/22/2020.
 */
public class NeutronNode implements INeutronNode, IDataPoolObject
{
    private static final DataPool<NeutronNode> NEUTRON_NODE_POOL = new DataPool(400000); //TODO add config

    private WeakReference<INeutronSource> source;

    private int value;

    private NeutronNode(INeutronSource source, int value)
    {
        this.source = new WeakReference(source);
        this.value = value;
    }

    @Override
    public int getNeutronValue()
    {
        return value;
    }

    @Override
    public void setNeutronValue(int value)
    {
        this.value = value;
    }

    @Override
    public DataMapType getType()
    {
        return DataMapType.NEUTRON;
    }

    @Nullable
    @Override
    public IDataMapSource getSource()
    {
        if (source == null || source.get() == null)
        {
            return null;
        }
        return source.get();
    }

    public static NeutronNode get(INeutronSource source, int value)
    {
        if (NEUTRON_NODE_POOL.has())
        {
            NeutronNode dataChange = NEUTRON_NODE_POOL.get();
            if (dataChange != null)
            {
                dataChange.source = new WeakReference(source);
                dataChange.value = value;
                return dataChange;
            }
        }
        return new NeutronNode(source, value);
    }

    @Override
    public void dispose()
    {
        source = null;
        NEUTRON_NODE_POOL.dispose(this);
    }
}
