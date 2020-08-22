package com.builtbroken.atomic;

import com.builtbroken.atomic.api.accelerator.IAcceleratorMagnet;
import com.builtbroken.atomic.api.accelerator.IAcceleratorTube;
import com.builtbroken.atomic.api.neutron.INeutronSource;
import com.builtbroken.atomic.api.radiation.IRadiationResistant;
import com.builtbroken.atomic.api.radiation.IRadiationSource;
import com.builtbroken.atomic.api.thermal.IThermalSource;
import com.builtbroken.atomic.content.ASFluids;
import com.builtbroken.atomic.content.ASIndirectEffects;
import com.builtbroken.atomic.content.ASItems;
import com.builtbroken.atomic.content.ASWorldGen;
import com.builtbroken.atomic.content.armor.ArmorRadLevelData;
import com.builtbroken.atomic.content.armor.ArmorRadiationHandler;
import com.builtbroken.atomic.content.commands.CommandAS;
import com.builtbroken.atomic.content.machines.accelerator.magnet.CapabilityMagnet;
import com.builtbroken.atomic.content.machines.processing.ProcessorRecipeHandler;
import com.builtbroken.atomic.lib.MassHandler;
import com.builtbroken.atomic.lib.placement.PlacementQueue;
import com.builtbroken.atomic.lib.neutron.NeutronHandler;
import com.builtbroken.atomic.lib.radiation.RadiationHandler;
import com.builtbroken.atomic.lib.thermal.ThermalHandler;
import com.builtbroken.atomic.map.MapHandler;
import com.builtbroken.atomic.map.exposure.thread.ThreadRadExposure;
import com.builtbroken.atomic.map.exposure.node.RadSourceMap;
import com.builtbroken.atomic.map.neutron.node.NeutronSourceMap;
import com.builtbroken.atomic.map.neutron.thread.ThreadNeutronExposure;
import com.builtbroken.atomic.map.thermal.thread.ThreadThermalAction;
import com.builtbroken.atomic.map.thermal.node.ThermalSourceMap;
import com.builtbroken.atomic.network.netty.PacketSystem;
import com.builtbroken.atomic.proxy.Mods;
import com.builtbroken.atomic.proxy.ProxyLoader;
import com.builtbroken.atomic.proxy.bc.ProxyBuildcraftEnergy;
import com.builtbroken.atomic.proxy.eu.ProxyIC2;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;

/**
 * Main mod class, handles references and registry calls
 * <p>
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 4/18/2018.
 */
@Mod(modid = AtomicScience.DOMAIN, name = "Atomic Science", version = AtomicScience.VERSION, dependencies = AtomicScience.DEPENDENCIES)
@Mod.EventBusSubscriber
public class AtomicScience
{
    public static final String DOMAIN = "atomicscience";
    public static final String PREFIX = DOMAIN + ":";

    public static final String MAJOR_VERSION = "@MAJOR@";
    public static final String MINOR_VERSION = "@MINOR@";
    public static final String REVISION_VERSION = "@REVIS@";
    public static final String BUILD_VERSION = "@BUILD@";
    public static final String MC_VERSION = "@MC@";
    public static final String VERSION = MC_VERSION + "-" + MAJOR_VERSION + "." + MINOR_VERSION + "." + REVISION_VERSION + "." + BUILD_VERSION;
    public static final String DEPENDENCIES = "";

    public static final String TEXTURE_DIRECTORY = "textures/";
    public static final String GUI_TEXTURE_DIRECTORY = TEXTURE_DIRECTORY + "gui/";
    public static final String MODEL_TEXTURE_DIRECTORY = TEXTURE_DIRECTORY + "models/";

    public static final int TICKS_SECOND = 20;
    public static final int TICKS_MIN = TICKS_SECOND * 60;
    public static final int TICKS_HOUR = TICKS_MIN * 60;

    @Mod.Instance(DOMAIN)
    public static AtomicScience INSTANCE;

