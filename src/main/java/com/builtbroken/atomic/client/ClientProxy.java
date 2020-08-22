package com.builtbroken.atomic.client;

import com.builtbroken.atomic.AtomicScience;
import com.builtbroken.atomic.CommonProxy;
import com.builtbroken.atomic.client.fx.FxSmoke;
import com.builtbroken.atomic.config.client.ConfigClient;
import com.builtbroken.atomic.content.ASItems;
import com.builtbroken.atomic.content.armor.ArmorRadData;
import com.builtbroken.atomic.content.armor.ArmorRadLevelData;
import com.builtbroken.atomic.content.armor.ArmorRadiationHandler;
import com.builtbroken.atomic.content.machines.laser.emitter.LaserModes;
import com.builtbroken.atomic.network.netty.PacketSystem;
import com.builtbroken.atomic.network.packet.trigger.PacketMouse;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.HashMap;

public class ClientProxy extends CommonProxy
{
    /** Client Cache: Amount of neutrons in the environment were the player is standing */
    public static float NEUT_EXPOSURE = 0;
    /** Client Cache: Amount of rads in the environment were the player is standing */
    public static float RAD_EXPOSURE = 0;
    /** Client Cache: Amount of rads the player has taken */
    public static float RAD_PLAYER = 0;
    /** Client Cache: Time until radiation is removed from the player again */
    public static int RAD_REMOVE_TIMER = 0;

    /** Client Cache: Amount of neutrons in the environment were the player is standing */
    public static float PREV_NEUT_EXPOSURE = 0;
    /** Client Cache: Amount of rads in the environment were the player is standing */
    public static float PREV_RAD_EXPOSURE = 0;
    /** Client Cache: Amount of rads the player has taken */
    public static float PREV_RAD_PLAYER = 0;

    public static final HashMap<String, ParticleSpawnFunction> PARTICLE_HANDLERS = new HashMap();

    private final float[] colorGetArray = new float[4];

    public ClientProxy()
    {
        super("ClientProxy");
    }

    @Override
    public void preInit()
    {
        OBJLoader.INSTANCE.addDomain(AtomicScience.DOMAIN);
        MinecraftForge.EVENT_BUS.register(this);

        //Register particles
        PARTICLE_HANDLERS.put(EffectRefs.LAZER_NORMAL, (x, y, z, vx, vy, vz) -> lazer(x, y, z, LaserModes.NORMAL));
        PARTICLE_HANDLERS.put(EffectRefs.LAZER_FIELD, (x, y, z, vx, vy, vz) -> lazer(x, y, z, LaserModes.FIELD));
        PARTICLE_HANDLERS.put(EffectRefs.LAZER_NORMAL_FIRE, (x, y, z, vx, vy, vz) -> lazerFire(x, y, z, vx, vy, vz, LaserModes.NORMAL));
        PARTICLE_HANDLERS.put(EffectRefs.LAZER_FIELD_FIRE, (x, y, z, vx, vy, vz) -> lazerFire(x, y, z, vx, vy, vz, LaserModes.FIELD));
    }

    @Override
    public void init()
    {
        super.init();
    }

    @SubscribeEvent
    public void mouseEvent(MouseEvent e)
    {
        EntityPlayer player = Minecraft.getMinecraft().player;

        for (EnumHand hand : EnumHand.values())
        {
            ItemStack stack = player.getHeldItem(hand);
            if (stack.getItem() == ASItems.itemWrench)
            {
                if (player.isSneaking() && e.getDwheel() != 0)
                {
                    boolean ctrl = Keyboard.isKeyDown(Keyboard.KEY_RCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_LCONTROL);
                    PacketSystem.INSTANCE.sendToServer(new PacketMouse(player.inventory.currentItem, ctrl, e.getDwheel() > 0));
                    e.setCanceled(true);
                }
                break;
            }
        }
    }

    @SubscribeEvent
    public void onToolTip(ItemTooltipEvent event)
    {
        ArmorRadData armorRadData = ArmorRadiationHandler.getArmorRadData(event.getItemStack());
        if (armorRadData != null)
        {
            //TODO show protection for current area
            //TODO use shift to show protection for all levels

            for (int i = 0; i < armorRadData.radiationLevels.size(); i++)
            {
                ArmorRadLevelData armorRadLevelData = armorRadData.radiationLevels.get(i);
                String prefx = "[" + i + "] ";
                event.getToolTip().add(prefx + armorRadLevelData.levelStart + "rads");
                event.getToolTip().add(padLeft("  -" + armorRadLevelData.protection_percent + "%  -" + armorRadLevelData.protection_flat, prefx.length() + 1));
            }
        }
    }

