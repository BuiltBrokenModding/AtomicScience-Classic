package com.builtbroken.atomic.api;

import com.builtbroken.atomic.api.accelerator.IAcceleratorMagnet;
import com.builtbroken.atomic.api.accelerator.IAcceleratorTube;
import com.builtbroken.atomic.api.effect.IIndirectEffectType;
import com.builtbroken.atomic.api.neutron.INeutronExposureSystem;
import com.builtbroken.atomic.api.neutron.INeutronSource;
import com.builtbroken.atomic.api.radiation.IRadiationExposureSystem;
import com.builtbroken.atomic.api.radiation.IRadiationResistant;
import com.builtbroken.atomic.api.radiation.IRadiationSource;
import com.builtbroken.atomic.api.radiation.IRadioactiveMaterialSystem;
import com.builtbroken.atomic.api.thermal.IThermalSource;
import com.builtbroken.atomic.api.thermal.IThermalSystem;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

/**
 * Reference object for Atomic Science API
 *
 *
 * Created by Dark(DarkGuardsman, Robert) on 3/13/2017.
 */
public final class AtomicScienceAPI
{
    /** Generic neutron application, used to apply neutron radiation to entity (does not cause damage) */
    public static IIndirectEffectType NEUTRON;
    
    /** Generic radiation application, used to apply radiation to entity (does not cause damage) */
    public static IIndirectEffectType RADIATION;

    /** Generic radiation damage type */
    public static IIndirectEffectType RADIATION_DAMAGE;

    //DO NOT override these, the option is there but its not used that way
    //  these are meant to exposure access to the system. A lot of internal
    //  code does not use the API directly. Meaning replacing these will
    //  result in mixed logic of some stuff changing while others don't.
    /** System used to access radiation exposure information */
    public static IRadiationExposureSystem radiationExposureSystem;
    
    /** System used to access neutron exposure information */
    public static INeutronExposureSystem neutronExposureSystem;

    /** System used to access radioactive material data on the map */
    public static IRadioactiveMaterialSystem radioactiveMaterialSystem;

    /** System used to access thermal data on the map */
    public static IThermalSystem thermalSystem;

    @CapabilityInject(IThermalSource.class)
    public static Capability<IThermalSource> THERMAL_CAPABILITY = null;
    
    @CapabilityInject(INeutronSource.class)
    public static Capability<INeutronSource> NEUTRON_CAPABILITY = null;

    @CapabilityInject(IRadiationSource.class)
    public static Capability<IRadiationSource> RADIATION_CAPABILITY = null;

    @CapabilityInject(IRadiationResistant.class)
    public static Capability<IRadiationResistant> RADIATION_RESISTANT_CAPABILITY = null;

    @CapabilityInject(IAcceleratorTube.class)
    public static Capability<IAcceleratorTube> ACCELERATOR_TUBE_CAPABILITY = null;

    @CapabilityInject(IAcceleratorMagnet.class)
    public static Capability<IAcceleratorMagnet> ACCELERATOR_MAGNET_CAPABILITY = null;
}
