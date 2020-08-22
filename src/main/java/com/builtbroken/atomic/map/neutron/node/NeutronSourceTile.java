package com.builtbroken.atomic.map.neutron.node;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.lang.ref.WeakReference;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

/**
 *
 * Created by Pu-238 on 8/22/2020.
 */
public class NeutronSourceTile<E extends TileEntity> extends NeutronSource<E>
{
    private final IntSupplier neutronFunction;
    private final BooleanSupplier activeFunction;

    private final WeakReference<E> hostReference;

    public NeutronSourceTile(E host, IntSupplier neutronFunction, BooleanSupplier activeFunction)
    {
        hostReference = new WeakReference(host);
        this.neutronFunction = neutronFunction;
        this.activeFunction = activeFunction;
    }

    @Override
    public boolean isNeutronEmitter()
    {
        return super.isNeutronEmitter() && activeFunction.getAsBoolean();
    }

    @Override
    public int getNeutronStrength()
    {
        return neutronFunction.getAsInt();
    }

    @Override
    public boolean doesSourceExist()
    {
        return world() != null
                && getHost() != null
                && !getHost().isInvalid()
                //Fix for chunk ghosting
                && world().getTileEntity(getPos()) == getHost();
    }

    @Override
    public World world()
    {
        return getHost().getWorld();
    }

    @Override
    public double z()
    {
        return getHost().getPos().getZ() + 0.5;
    }

    @Override
    public double x()
    {
        return getHost().getPos().getX() + 0.5;
    }

    @Override
    public double y()
    {
        return getHost().getPos().getY() + 0.5;
    }

    @Override
    public int zi()
    {
        return getHost().getPos().getZ();
    }

    @Override
    public int xi()
    {
        return getHost().getPos().getX();
    }

    @Override
    public int yi()
    {
        return getHost().getPos().getY();
    }

    @Override
    public boolean equals(Object object)
    {
        if(object == this)
        {
            return true;
        }
        if(object instanceof NeutronSourceTile)
        {
            if(getHost() != null && ((NeutronSourceTile) object).getHost() != null)
            {
                return getHost().getWorld() == ((TileEntity) ((NeutronSourceTile) object).getHost()).getWorld() && getHost().getPos() == ((TileEntity) ((NeutronSourceTile) object).getHost()).getPos();
            }
            return ((NeutronSourceTile) object).getHost() == getHost();
        }
        return false;
    }

    @Override
    protected String getDebugName()
    {
        return "NeutronSourceTile";
    }

    @Override
    public E getHost()
    {
        return hostReference.get();
    }
}
