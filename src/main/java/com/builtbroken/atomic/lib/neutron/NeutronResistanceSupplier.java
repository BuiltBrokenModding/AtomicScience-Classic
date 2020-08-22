package com.builtbroken.atomic.lib.neutron;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@FunctionalInterface
public interface NeutronResistanceSupplier
{
    /**
     * Gets a result.
     *
     * @return a result
     */
    float getAsFloat(World world, BlockPos pos, IBlockState state);
}