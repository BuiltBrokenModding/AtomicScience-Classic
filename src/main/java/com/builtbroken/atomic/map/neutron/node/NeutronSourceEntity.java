package com.builtbroken.atomic.map.neutron.node;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import java.lang.ref.WeakReference;

/**
 *
 * Created by Pu-238 on 8/22/2020.
 */
public abstract class NeutronSourceEntity<E extends Entity> extends NeutronSource<E>
{
    private final WeakReference<E> hostReference;

    public NeutronSourceEntity(E host)
    {
        hostReference = new WeakReference(host);
    }
    @Override
    public boolean isStillValid()
    {
        return super.isStillValid() && !getHost().isDead;
    }

    @Override
    public boolean doesSourceExist()
    {
        return super.doesSourceExist() && !getHost().isDead;
    }

    @Override
    public World world()
    {
        return getHost().world;
    }

    @Override
    public double z()
    {
        return getHost().posZ;
    }

    @Override
    public double x()
    {
        return getHost().posX;
    }

    @Override
    public double y()
    {
        return getHost().posY;
    }

    @Override
    protected String getDebugName()
    {
        return "NeutronSourceEntity";
    }

    @Override
    public E getHost()
    {
        return hostReference.get();
    }
}
