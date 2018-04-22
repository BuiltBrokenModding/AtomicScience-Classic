package com.builtbroken.atomic.content.effects.type;

import com.builtbroken.atomic.api.effect.IIndirectEffectSource;
import com.builtbroken.atomic.config.ConfigRadiation;
import com.builtbroken.atomic.content.ASIndirectEffects;
import com.builtbroken.atomic.content.effects.IndirectEffectType;
import com.builtbroken.atomic.content.effects.events.IndirectEffectEntityEvent;
import com.builtbroken.atomic.lib.network.packet.sync.PacketPlayerRadiation;
import com.builtbroken.atomic.lib.network.netty.PacketSystem;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;

/**
 * Indirect effect type for radiation
 * <p>
 * Acts as a catch-all for any ionization-radiation type that can harm an entity.
 * <p>
 * Subtypes will funnel into this type to still allow tracking individual types.
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/21/2018.
 */
public class IETRadiation extends IndirectEffectType
{
    //As a note, the value is actually REM and not RAD.
    //  This is done to track radiation from different sources
    //  As each source of radiation will cause different scales of damage
    //  However, to keep things simple, all values are converted to REM

    public IETRadiation()
    {
        super("radiation");
    }

    @Override
    public void applyIndirectEffect(IIndirectEffectSource source, Entity target, float power)
    {
        IndirectEffectEntityEvent.Pre effectEntityEvent = new IndirectEffectEntityEvent.Pre(source, this, target, power, power);
        if (!effectEntityEvent.isCanceled())
        {
            //Get data
            final NBTTagCompound radiation_data = ASIndirectEffects.getRadiationData(target, true);

            //Get last RAD value
            float rads = radiation_data.getFloat(ASIndirectEffects.NBT_RADS);

            //Add
            rads += effectEntityEvent.appliedPower;

            //Set new value
            radiation_data.setFloat(ASIndirectEffects.NBT_RADS, Math.max(0, Math.min(ConfigRadiation.RADIATION_DEATH_POINT, rads)));

            //Track last time value was set
            radiation_data.setLong(ASIndirectEffects.NBT_RADS_ADD, System.currentTimeMillis());
        }
    }

    @SubscribeEvent
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent event)
    {
        if (!event.entity.worldObj.isRemote)
        {
            EntityLivingBase entity = event.entityLiving;
            if (ASIndirectEffects.hasRadiationData(entity))
            {
                NBTTagCompound data = ASIndirectEffects.getRadiationData(entity, false);

                //TODO use Java 8 functions or interface object to trigger effects (makes the code easier to work with)
                //TODO slowly decrease (1 rad per 5 min with an exponential curve, do not remove negative effects)
                //TODO reduce max HP base on rad level (1Hp per 10 rad, max of 10hp [50%])
                //TODO if over 200 start removing hp slowly (simulation radiation poisoning)
                //TODO if over 100 have character suffer potion effects
                //TODO if over 1000, kill character

                //Sync data to client if changes
                if (entity instanceof EntityPlayerMP)
                {
                    //Limit precision errors
                    final float syncError = 0.001f;

                    //Check exposure change
                    float exposure = 0f; //TODO calculate exposure this tick were the player is standing
                    float prev_exposure = data.getFloat(ASIndirectEffects.NBT_RADS_ENVIROMENT_PREV);
                    float delta_exposure = Math.abs(prev_exposure - exposure);

                    //Check rad change
                    float rad = data.getFloat(ASIndirectEffects.NBT_RADS);
                    float prev_rad = data.getFloat(ASIndirectEffects.NBT_RADS_PREV);
                    float delta_rad = Math.abs(prev_rad - rad);

                    //Only sync if change has happened in data
                    if (delta_rad > syncError || delta_exposure > syncError || ((EntityPlayerMP) entity).ticksExisted % 20 == 0)
                    {
                        PacketSystem.INSTANCE.sendToPlayer(new PacketPlayerRadiation(rad, exposure), (EntityPlayerMP) entity);

                        //Update previous, always do in sync to prevent slow creep of precision errors
                        data.setFloat(ASIndirectEffects.NBT_RADS_PREV, rad);
                        data.setFloat(ASIndirectEffects.NBT_RADS_ENVIROMENT_PREV, exposure);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event)
    {
        if (!event.entity.worldObj.isRemote)
        {
            //Clear data client side for respawn
            if (event.entity instanceof EntityPlayerMP)
            {
                PacketSystem.INSTANCE.sendToPlayer(new PacketPlayerRadiation(0, 0), (EntityPlayerMP) event.entity);
            }
        }
    }
}
