package com.builtbroken.atomic.map.neutron.node;

import com.builtbroken.atomic.map.MapHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

/**
 * Neutron source taken from the map itself
 *
 *
 * Created by Pu-238 on 8/22/2020.
 */
public class NeutronSourceMap extends NeutronSource<BlockPos>
{
    private int value;
    private int dim;
    private BlockPos pos;

    public NeutronSourceMap(int dim, BlockPos host, int value) //TODO apply flywheel pattern
    {
        this.pos = host;
        this.dim = dim;
        this.value = value;
    }

    @Override
    public int getNeutronStrength()
    {
        return value;
    }

    @Override
    public double z()
    {
        return getHost().getZ() + 0.5;
    }

    @Override
    public double x()
    {
        return getHost().getX() + 0.5;
    }

    @Override
    public double y()
    {
        return getHost().getY() + 0.5;
    }

    @Override
    public int zi()
    {
        return getHost().getZ();
    }

    @Override
    public int xi()
    {
        return getHost().getX();
    }

    @Override
    public int yi()
    {
        return getHost().getY();
    }

    @Override
    public boolean isStillValid()
    {
        return world() != null && world().isBlockLoaded(getHost()) && value == getType().getValue(MapHandler.GLOBAL_DATA_MAP.getData(world(), getHost()));
    }

    @Override
    public World world()
    {
        return DimensionManager.getWorld(dim);
    }

    @Override
    public BlockPos getHost()
    {
        return pos;
    }
}
