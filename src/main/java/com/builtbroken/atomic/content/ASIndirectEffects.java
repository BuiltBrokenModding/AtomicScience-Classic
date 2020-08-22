package com.builtbroken.atomic.content;

import com.builtbroken.atomic.AtomicScience;
import com.builtbroken.atomic.api.AtomicScienceAPI;
import com.builtbroken.atomic.api.armor.IAntiPoisonArmor;
import com.builtbroken.atomic.api.effect.IIndirectEffectInstance;
import com.builtbroken.atomic.api.effect.IIndirectEffectSource;
import com.builtbroken.atomic.api.effect.IIndirectEffectType;
import com.builtbroken.atomic.config.logic.ConfigRadiation;
import com.builtbroken.atomic.content.effects.IndirectEffectInstance;
import com.builtbroken.atomic.content.effects.RadiationEntityEventHandler;
import com.builtbroken.atomic.content.effects.type.IETRadiation;
import com.builtbroken.atomic.content.effects.type.IETRadiationDamage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 4/20/2018.
 */
public class ASIndirectEffects
{
    /** NBT tag used to store rads on an entity */
    public static final String NBT_RADIATION_DATA = AtomicScience.PREFIX + "radiation_data";
    /** NBT tag used to store rads on an entity */
    public static final String NBT_RADS = "rads";
    /** NBT tag used to track prev value to see if a packet is needed */
    public static final String NBT_RADS_PREV = "prev_rads";
    /** NBT tag used to track prev environment value to see if a packet is needed */
    public static final String NBT_RADS_ENVIROMENT_PREV = "prev_env_rads";
    public static final String NBT_NEUTRON_ENVIROMENT_PREV = "prev_env_neuts";

    /** NBT tag used to store last rad remove time on an entity */
    public static final String NBT_RADS_REMOVE_TIMER = "remove_timer";

    public static void register()
    {
        AtomicScienceAPI.RADIATION = new IETRadiation();
        AtomicScienceAPI.RADIATION_DAMAGE = new IETRadiationDamage();

        if (ConfigRadiation.ENABLE_EXPOSURE)
        {
            RadiationEntityEventHandler.init();
            MinecraftForge.EVENT_BUS.register(new RadiationEntityEventHandler());
        }
    }

    //TODO track entities and tiles (to produce radiation in environment)
    //TODO build a radiation environment map, save to chunk
    //TODO use tracking data to cause radiation to spawn from entities
    //TODO create function list to allow entities to control logic
    //TODO create ban list to disable running on some entities (mainly for entities that handle logic themselves)

    public static NBTTagCompound getRadiationData(Entity entity, boolean init)
    {
        if (!hasRadiationData(entity) && init)
        {
            entity.getEntityData().setTag(NBT_RADIATION_DATA, new NBTTagCompound());
        }
        return entity.getEntityData().getCompoundTag(NBT_RADIATION_DATA);
    }

    /**
     * Called to set an entities radiation value
     *
     * @param entity
     * @param value
     */
    public static void setRadiation(Entity entity, float value)
    {
        NBTTagCompound data = getRadiationData(entity, true);
        data.setFloat(NBT_RADS, value);
    }

    /**
     * Called to get an entities radiation value
     *
     * @param entity
     * @return
     */
    public static float getRadiation(Entity entity)
    {
        if (hasRadiationData(entity))
        {
            return getRadiationData(entity, true).getFloat(NBT_RADS);
        }
        return 0;
    }

    /**
     * Called to add radiation to the entity
     *
     * @param entity
     * @param value
     */
    public static void addRadiation(Entity entity, float value)
    {
        float current = getRadiation(entity);
        current += value;
        setRadiation(entity, current);

        //Rest remove timer, as it can only run if radiation is not being added
        NBTTagCompound data = getRadiationData(entity, true);
        data.setInteger(NBT_RADS_REMOVE_TIMER, 0);

        //TODO fire event
    }

    /**
     * Called to remove radiation from the entity
     *
     * @param entity
     * @param value
     */
    public static void removeRadiation(Entity entity, float value)
    {
        float current = getRadiation(entity);
        current -= value;
        setRadiation(entity, Math.max(0, current));
        //TODO fire event
    }

    /**
     * Check if the entity has radiation data
     *
     * @param entity
     * @return
     */
    public static boolean hasRadiationData(Entity entity)
    {
        return entity.getEntityData().hasKey(NBT_RADIATION_DATA, 10);
    }

    /**
     * Applies a generic radiation type to an entity with the amount
     *
     * @param entity - entity to hit
     * @param source - source of the radiation
     * @param amount - amount of radiation
     */
    public static void applyIndirectEffect(EntityLivingBase entity, IIndirectEffectType type,  IIndirectEffectSource source, float amount)
    {
        IndirectEffectInstance indirectEffectInstance = new IndirectEffectInstance(type, source, amount);
        applyIndirectEffect(entity, indirectEffectInstance);
    }

    /**
     * Called to apply an indirect effect to the entity. Handles protection checks, events, and armor callbacks.
     *
     * @param entity                 - entity to hit
     * @param indirectEffectInstance - instance containing source, power, and type
     */
    public static void applyIndirectEffect(EntityLivingBase entity, IIndirectEffectInstance indirectEffectInstance)
    {
        if (isProtected(entity, indirectEffectInstance))
        {
            onProtected(entity, indirectEffectInstance);
        }
        else
        {
            indirectEffectInstance.applyIndirectEffect(entity);
        }
    }

    /**
     * Called to check if the entity is protected from the radiation source
     *
     * @param entity                 - entity to hit
     * @param indirectEffectInstance - instance containing source, power, and type
     * @return true if protected
     */
    public static boolean isProtected(EntityLivingBase entity, IIndirectEffectInstance indirectEffectInstance)
    {
        //Loop armor 1-4
        for (int i = 2; i < 6; i++)
        {
            final ItemStack stack = entity.getItemStackFromSlot(EntityEquipmentSlot.values()[i]);

            //Armor set is checked by the armor itself. In theory this means the first item should return true for a full set.
            if (stack != null && stack.getItem() instanceof IAntiPoisonArmor
                    && ((IAntiPoisonArmor) stack.getItem()).doesArmorProtectFromSource(stack, entity, indirectEffectInstance))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Called to apply post protection callbacks for armor. Used to damage armor and cause secondary effects.
     *
     * @param entity                 - entity to hit
     * @param indirectEffectInstance - instance containing source, power, and type
     */
    public static void onProtected(EntityLivingBase entity, IIndirectEffectInstance indirectEffectInstance)
    {
        for (int i = 2; i < 6; i++)
        {
            final ItemStack stack = entity.getItemStackFromSlot(EntityEquipmentSlot.values()[i]);

            //Armor set is checked by the armor itself. In theory this means the first item should return true for a full set.
            if (stack != null && stack.getItem() instanceof IAntiPoisonArmor)
            {
                ((IAntiPoisonArmor) stack.getItem()).onArmorProtectFromSource(stack, entity, indirectEffectInstance);
            }
        }
    }
}