    public static final FontRenderer getFont(ItemStack stack)
    {
        FontRenderer font = stack.getItem().getFontRenderer(stack);
        if (font != null)
        {
            return font;
        }
        return Minecraft.getMinecraft().fontRenderer;
    }

    private static String padLeft(String s, int n)
    {
        return String.format("%1$" + n + "s", s);
    }

    @Override
    public void spawnParticle(String particle, double x, double y, double z, double vx, double vy, double vz)
    {
        //TODO build an effect system to register effects
        if (Minecraft.getMinecraft().world != null)
        {
            if (PARTICLE_HANDLERS.containsKey(particle))
            {
                PARTICLE_HANDLERS.get(particle).spawn(x, y, z, vx, vy, vz);
            }


            //TODO convert to handlers
            else if (particle.startsWith(EffectRefs.STEAM))
            {
                //TODO implement
            }
            else if (particle.startsWith(EffectRefs.REACTOR_RUNNING))
            {
                reactorRunning(x, y, z);
            }
            else if (particle.startsWith(EffectRefs.BOILING))
            {
                int count = Integer.parseInt(particle.split(";")[1]); //TODO move to packet
                boiling(x, y, z, count);
            }
            else if (particle.equalsIgnoreCase(EffectRefs.BOILER_COMPLETE))
            {
                boilerComplete(x, y, z);
            }
            else if (particle.equalsIgnoreCase(EffectRefs.BOILER_RUNNING))
            {
                centrifugeRunning(x, y, z);
            }
            else if (particle.equalsIgnoreCase(EffectRefs.CENTRIFUGE_COMPLETE))
            {
                centrifugeComplete(x, y, z);
            }
            else if (particle.equalsIgnoreCase(EffectRefs.CENTRIFUGE_RUNNING))
            {
                centrifugeRunning(x, y, z);
            }
            else if (particle.equalsIgnoreCase(EffectRefs.EXTRACTOR_COMPLETE))
            {
                extractorComplete(x, y, z, (int) vx);
            }
            else if (particle.equalsIgnoreCase(EffectRefs.EXTRACTOR_RUNNING))
            {
                extractorRunning(x, y, z, (int) vx);
            }
            else if (particle.equalsIgnoreCase(EffectRefs.ACCELERATOR_PARTICLE))
            {
                Minecraft.getMinecraft().world.spawnParticle(EnumParticleTypes.HEART,
                        x + r(0.4),
                        y + 0.6 + r(0.1),
                        z + r(0.4),
                        0, 0, 0);
            }
        }
    }

    private void boiling(double x, double y, double z, int count)
    {
        if (ConfigClient.PARTICLES.BOILING_EFFECT)
        {
            final int xi = (int) Math.floor(x);
            final int yi = (int) Math.floor(y);
            final int zi = (int) Math.floor(z);

            final BlockPos blockPos = new BlockPos(xi, yi, zi);

            IBlockState blockState = Minecraft.getMinecraft().world.getBlockState(blockPos.up());
            boolean isAir = blockState.getBlock().isAir(blockState, Minecraft.getMinecraft().world, blockPos.up());

            for (int i = 0; i < count; i++)
            {
                if (isAir)
                {
                    Minecraft.getMinecraft().world.spawnParticle(EnumParticleTypes.WATER_SPLASH,
                            x + r(0.4),
                            y + 0.6 + r(0.1),
                            z + r(0.4),
                            0, 0, 0);
                }

                Minecraft.getMinecraft().world.spawnParticle(EnumParticleTypes.WATER_BUBBLE,
                        x + r(0.5),
                        y + r(0.5),
                        z + r(0.5),
                        r(0.1) - r(0.1),
                        r(0.1),
                        r(0.1) - r(0.1));
            }
        }
    }

    private void boilerComplete(double x, double y, double z)
    {
        if (ConfigClient.PARTICLES.MACHINE_COMPLETE)
        {
            final float randomSpeed = 0.05f;
            int rand = Minecraft.getMinecraft().world.rand.nextInt(5);
            Color color = Color.GREEN.darker().darker();
            for (int i = 0; i < 10 + rand; i++)
            {
                FxSmoke smoke = new FxSmoke(Minecraft.getMinecraft().world,
                        x,
                        y - 0.3,
                        z,
                        r(randomSpeed) - r(randomSpeed),
                        r(randomSpeed) - r(randomSpeed),
                        r(randomSpeed) - r(randomSpeed),
                        (float) (1f - r(0.2) + r(0.2)));

                if (r(1) > 0.5)
                {
                    color.darker();
                }

                Minecraft.getMinecraft().effectRenderer.addEffect(smoke.setColor(color));
            }

            for (int i = 0; i < 10 + rand; i++)
            {
                FxSmoke smoke = new FxSmoke(Minecraft.getMinecraft().world,
                        x,
                        y,
                        z,
                        r(randomSpeed) - r(randomSpeed),
                        r(randomSpeed) - r(randomSpeed),
                        r(randomSpeed) - r(randomSpeed),
                        (float) (1f - r(0.2) + r(0.2)));

                if (r(1) > 0.5)
                {
                    color.darker();
                }

                Minecraft.getMinecraft().effectRenderer.addEffect(smoke.setColor(color));
            }
        }
    }

