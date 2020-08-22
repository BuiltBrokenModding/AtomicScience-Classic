package com.builtbroken.atomic.content.effects;

import com.builtbroken.atomic.api.AtomicScienceAPI;
import com.builtbroken.atomic.config.logic.ConfigRadiation;
import com.builtbroken.atomic.content.ASIndirectEffects;
import com.builtbroken.atomic.content.effects.effects.REOPotion;
import com.builtbroken.atomic.content.effects.effects.RadiationEffectOutcome;
import com.builtbroken.atomic.content.effects.source.SourceWrapperPosition;
import com.builtbroken.atomic.map.MapHandler;
import com.builtbroken.atomic.network.netty.PacketSystem;
import com.builtbroken.atomic.network.packet.sync.PacketPlayerRadiation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Handles evens and tracking radiation on the entities
 *
 *
 * Created by Dark(DarkGuardsman, Robert) on 4/29/2018.
 */
public class RadiationEntityEventHandler
{
    public static DamageSource radiationDeathDamage = new DamageSource("radiation").setDamageBypassesArmor().setDamageIsAbsolute();

    public static List<RadiationEffectOutcome> globalEffectList = new ArrayList();
    public static HashMap<Class<? extends EntityLivingBase>, List<RadiationEffectOutcome>> perEntityEffectList = new HashMap();

    public static HashMap<Class<? extends EntityLivingBase>, Float> perEntityRadiationScale = new HashMap();

    //TODO use Java 8 functions or interface object to trigger effects (makes the code easier to work with)
    //TODO reduce max HP base on rad level (1Hp per 10 rad, max of 10hp [50%])
    //TODO if over 200 start removing hp slowly (simulation radiation poisoning)
    //TODO if over 100 have character suffer potion effects
    //TODO if over 1000, kill character

    public static void setMaxRadiation(Class<? extends EntityLivingBase> clazz, float maxValue)
    {
        perEntityRadiationScale.put(clazz, ConfigRadiation.RADIATION_DEATH_POINT / maxValue);
    }

    public static void init()
    {
        //Simulate radiation posing
        globalEffectList.add(new REOPotion(() -> ConfigRadiation.RADIATION_SICKNESS_POINT,
                (entity, rads) -> scaleChance(entity, rads, ConfigRadiation.RADIATION_SICKNESS_POINT),
                entity -> new PotionEffect(MobEffects.HUNGER, 100)));

        //Simulate radiation posing
        globalEffectList.add(new REOPotion(() -> ConfigRadiation.RADIATION_WEAKNESS_POINT,
                (entity, rads) -> scaleChance(entity, rads, ConfigRadiation.RADIATION_WEAKNESS_POINT),
                entity ->
                {
                    if (entity.getEntityWorld().rand.nextBoolean())
                    {
                        return new PotionEffect(MobEffects.WEAKNESS, 100);
                    }
                    else if (entity.getEntityWorld().rand.nextBoolean())
                    {
                        return new PotionEffect(MobEffects.MINING_FATIGUE, 100);
                    }
                    else if (entity.getEntityWorld().rand.nextBoolean())
                    {
                        return new PotionEffect(MobEffects.SLOWNESS, 100);
                    }
                    return null;
                }));

        //Simulate temp blindness
        globalEffectList.add(new REOPotion(() -> ConfigRadiation.RADIATION_CONFUSION_POINT,
                (entity, rads) -> scaleChance(entity, rads, ConfigRadiation.RADIATION_CONFUSION_POINT),
                entity -> new PotionEffect(MobEffects.BLINDNESS, 100)));

        //Simulate high radiation thermal damage
        globalEffectList.add(new RadiationEffectOutcome(() -> ConfigRadiation.RADIATION_DEATH_POINT, (entity, rads, env) -> {

            final float check = 500 / 20f;
            if (env > check) //TODO add config
            {
                float scale = env / check;
                SourceWrapperPosition sourceWrapperPosition = new SourceWrapperPosition( //TODO recycle object
                        entity.world,
                        entity.posX,
                        entity.posY,
                        entity.posZ
                );
                ASIndirectEffects.applyIndirectEffect(entity, AtomicScienceAPI.RADIATION_DAMAGE, sourceWrapperPosition, 1 * scale); //TODO recycle object
            }
        }));

        //Simulate radiation organ failure
        globalEffectList.add(new RadiationEffectOutcome(() -> ConfigRadiation.RADIATION_DEATH_POINT, (entity, rads, env) -> {
            if (scaleToEntity(entity, rads) > ConfigRadiation.RADIATION_DEATH_POINT)
            {
                entity.attackEntityFrom(radiationDeathDamage, 5); //TODO pass through indirect system to allow armor to block
            }
        }));


        setMaxRadiation(EntityBat.class, 200);
        setMaxRadiation(EntityChicken.class, 400);
        setMaxRadiation(EntityParrot.class, 400);
        setMaxRadiation(EntityRabbit.class, 1000);
        setMaxRadiation(EntityOcelot.class, 2000);
        setMaxRadiation(EntityWolf.class, 2500);
        setMaxRadiation(EntitySheep.class, 3000);
        setMaxRadiation(EntityCow.class, 5000);
        setMaxRadiation(EntityPig.class, 5000);


        setMaxRadiation(EntityEndermite.class, 1000);
        setMaxRadiation(EntitySilverfish.class, 1000);
        setMaxRadiation(EntitySlime.class, 8000);
        setMaxRadiation(EntityZombie.class, 15000);
        setMaxRadiation(EntitySkeleton.class, 25000);
        setMaxRadiation(EntityShulker.class, 35000);

        setMaxRadiation(EntityEnderman.class, 45000);
    }

