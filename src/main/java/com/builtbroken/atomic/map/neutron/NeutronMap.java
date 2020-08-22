package com.builtbroken.atomic.map.neutron;

import com.builtbroken.atomic.api.map.DataMapType;
import com.builtbroken.atomic.api.neutron.INeutronExposureSystem;
import com.builtbroken.atomic.map.MapHandler;
import com.builtbroken.atomic.map.data.storage.DataMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Handles radiation exposure map. Is not saved and only cached to improve runtime.
 *
 *
 * Created by Pu-238 on 8/22/2020.
 */
public class NeutronMap implements INeutronExposureSystem
{
    ///----------------------------------------------------------------
    ///-------- Level Data Accessors
    ///----------------------------------------------------------------

    /**
     * Gets the '(REM) roentgen equivalent man' at the given location
     *
     * @param world - location
     * @param pos   - location
     * @return rem level in mili-rads (1/1000ths of a rem)
     */
    public int getNeutronLevel(World world, BlockPos pos)
    {
        DataMap map = MapHandler.GLOBAL_DATA_MAP.getMap(world, false);
        if (map != null)
        {
            return map.getValue(pos, DataMapType.NEUTRON);
        }
        return 0;
    }

    /**
     * Gets the NEU neutron radiation level at the given location
     *
     * @param world - location
     * @param x     - location
     * @param y     - location
     * @param z     - location
     * @return neu level in mili-neus (1/1000ths of a neu)
     */
    public int getNeutronLevel(World world, int x, int y, int z)
    {
        DataMap map = MapHandler.GLOBAL_DATA_MAP.getMap(world, false);
        if (map != null)
        {
            return map.getValue(x, y, z, DataMapType.NEUTRON);
        }
        return 0;
    }

    /**
     * Gets the neutron exposure for the entity
     *
     * @param entity - entity, will use the entity size to get an average value
     * @return NEU value
     */
    public float getNeutronExposure(Entity entity)
    {
        float value = 0;

        //Top point
        value += getNeutronLevel(entity.world, (int) Math.floor(entity.posX), (int) Math.floor(entity.posY + entity.height), (int) Math.floor(entity.posZ));

        //Mid point
        value += getNeutronLevel(entity.world, (int) Math.floor(entity.posX), (int) Math.floor(entity.posY + (entity.height / 2)), (int) Math.floor(entity.posZ));

        //Bottom point
        value += getNeutronLevel(entity.world, (int) Math.floor(entity.posX), (int) Math.floor(entity.posY), (int) Math.floor(entity.posZ));

        //Average TODO build alg to use body size (collision box)
        value /= 3f;

        //Convert from mili neu to neu
        value /= 1000f;

        return value;
    }

    ///----------------------------------------------------------------
    ///--------Edit events
    ///----------------------------------------------------------------

    public void onWorldUnload(World world)
    {

    }

    /*
    @SubscribeEvent()
    public void onChunkAdded(MapSystemEvent.AddChunk event)
    {
        if (event.world() != null && !event.world().isRemote && event.map.mapSystem == MapHandler.MATERIAL_MAP)
        {
            MapHandler.THREAD_RAD_EXPOSURE.queueChunkForAddition(event.chunk);
        }
    }

    @SubscribeEvent()
    public void onChunkRemove(MapSystemEvent.RemoveChunk event)
    {
        if (event.world() != null && !event.world().isRemote && event.map.mapSystem == MapHandler.MATERIAL_MAP)
        {
            MapHandler.THREAD_RAD_EXPOSURE.queueChunkForRemoval(event.chunk);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRadiationChange(MapSystemEvent.UpdateValue event)
    {
        if (event.world() != null && !event.world().isRemote && event.map.mapSystem == MapHandler.MATERIAL_MAP)
        {
            MapHandler.THREAD_RAD_EXPOSURE.queuePosition(DataChange.get(event.dim(), event.x, event.y, event.z, event.prev_value, event.node));
        }
    }
    */

    /*
    @SubscribeEvent()
    public void serverTick(TickEvent.ServerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            //Cleanup
            clearDeadSources();

            //Loop sources looking for changes
            for (RadSourceWrapper wrapper : radiationSourceMap.values())
            {
                if (wrapper.hasSourceChanged())
                {
                    updateSourceData(wrapper.source, wrapper.source.getRadioactiveMaterial());
                }
            }
        }
    }

    @SubscribeEvent()
    public void itemPickUpEvent(PlayerEvent.ItemPickupEvent event)
    {
        if (!event.player.world.isRemote)
        {
            EntityItem entityItem = event.pickedUp;
            if (entityItem != null)
            {
                ItemStack stack = entityItem.getItem();
                if (stack != null && stack.getItem() instanceof IRadioactiveItem)
                {
                    removeSource(entityItem);
                }
            }
        }
    }

    @SubscribeEvent()
    public void entityJoinWorld(EntityJoinWorldEvent event)
    {
        if (!event.getWorld().isRemote && event.getEntity().isEntityAlive())
        {
            //Add source handles checking if its an actual source
            addSource(event.getEntity());
        }
    }
    */
}