    public static Logger logger = LogManager.getLogger(DOMAIN);

    public static final boolean runningAsDev = System.getProperty("development") != null && System.getProperty("development").equalsIgnoreCase("true");

    public static CreativeTabs creativeTab;

    @SidedProxy(clientSide = "com.builtbroken.atomic.client.ClientProxy", serverSide = "com.builtbroken.atomic.ServerProxy")
    public static CommonProxy sideProxy;

    public static ProxyLoader proxyLoader;

    public static File configFolder;

    public AtomicScience()
    {
        if (runningAsDev)
        {
            FluidRegistry.enableUniversalBucket();
        }
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event)
    {

    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        configFolder = new File(event.getModConfigurationDirectory(), "/bbm/atomicscience");

        //Create tab
        creativeTab = new CreativeTabs(DOMAIN)
        {
            @Override
            public ItemStack createIcon()
            {
                return new ItemStack(ASItems.itemArmorHazmatHelm);
            }
        };

        //Register handlers
        NetworkRegistry.INSTANCE.registerGuiHandler(this, sideProxy);
        MinecraftForge.EVENT_BUS.register(new PlacementQueue());

        MapHandler.register();
        ThermalHandler.init();
        RadiationHandler.init();
        NeutronHandler.init();
        MassHandler.init();

        proxyLoader = new ProxyLoader("AS");

        //Content
        proxyLoader.add(new ASFluids.Proxy()); //must run before items and blocks
        proxyLoader.add(new ASWorldGen());

        //Recipes
        proxyLoader.add(ProcessorRecipeHandler.INSTANCE);

        //Handlers
        proxyLoader.add(PacketSystem.INSTANCE);
        proxyLoader.add(sideProxy);

        if (Mods.IC2.isLoaded())
        {
            proxyLoader.add(new ProxyIC2());
        }
        if (Mods.BUILDCRAFT_ENERGY.isLoaded())
        {
            proxyLoader.add(new ProxyBuildcraftEnergy());
        }

        //Register content
        ASIndirectEffects.register();

        //Proxy
        proxyLoader.preInit();

        //Used to compare rendering
        if (runningAsDev)
        {
            for (ASFluids value : ASFluids.values())
            {
                FluidRegistry.addBucketForFluid(value.fluid);
            }
        }

        registerCaps();
    }

