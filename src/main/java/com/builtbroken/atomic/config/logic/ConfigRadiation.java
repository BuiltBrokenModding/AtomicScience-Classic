package com.builtbroken.atomic.config.logic;

import com.builtbroken.atomic.AtomicScience;
import com.builtbroken.atomic.proxy.ContentProxy;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

/**
 * Main config for the mod
 *
 *
 * Created by Dark(DarkGuardsman, Robert) on 4/18/2018.
 */
public class ConfigRadiation extends ContentProxy
{
    //-------------------------------------------------------------------------------
    //--------- Data for generating config values -----------------------------------
    //-------------------------------------------------------------------------------
    //Value used to convert Mev to 1 RAD (should be equal to Mev / 624151 * 100)
    //1 erg = 624151 Mev
    //1 rad = 100 erg
    public static final double MeV_per_RAD = 6.24E7;

    //Mega eV per gram of material
    public static float MEV_GRAM_U238 = 4.267f; //https://en.wikipedia.org/wiki/Uranium-238
    public static float MEV_GRAM_U235 = 4.679f; //https://en.wikipedia.org/wiki/Uranium-235
    public static float MEV_GRAM_U234 = 4.679f;

    //mass in grams of sample items % of material
    public static float MASS_U238_SAMPLE = 500;
    public static float MASS_U235_SAMPLE = 500;
    public static float MASS_U234_SAMPLE = 500;

    //Activity (number of decays per tick)
    public static float ACTIVITY_U238 = 12445; //TODO get actual numbers, then scale
    public static float ACTIVITY_U235 = 80011; //TODO get actual numbers, then scale
    public static float ACTIVITY_U234 = 80011;
    //-------------------------------------------------------------------------------


    //-------------------------------------------------------------------------------
    //Start of configs  TODO CONFIG
    //-------------------------------------------------------------------------------

    /** Enable radiation events */
    public static boolean ENABLE_EXPOSURE = true;

    /** Enable radiation map */
    public static boolean ENABLE_MAP = true;

    /** Enable tracking of inventories for radiation */
    public static boolean ENABLE_INVENTORY = true;

    /** Enable tracking of entity items (dropped items) */
    public static boolean ENABLE_ENTITY_ITEMS = true;

    /** Enable tracking of inventories for entities */
    public static boolean ENABLE_ENTITY = true;

    /** Time to wait from last radiation remove to remove again */
    public static int RAD_REMOVE_TIMER = 5 * 60 * 20; //5mins
    /** Amount of radiation to remove (as a percentage) */
    public static float RAD_REMOVE_PERCENTAGE = 0.05f;
    public static float RAD_REMOVE_LOWER_LIMIT = 1f;

    /** Amount of rads required to kill the player */
    public static float RADIATION_DEATH_POINT = 10000;

    /** Amount of rads required to start causing problems */
    public static float RADIATION_SICKNESS_POINT = 1000;

    /** Amount of rads required to start causing problems */
    public static float RADIATION_WEAKNESS_POINT = 5000;

    /** Amount of rads required to start causing problems */
    public static float RADIATION_CONFUSION_POINT = 8000;

    //Scale value to convert RADs to REMs
    //RBE -> relative biological effectiveness (how each type relates to xray & gamma)
    //https://community.dur.ac.uk/ian.terry/teaching/nplab/dose_test.htm
    public static float RBE_XRAY_RADIATION = 1; //will go through entity
    public static float RBE_GAMMA_RADIATION = 1; //will go through entity
    public static float RBE_NEURTONS_FAST = 10; //will go through entity
    public static float RBE_NEURTONS_SLOW = 5; //will go through entity
    public static float RBE_ALPHA_RADIATION = 20; //stopped by anything
    public static float RBE_BETA_RADIATION = 10; //stopped by entity

    //Radiation per hour (mass * energy * decay_rate / MeV to rad
    public static float RAD_U234 = (float) (MASS_U234_SAMPLE * MEV_GRAM_U234 * ACTIVITY_U234 / MeV_per_RAD); //alpha radiation
    public static float RAD_U235 = (float) (MASS_U235_SAMPLE * MEV_GRAM_U235 * ACTIVITY_U235 / MeV_per_RAD); //alpha radiation
    public static float RAD_U238 = (float) (MASS_U238_SAMPLE * MEV_GRAM_U238 * ACTIVITY_U238 / MeV_per_RAD); //alpha radiation

    /** How many points of map radioactive material converts to 1 RAD */
    public static float MAP_VALUE_TO_MILI_RAD = 0.01f; //100 material to 1/1000th of a RAD (material is a placeholder values since grams will not work)

    public static int RADIOACTIVE_MAT_VALUE_U234 = (int) Math.ceil(RAD_U235 / MAP_VALUE_TO_MILI_RAD);
    public static int RADIOACTIVE_MAT_VALUE_U235 = (int) Math.ceil(RAD_U235 / MAP_VALUE_TO_MILI_RAD);
    public static int RADIOACTIVE_MAT_VALUE_U238 = (int) Math.ceil(RAD_U238 / MAP_VALUE_TO_MILI_RAD);