    private void centrifugeComplete(double x, double y, double z)
    {
        if (ConfigClient.PARTICLES.MACHINE_COMPLETE)
        {
            final float randomSpeed = 0.05f;
            int rand = Minecraft.getMinecraft().world.rand.nextInt(5);
            Color color = Color.GREEN.darker().darker();
            for (int i = 0; i < 10 + rand; i++)
            {
                FxSmoke smoke = new FxSmoke(Minecraft.getMinecraft().world,
                        x,
                        y,
                        z,
                        r(randomSpeed) - r(randomSpeed),
                        r(randomSpeed) - r(randomSpeed),
                        r(randomSpeed) - r(randomSpeed),
                        (float) (1f - r(0.2) + r(0.2)));

                if (r(1) > 0.5)
                {
                    color.darker();
                }

                Minecraft.getMinecraft().effectRenderer.addEffect(smoke.setColor(color));
            }
        }
    }

    private void centrifugeRunning(double x, double y, double z)
    {
        if (ConfigClient.PARTICLES.MACHINE_RUNNING)
        {
            final float randomSpeed = 0.02f;
            FxSmoke smoke = new FxSmoke(Minecraft.getMinecraft().world,
                    x,
                    y,
                    z,
                    r(randomSpeed) - r(randomSpeed),
                    r(randomSpeed) - r(randomSpeed),
                    r(randomSpeed) - r(randomSpeed),
                    (float) (1f - r(0.2) + r(0.2)));

            Minecraft.getMinecraft().effectRenderer.addEffect(smoke.setColor(Color.GREEN));
        }
    }

    private void extractorComplete(double x, double y, double z, int facing)
    {
        if (ConfigClient.PARTICLES.MACHINE_COMPLETE)
        {
            final float randomSpeed = 0.05f;
            int rand = Minecraft.getMinecraft().world.rand.nextInt(5);
            Color color = Color.GREEN.darker().darker();

            EnumFacing direction = EnumFacing.byIndex(facing);
            for (int i = 0; i < 10 + rand; i++)
            {
                FxSmoke smoke = new FxSmoke(Minecraft.getMinecraft().world,
                        x + direction.getXOffset() * 0.2,
                        y + direction.getYOffset() * 0.2,
                        z + direction.getZOffset() * 0.2,
                        r(randomSpeed) - r(randomSpeed),
                        r(randomSpeed) - r(randomSpeed),
                        r(randomSpeed) - r(randomSpeed),
                        (float) (1f - r(0.2) + r(0.2)));

                if (r(1) > 0.5)
                {
                    color.darker();
                }

                Minecraft.getMinecraft().effectRenderer.addEffect(smoke.setColor(color));
            }
        }
    }

    private void extractorRunning(double x, double y, double z, int facing)
    {
        if (ConfigClient.PARTICLES.MACHINE_RUNNING)
        {
            final float randomSpeed = 0.02f;

            EnumFacing direction = EnumFacing.byIndex(facing);

            FxSmoke smoke = new FxSmoke(Minecraft.getMinecraft().world,
                    x + direction.getXOffset() * 0.2,
                    y + direction.getYOffset() * 0.2,
                    z + direction.getZOffset() * 0.2,
                    r(randomSpeed) - r(randomSpeed),
                    r(randomSpeed) - r(randomSpeed),
                    r(randomSpeed) - r(randomSpeed),
                    (float) (1f - r(0.2) + r(0.2)));
            Minecraft.getMinecraft().effectRenderer.addEffect(smoke.setColor(Color.GREEN));

            smoke = new FxSmoke(Minecraft.getMinecraft().world,
                    x + direction.getXOffset() * 0.2 + direction.getZOffset() * 0.3,
                    y + direction.getYOffset() * 0.2,
                    z + direction.getZOffset() * 0.2 + direction.getXOffset() * 0.3,
                    r(randomSpeed) - r(randomSpeed),
                    r(randomSpeed) - r(randomSpeed),
                    r(randomSpeed) - r(randomSpeed),
                    (float) (1f - r(0.2) + r(0.2)));
            Minecraft.getMinecraft().effectRenderer.addEffect(smoke.setColor(Color.GREEN));

            smoke = new FxSmoke(Minecraft.getMinecraft().world,
                    x + direction.getXOffset() * 0.2 - direction.getZOffset() * 0.3,
                    y + direction.getYOffset() * 0.2,
                    z + direction.getZOffset() * 0.2 - direction.getXOffset() * 0.3,
                    r(randomSpeed) - r(randomSpeed),
                    r(randomSpeed) - r(randomSpeed),
                    r(randomSpeed) - r(randomSpeed),
                    (float) (1f - r(0.2) + r(0.2)));
            Minecraft.getMinecraft().effectRenderer.addEffect(smoke.setColor(Color.GREEN));
        }
    }