    public void registerCaps()
    {
        CapabilityManager.INSTANCE.register(IRadiationSource.class, new Capability.IStorage<IRadiationSource>()
                {
                    @Nullable
                    @Override
                    public NBTBase writeNBT(Capability<IRadiationSource> capability, IRadiationSource instance, EnumFacing side)
                    {
                        return null;
                    }

                    @Override
                    public void readNBT(Capability<IRadiationSource> capability, IRadiationSource instance, EnumFacing side, NBTBase nbt)
                    {

                    }
                },
                () -> new RadSourceMap(0, BlockPos.ORIGIN, 0));

        CapabilityManager.INSTANCE.register(INeutronSource.class, new Capability.IStorage<INeutronSource>()
        {
            @Nullable
            @Override
            public NBTBase writeNBT(Capability<INeutronSource> capability, INeutronSource instance, EnumFacing side)
            {
                return null;
            }

            @Override
            public void readNBT(Capability<INeutronSource> capability, INeutronSource instance, EnumFacing side, NBTBase nbt)
            {

            }
        },
        () -> new NeutronSourceMap(0, BlockPos.ORIGIN, 0));
        
        CapabilityManager.INSTANCE.register(IThermalSource.class, new Capability.IStorage<IThermalSource>()
                {
                    @Nullable
                    @Override
                    public NBTBase writeNBT(Capability<IThermalSource> capability, IThermalSource instance, EnumFacing side)
                    {
                        return null;
                    }

                    @Override
                    public void readNBT(Capability<IThermalSource> capability, IThermalSource instance, EnumFacing side, NBTBase nbt)
                    {

                    }
                },
                () -> new ThermalSourceMap(0, BlockPos.ORIGIN, 0));

        CapabilityManager.INSTANCE.register(IRadiationResistant.class, new Capability.IStorage<IRadiationResistant>()
                {
                    @Nullable
                    @Override
                    public NBTBase writeNBT(Capability<IRadiationResistant> capability, IRadiationResistant instance, EnumFacing side)
                    {
                        return null;
                    }

                    @Override
                    public void readNBT(Capability<IRadiationResistant> capability, IRadiationResistant instance, EnumFacing side, NBTBase nbt)
                    {

                    }
                },
                () -> (IRadiationResistant) () -> 0);

        CapabilityManager.INSTANCE.register(IAcceleratorMagnet.class, new Capability.IStorage<IAcceleratorMagnet>()
                {
                    @Nullable
                    @Override
                    public NBTBase writeNBT(Capability<IAcceleratorMagnet> capability, IAcceleratorMagnet instance, EnumFacing side)
                    {
                        return null;
                    }

                    @Override
                    public void readNBT(Capability<IAcceleratorMagnet> capability, IAcceleratorMagnet instance, EnumFacing side, NBTBase nbt)
                    {

                    }
                },
                () -> new CapabilityMagnet(null));

        CapabilityManager.INSTANCE.register(IAcceleratorTube.class, new Capability.IStorage<IAcceleratorTube>()
                {
                    @Nullable
                    @Override
                    public NBTBase writeNBT(Capability<IAcceleratorTube> capability, IAcceleratorTube instance, EnumFacing side)
                    {
                        return null;
                    }

                    @Override
                    public void readNBT(Capability<IAcceleratorTube> capability, IAcceleratorTube instance, EnumFacing side, NBTBase nbt)
                    {

                    }
                },
                () -> null);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent evt)
    {
        //Proxy
        proxyLoader.init();

        ArmorRadiationHandler.getArmorRadData(new ItemStack(Items.LEATHER_CHESTPLATE), true)
                .addRadiationLevel(new ArmorRadLevelData(0).setProtectionFlat(20).setTranslationKey(AtomicScience.PREFIX + ":basic"));
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        //Proxy
        proxyLoader.postInit();
    }

    @Mod.EventHandler
    public void loadComplete(FMLLoadCompleteEvent event)
    {
        //Proxy
        proxyLoader.loadComplete();
    }

    @Mod.EventHandler
    public void serverAboutToStart(FMLServerAboutToStartEvent event)
    {
        //Start thread
        MapHandler.THREAD_RAD_EXPOSURE = new ThreadRadExposure();
        MapHandler.THREAD_RAD_EXPOSURE.start(); //TODO switch to worker thread
        MapHandler.THREAD_THERMAL_ACTION = new ThreadThermalAction();
        MapHandler.THREAD_THERMAL_ACTION.start(); //TODO switch to worker thread
        MapHandler.THREAD_NEUTRON_EXPOSURE = new ThreadNeutronExposure();
        MapHandler.THREAD_NEUTRON_EXPOSURE.start(); //TODO switch to worker thread
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        // Setup command
        ICommandManager commandManager = FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager();
        ServerCommandManager serverCommandManager = ((ServerCommandManager) commandManager);
        serverCommandManager.registerCommand(new CommandAS());
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event)
    {
        //Kill old thread
        MapHandler.THREAD_RAD_EXPOSURE.kill();
        MapHandler.THREAD_THERMAL_ACTION.kill();
        MapHandler.THREAD_NEUTRON_EXPOSURE.kill();
    }

    /**
     * Inject the new values and save to the config file when the config has been changed from the GUI.
     *
     * @param event The event
     */
    @SubscribeEvent
    public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if (event.getModID().equals(DOMAIN))
        {
            ConfigManager.sync(DOMAIN, Config.Type.INSTANCE);
        }
    }
}