    public static float scaleChance(EntityLivingBase entityLivingBase, float rads, float checkRads)
    {
        return (scaleToEntity(entityLivingBase, rads) - checkRads) / (ConfigRadiation.RADIATION_DEATH_POINT - checkRads);
    }

    public static float scaleToEntity(EntityLivingBase entity, float rads)
    {
        Class<? extends Entity> clazz = entity.getClass();
        if (perEntityRadiationScale.containsKey(clazz))
        {
            return perEntityRadiationScale.get(clazz) * rads;
        }
        return rads;
    }

    @SubscribeEvent
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent event)
    {
        if (!event.getEntity().getEntityWorld().isRemote)
        {
            EntityLivingBase entity = event.getEntityLiving();

            if (entity.isEntityAlive())
            {
                //Increase radiation from environment
                if (!entity.isInvisible() && (!(entity instanceof EntityPlayerMP) || !((EntityPlayerMP) entity).capabilities.isCreativeMode))
                {
                    final float remExposure = MapHandler.RADIATION_MAP.getRemExposure(entity);
                    applyExposure(entity, remExposure);

                    final float rad = ASIndirectEffects.getRadiation(entity);

                    //Apply potion effects
                    if (entity.ticksExisted % 5 == 0)
                    {
                        //Global list of effects
                        globalEffectList.forEach(effect -> effect.applyEffects(entity, rad, remExposure));

                        //Per entity list of effects
                        Class<? extends Entity> clazz = entity.getClass();
                        if (perEntityEffectList.containsKey(clazz))
                        {
                            perEntityEffectList.get(clazz).forEach(effect -> effect.applyEffects(entity, rad, remExposure));
                        }
                    }
                }

                //Sync data to client if changes
                if (entity instanceof EntityPlayerMP)
                {
                    syncPlayerData((EntityPlayerMP) entity);
                }
            }
        }
    }

    /**
     * Applies exposure and reduction of radiation over time
     *
     * @param entity
     */
    protected void applyExposure(EntityLivingBase entity, float remExposure)
    {
        //Apply exposure if greater than zero
        if (remExposure > 0)
        {
            SourceWrapperPosition sourceWrapperPosition = new SourceWrapperPosition(
                    entity.world,
                    entity.posX,
                    entity.posY,
                    entity.posZ
            );
            ASIndirectEffects.applyIndirectEffect(entity, AtomicScienceAPI.RADIATION, sourceWrapperPosition, remExposure);
        }
        else if (ASIndirectEffects.hasRadiationData(entity))
        {
            //Get data
            final NBTTagCompound data = ASIndirectEffects.getRadiationData(entity, false);

            //Only remove after add timer
            int removeTimer = data.getInteger(ASIndirectEffects.NBT_RADS_REMOVE_TIMER);
            if (removeTimer > ConfigRadiation.RAD_REMOVE_TIMER)
            {
                float rad = ASIndirectEffects.getRadiation(entity);
                if (rad <= ConfigRadiation.RAD_REMOVE_LOWER_LIMIT)
                {
                    ASIndirectEffects.setRadiation(entity, 0);
                }
                else
                {
                    //Remove a percentage of radiation
                    float amountToRemove = rad * ConfigRadiation.RAD_REMOVE_PERCENTAGE;
                    ASIndirectEffects.removeRadiation(entity, amountToRemove);
                }

                //Update remove timer
                data.setInteger(ASIndirectEffects.NBT_RADS_REMOVE_TIMER, 0);
            }
            else
            {
                //Update remove timer
                data.setInteger(ASIndirectEffects.NBT_RADS_REMOVE_TIMER, removeTimer + 1);
            }
        }
    }

    /**
     * Handles checking if a packet is needed to update the client
     *
     * @param player
     */
    protected void syncPlayerData(EntityPlayerMP player)
    {
        boolean sendPacket = false;
        //Only sync if we have data to sync
        if (ASIndirectEffects.hasRadiationData(player))
        {
            //Get data
            NBTTagCompound data = ASIndirectEffects.getRadiationData(player, false);

            //Limit precision errors
            final float syncError = 0.001f;

            //Check exposure change
            float exposure = MapHandler.RADIATION_MAP.getRemExposure(player);
            float prev_exposure = data.getFloat(ASIndirectEffects.NBT_RADS_ENVIROMENT_PREV);
            float delta_exposure = Math.abs(prev_exposure - exposure);
            
            float neutrons = MapHandler.NEUTRON_MAP.getNeutronExposure(player);
            float prev_neutrons = data.getFloat(ASIndirectEffects.NBT_NEUTRON_ENVIROMENT_PREV);
            float delta_neutrons = Math.abs(prev_neutrons - neutrons);

            //Check rad change
            float rad = data.getFloat(ASIndirectEffects.NBT_RADS);
            float prev_rad = data.getFloat(ASIndirectEffects.NBT_RADS_PREV);
            float delta_rad = Math.abs(prev_rad - rad);

            //Only sync if change has happened in data
            if (delta_rad > syncError || delta_exposure > syncError || delta_neutrons > syncError || player.ticksExisted % 20 == 0)
            {
                sendPacket = true;
                //Update previous, always do in sync to prevent slow creep of precision errors
                data.setFloat(ASIndirectEffects.NBT_RADS_PREV, rad);
                data.setFloat(ASIndirectEffects.NBT_RADS_ENVIROMENT_PREV, exposure);
                data.setFloat(ASIndirectEffects.NBT_NEUTRON_ENVIROMENT_PREV, neutrons);
            }
        }

        //Update client
        if (sendPacket || player.ticksExisted % 5 == 0)
        {
            sendPacket(player);
        }
    }

    public static void sendPacket(EntityPlayerMP player)
    {
        NBTTagCompound data = ASIndirectEffects.getRadiationData(player, false);
        PacketSystem.INSTANCE.sendToPlayer(new PacketPlayerRadiation(
                data.getFloat(ASIndirectEffects.NBT_RADS),
                MapHandler.RADIATION_MAP.getRemExposure(player),
                data.getInteger(ASIndirectEffects.NBT_RADS_REMOVE_TIMER),
                MapHandler.NEUTRON_MAP.getNeutronExposure(player)), player);
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event)
    {
        if (!event.getEntity().getEntityWorld().isRemote)
        {
            //Clear data client side for respawn
            if (event.getEntity() instanceof EntityPlayerMP)
            {
                float remExposure = MapHandler.RADIATION_MAP.getRemExposure(event.getEntity());
                System.out.println(remExposure);
                PacketSystem.INSTANCE.sendToPlayer(new PacketPlayerRadiation(0, 0, 0, 0), (EntityPlayerMP) event.getEntity());
            }
        }
    }
}