    private void lazer(double x, double y, double z, LaserModes laserModes)
    {
        for (int i = 0; i < 2; i++)
        {
            spawnParticle(EnumParticleTypes.REDSTONE,
                    x + 0.05 * Math.random(),
                    y + 0.05 * Math.random(),
                    z + 0.05 * Math.random(),
                    laserModes.color);
        }
    }

    private void lazerFire(double sx, double sy, double sz, double ex, double ey, double ez, LaserModes laserModes)
    {
        //Get difference in start and end
        final double deltaX = ex - sx;
        final double deltaY = ey - sy;
        final double deltaZ = ez - sz;

        //Get distance
        final double distance = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY) + (deltaZ * deltaZ));

        //Normalize
        final double vx = deltaX / distance;
        final double vy = deltaY / distance;
        final double vz = deltaZ / distance;

        //Get steps
        final int steps = (int) Math.ceil(distance / 0.2);
        final double dStep = distance / steps;

        //Spawn particles
        for (int step = 0; step < steps; step++)
        {
            final double px = sx + dStep * step * vx;
            final double py = sy + dStep * step * vy;
            final double pz = sz + dStep * step * vz;
            spawnParticle(EnumParticleTypes.REDSTONE, px, py, pz, laserModes.color);
        }

        if (laserModes == LaserModes.FIELD)
        //Spawn particles
        {
            final float rd = 0.3f;
            for (int i = 0; i < 2; i++)
            {
                final double px = ex + Math.random() * rd - Math.random() * rd;
                final double py = ey + Math.random() * rd - Math.random() * rd;
                final double pz = ez + Math.random() * rd - Math.random() * rd;
                spawnParticle(EnumParticleTypes.REDSTONE, px, py, pz, laserModes.color);
            }
        }
    }

    private void spawnParticle(EnumParticleTypes type, double x, double y, double z)
    {
        spawnParticle(type, x, y, z, 0, 0, 0);
    }

    private void spawnParticle(EnumParticleTypes type, double x, double y, double z, Color color)
    {
        float[] colors = color.getRGBComponents(colorGetArray);
        spawnParticle(type, x, y, z, colors[0], colors[1], colors[2]);
    }

    private void spawnParticle(EnumParticleTypes type, double x, double y, double z, double vx, double vy, double vz, int... nums)
    {
        Minecraft.getMinecraft().effectRenderer.spawnEffectParticle(type.getParticleID(), x, y, z, vx, vy, vz, nums);
    }

    private void reactorRunning(double x, double y, double z)
    {
        if (ConfigClient.PARTICLES.REACTOR_RUNNING)
        {
            final float randomSpeed = 0.05f;
            Color color = Color.GREEN;
            for (int j = 0; j < 6; j++)
            {
                for (int i = 0; i < 4; i++)
                {
                    FxSmoke smoke = new FxSmoke(Minecraft.getMinecraft().world,
                            x + r(0.01) - r(0.01),
                            y + 0.15 - 0.15 * j,
                            z + r(0.01) - r(0.01),
                            r(randomSpeed) - r(randomSpeed),
                            r(0.01) - r(0.01),
                            r(randomSpeed) - r(randomSpeed),
                            (float) (1f - r(0.2) + r(0.2)))
                            .setColor(color)
                            .setYAcceleration(0);

                    if (r(1) > 0.5)
                    {
                        color = color.darker();
                    }
                    else
                    {
                        color = color.brighter();
                    }

                    Minecraft.getMinecraft().effectRenderer.addEffect(smoke);
                }
            }
        }
    }

    private double r(double random)
    {
        return Math.random() * random - Math.random() * random;
    }
}
