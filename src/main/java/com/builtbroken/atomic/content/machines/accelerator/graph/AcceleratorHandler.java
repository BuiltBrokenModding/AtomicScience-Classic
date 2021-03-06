package com.builtbroken.atomic.content.machines.accelerator.graph;

import com.builtbroken.atomic.AtomicScience;
import com.builtbroken.atomic.content.machines.accelerator.particle.AcceleratorParticle;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;

/**
 * Created by Dark(DarkGuardsman, Robert) on 4/7/2019.
 */
@Mod.EventBusSubscriber(modid = AtomicScience.DOMAIN)
public class AcceleratorHandler
{

    private static final HashMap<Integer, AcceleratorWorld> dimToAcceleratorWorld = new HashMap();

    public static AcceleratorWorld get(World world)
    {
        return get(world.provider.getDimension());
    }

    public static AcceleratorWorld get(int dim)
    {
        return dimToAcceleratorWorld.get(dim);
    }

    public static AcceleratorWorld getOrCreate(World world)
    {
        return getOrCreate(world.provider.getDimension());
    }

    public static AcceleratorWorld getOrCreate(int dim)
    {
        AcceleratorWorld acceleratorWorld = get(dim);
        if (acceleratorWorld == null)
        {
            acceleratorWorld = new AcceleratorWorld(dim);
            dimToAcceleratorWorld.put(dim, acceleratorWorld);
        }
        return acceleratorWorld;
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            final World world = event.world;
            final AcceleratorWorld acceleratorWorld = get(world);
            if (acceleratorWorld != null)
            {
                if (event.side.isServer())
                {
                    acceleratorWorld.update(world);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.ClientTickEvent event)
    {

    }

    @SubscribeEvent
    public static void onUnload(WorldEvent.Unload event)
    {
        final World world = event.getWorld();
        final AcceleratorWorld acceleratorWorld = get(world);

        if (acceleratorWorld != null)
        {
            acceleratorWorld.unload(world);
            dimToAcceleratorWorld.remove(world.provider.getDimension());
        }
    }

    /**
     * Called to create a new particle inside of a node
     *
     * @param world         - world containing the node
     * @param node          - node to use
     * @param item          - item to use for creation
     * @param energyToStart - starting energy
     */
    public static void newParticle(World world, AcceleratorNode node, ItemStack item, int energyToStart)
    {
        final AcceleratorWorld acceleratorWorld = getOrCreate(world);

        //Create
        AcceleratorParticle particle = new AcceleratorParticle(world.provider.getDimension(), node.getPos(), node.getDirection(), energyToStart)
                .setCurrentNode(node)
                .setItem(item.copy());

        //Add
        node.addParticle(particle);

        //System.out.println("Created particle " + particle);

        //TODO fire events
        //TODO sanity check

    }

    public static void spawnParticleInWorld(AcceleratorParticle particle)
    {
        //TODO spawn entity in world
        //TODO make entity bounce off walls
        //TODO make it consume energy over time
        //TODO explode if hits some materials
        //TODO cause a lot of entity damage
    }
}