    public static int RADIOACTIVE_MAT_VALUE_YELLOW_CAKE = RADIOACTIVE_MAT_VALUE_U235 / 10;
    public static int RADIOACTIVE_MAT_VALUE_FUEL_ROD = RADIOACTIVE_MAT_VALUE_U235 * 100;
    public static int RADIOACTIVE_MAT_VALUE_BREEDER_ROD = RADIOACTIVE_MAT_VALUE_FUEL_ROD / 10;

    public static int RADIOACTIVE_REACTOR_VALUE_FUEL_ROD = RADIOACTIVE_MAT_VALUE_FUEL_ROD * 100;
    public static int RADIOACTIVE_REACTOR_VALUE_BREEDER_ROD = RADIOACTIVE_MAT_VALUE_BREEDER_ROD * 100;
    
    public static int NEUTRON_VALUE_FUEL_ROD = RADIOACTIVE_REACTOR_VALUE_FUEL_ROD * 2;
    public static int NEUTRON_VALUE_BREEDER_ROD = 0;

    public static float RADIATION_DECAY_PER_BLOCK = 0.05f;
    public static float RADIATION_DECAY_PER_FLUID = 0.15f;
    public static float RADIATION_DECAY_METAL = 0.50f;
    public static float RADIATION_DECAY_STONE = 0.20f;
    
    public static float NEUTRON_DECAY_PER_BLOCK = 0.10f;
    public static float NEUTRON_DECAY_PER_FLUID = 0.45f;
    public static float NEUTRON_DECAY_METAL = 0.25f;
    public static float NEUTRON_DECAY_STONE = 0.30f;


    public ConfigRadiation()
    {
        super("config.rad");
    }

    @Override
    public void preInit()
    {
        Configuration configuration = new Configuration(new File(AtomicScience.configFolder, "Radiation.cfg"), AtomicScience.VERSION);
        configuration.load();
        ENABLE_EXPOSURE = configuration.getBoolean("enable_exposure", Configuration.CATEGORY_GENERAL, ENABLE_EXPOSURE,
                "(true -> on, false -> off) Enabled event handling used to apply radiation to entities and update damage effects.");

        ENABLE_MAP = configuration.getBoolean("enable_map", Configuration.CATEGORY_GENERAL, ENABLE_MAP,
                "(true -> on, false -> off) Enabled events used to update the radiation map. If disabled other radiation systems will stop working as well. " +
                        "However, the mod is playable as this is just related to radiation used to harm entities, machines, and items.");

        //Map settings
        final String cat_map = "rad_map";

        MAP_VALUE_TO_MILI_RAD = configuration.getFloat("material_to_radiation", cat_map, MAP_VALUE_TO_MILI_RAD, 0.0001f, 100,
                "Conversation rate of material on the map to radiation values produced. Value is material -> milli-rad. " +
                        "Keep value low as map is limited to ~2.7 billion for values. Meaning values to large will not function." +
                        "Example (good) 0.01 * 10000 = 100");

        //Entity settings
        final String cat_entity = "entity";
        RAD_REMOVE_TIMER = configuration.getInt("rad_remove_timer", cat_entity, RAD_REMOVE_TIMER, 1, Integer.MAX_VALUE,
                "Amount of time in ticks (20 ticks a second) to wait before removing radiation");

        RAD_REMOVE_PERCENTAGE = configuration.getFloat("rad_remove_percentage", cat_entity, RAD_REMOVE_PERCENTAGE, 0, 1, "Percentage of radiation to remove each removal cycle");
        RAD_REMOVE_LOWER_LIMIT = configuration.getFloat("rad_remove_lower_limit", cat_entity, RAD_REMOVE_LOWER_LIMIT, 0, 100000, "Amount that once below radiation is set to zero");

        RADIATION_DEATH_POINT = configuration.getFloat("death_radiation_point", cat_entity, RADIATION_DEATH_POINT, 1, Integer.MAX_VALUE, "Amount of radiation before the player dies");

        //Material settings
        final String cat_rad_mat = "source_rad_material_values";
        configuration.setCategoryComment(cat_rad_mat, "Amount of radioactive material present inside of each source. This value is used to calculate radiation to emmit.");
        RADIOACTIVE_MAT_VALUE_U235 = configuration.getInt("U235", cat_rad_mat, RADIOACTIVE_MAT_VALUE_U235, 1, Integer.MAX_VALUE, "Radiation material value for U235 pellet");
        RADIOACTIVE_MAT_VALUE_U238 = configuration.getInt("U238", cat_rad_mat, RADIOACTIVE_MAT_VALUE_U238, 1, Integer.MAX_VALUE, "Radiation material value for U238 pellet");
        RADIOACTIVE_MAT_VALUE_YELLOW_CAKE = configuration.getInt("yellowcake", cat_rad_mat, RADIOACTIVE_MAT_VALUE_YELLOW_CAKE, 1, Integer.MAX_VALUE, "Radiation material value for yellowcake");
        RADIOACTIVE_MAT_VALUE_FUEL_ROD = configuration.getInt("fuel_rod", cat_rad_mat, RADIOACTIVE_MAT_VALUE_FUEL_ROD, 1, Integer.MAX_VALUE, "Radiation material value for fission fuel rod");
        RADIOACTIVE_MAT_VALUE_BREEDER_ROD = configuration.getInt("breeder_rod", cat_rad_mat, RADIOACTIVE_MAT_VALUE_BREEDER_ROD, 1, Integer.MAX_VALUE, "Radiation material value for fission breeder rod");

        configuration.save();
    }
}
