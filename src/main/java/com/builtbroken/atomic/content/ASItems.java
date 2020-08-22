package com.builtbroken.atomic.content;

import com.builtbroken.atomic.AtomicScience;
import com.builtbroken.atomic.config.content.ConfigContent;
import com.builtbroken.atomic.config.logic.ConfigRadiation;
import com.builtbroken.atomic.content.armor.ItemHazmatColor;
import com.builtbroken.atomic.content.items.ItemFuelRod;
import com.builtbroken.atomic.content.armor.ItemHazmatClassic;
import com.builtbroken.atomic.content.items.ItemHeatProbe;
import com.builtbroken.atomic.content.items.ItemRadioactive;
import com.builtbroken.atomic.content.items.cell.CreativeTabCells;
import com.builtbroken.atomic.content.items.cell.ItemFluidCell;
import com.builtbroken.atomic.content.items.cell.ItemPoweredCell;
import com.builtbroken.atomic.content.items.wrench.ItemWrench;
import net.minecraft.block.Block;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 4/18/2018.
 */
@Mod.EventBusSubscriber(modid = AtomicScience.DOMAIN)
public final class ASItems
{
    //Armor
    public static ItemHazmatClassic itemArmorHazmatHelm;
    public static ItemHazmatClassic itemArmorHazmatChest;
    public static ItemHazmatClassic itemArmorHazmatLegs;
    public static ItemHazmatClassic itemArmorHazmatBoots;

    public static ItemHazmatColor itemArmorHazmatHelmColor;
    public static ItemHazmatColor itemArmorHazmatChestColor;
    public static ItemHazmatColor itemArmorHazmatLegsColor;
    public static ItemHazmatColor itemArmorHazmatBootsColor;

    //Fluid Cells
    public static ItemFluidCell itemFluidCell; //Generic fluid cell (replaces water cell, toxic waste bucket)
    public static ItemPoweredCell itemPoweredCell; //Generic fluid cell (replaces Anti-matter and strange-matter cells)

    //Machine inputs
    public static Item itemFissileFuelCell;
    public static Item itemBreederFuelCell;

    //Crafting items
    public static Item itemEmptyCell; //crafting item (replaces empty cell)
    public static Item itemYellowCake;
    public static Item itemUranium234;
    public static Item itemUranium235;
    public static Item itemUranium238;

    //Waste items
    public static Item itemProcessingWaste;
    public static Item itemToxicWaste;

    //Tools
    public static Item itemHeatProbe;
    public static ItemWrench itemWrench;


    public static ItemBlock blockReactorCell;
    public static ItemBlock blockReactorController;
    public static ItemBlock blockSteamFunnel;
    public static ItemBlock blockSteamTurbine;

    public static ItemBlock blockUraniumOre;

    public static ItemBlock blockChemExtractor;
    public static ItemBlock blockChemBoiler;
    public static ItemBlock blockChemCentrifuge;

    public static ItemBlock blockRodPipe;
    public static ItemBlock blockRodPipeInv;
    public static ItemBlock blockCapRelay;

    public static ItemBlock blockThermalSensorRedstone;

    public static ItemBlock blockMagnet;
    public static ItemBlock blockAcceleratorTube;
    public static ItemBlock blockAcceleratorGun;
    public static ItemBlock blockAcceleratorExit;

    public static ItemBlock blockParticleDetector;

    public static ItemBlock blockLaserEmitter;
    public static ItemBlock blockLaserBooster;

    public static ItemBlock blockItemContainer;

    /** Armor material used by hazmat armor sets */
    public static ItemArmor.ArmorMaterial hazmatArmorMaterial;
    public static ItemArmor.ArmorMaterial hazmatArmorMaterialColor;


    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        //Armor
        hazmatArmorMaterial = EnumHelper.addArmorMaterial(AtomicScience.PREFIX + "hazmat", AtomicScience.PREFIX + "hazmat", 5, new int[]{0, 0, 0, 0}, 0, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0.0F);
        hazmatArmorMaterialColor = EnumHelper.addArmorMaterial(AtomicScience.PREFIX + "hazmat_color", AtomicScience.PREFIX + "hazmat_color", 5, new int[]{0, 0, 0, 0}, 0, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0.0F);

        event.getRegistry().register(itemArmorHazmatHelm = new ItemHazmatClassic(EntityEquipmentSlot.HEAD, "mask"));
        event.getRegistry().register(itemArmorHazmatChest = new ItemHazmatClassic(EntityEquipmentSlot.CHEST, "body"));
        event.getRegistry().register(itemArmorHazmatLegs = new ItemHazmatClassic(EntityEquipmentSlot.LEGS, "leggings"));
        event.getRegistry().register(itemArmorHazmatBoots = new ItemHazmatClassic(EntityEquipmentSlot.FEET, "boots"));

        event.getRegistry().register(itemArmorHazmatHelmColor = new ItemHazmatColor(EntityEquipmentSlot.HEAD, "mask"));
        event.getRegistry().register(itemArmorHazmatChestColor = new ItemHazmatColor(EntityEquipmentSlot.CHEST, "body"));
        event.getRegistry().register(itemArmorHazmatLegsColor = new ItemHazmatColor(EntityEquipmentSlot.LEGS, "leggings"));
        event.getRegistry().register(itemArmorHazmatBootsColor = new ItemHazmatColor(EntityEquipmentSlot.FEET, "boots"));

        //Cells
        event.getRegistry().register(itemFluidCell = (ItemFluidCell) new ItemFluidCell(Fluid.BUCKET_VOLUME).setRegistryName(AtomicScience.PREFIX + "fluid_cell"));
        itemFluidCell.addSupportedFluid(FluidRegistry.WATER, AtomicScience.PREFIX + "items/cell_water", itemFluidCell.getTranslationKey() + ".water");
        itemFluidCell.addSupportedFluid(ASFluids.DEUTERIUM.fluid, AtomicScience.PREFIX + "items/cell_deuterium", itemFluidCell.getTranslationKey() + ".deuterium");


        event.getRegistry().register(itemPoweredCell = (ItemPoweredCell) new ItemPoweredCell().setRegistryName(AtomicScience.PREFIX + "powered_cell"));
        itemPoweredCell.addSupportedFluid(ASFluids.ANTIMATTER.fluid, AtomicScience.PREFIX + "items/cell_antimatter", itemPoweredCell.getTranslationKey() + ".antimatter");
        itemPoweredCell.addSupportedFluid(ASFluids.STRANGE_MATTER.fluid, AtomicScience.PREFIX + "items/cell_strange_matter", itemPoweredCell.getTranslationKey() + ".strange_matter");


        //Machine inputs
        event.getRegistry().register(itemFissileFuelCell = new ItemFuelRod("fissile_fuel_cell", "cell.fuel.fissile",
                () -> ConfigContent.REACTOR.FUEL_ROD_RUNTIME,
                () -> ConfigRadiation.RADIOACTIVE_MAT_VALUE_FUEL_ROD
                , () -> ConfigRadiation.RADIOACTIVE_REACTOR_VALUE_FUEL_ROD,
                () -> ConfigContent.REACTOR.HEAT_REACTOR_FUEL_ROD
                , () -> ConfigRadiation.NEUTRON_VALUE_FUEL_ROD));
        event.getRegistry().register(itemBreederFuelCell = new ItemFuelRod("breeder_fuel_cell", "cell.fuel.breeder",
                () -> ConfigContent.REACTOR.BREEDER_ROD_RUNTIME,
                () -> ConfigRadiation.RADIOACTIVE_MAT_VALUE_BREEDER_ROD
                , () -> ConfigRadiation.RADIOACTIVE_REACTOR_VALUE_BREEDER_ROD,
                () -> ConfigContent.REACTOR.HEAT_REACTOR_BREEDER_ROD
                , () -> ConfigRadiation.NEUTRON_VALUE_BREEDER_ROD));

        //Crafting items
        event.getRegistry().register(itemEmptyCell = new Item()
                .setTranslationKey(AtomicScience.PREFIX + "cell.empty")
                .setCreativeTab(AtomicScience.creativeTab)
                .setRegistryName(AtomicScience.PREFIX + "cell_empty"));

        //https://en.wikipedia.org/wiki/Isotopes_of_uranium
        //https://education.jlab.org/itselemental/ele092.html
        //https://en.wikipedia.org/wiki/Weapons-grade_nuclear_material
        event.getRegistry().register(itemYellowCake = new ItemRadioactive("yellow_cake", "cake.yellow", () -> ConfigRadiation.RADIOACTIVE_MAT_VALUE_YELLOW_CAKE));
        event.getRegistry().register(itemUranium234 = new ItemRadioactive("uranium_234", "uranium.234", () -> ConfigRadiation.RADIOACTIVE_MAT_VALUE_U234)); //Useless
        event.getRegistry().register(itemUranium235 = new ItemRadioactive("uranium_235", "uranium.235", () -> ConfigRadiation.RADIOACTIVE_MAT_VALUE_U235)); //Used as fuel
        event.getRegistry().register(itemUranium238 = new ItemRadioactive("uranium_238", "uranium.238", () -> ConfigRadiation.RADIOACTIVE_MAT_VALUE_U238)); //can be turned into plutonium-239 or depleted uranium

        OreDictionary.registerOre("pelletUranium234", itemUranium235);
        OreDictionary.registerOre("pelletUranium235", itemUranium235);
        OreDictionary.registerOre("pelletUranium238", itemUranium238);

        //Waste items
        event.getRegistry().register(itemProcessingWaste = new ItemRadioactive("processing_waste", "processing.waste", () -> ConfigRadiation.RADIOACTIVE_MAT_VALUE_YELLOW_CAKE));
        event.getRegistry().register(itemToxicWaste = new ItemRadioactive("toxic_waste", "toxic.waste", () -> ConfigRadiation.RADIOACTIVE_MAT_VALUE_YELLOW_CAKE));

        //Tools
        event.getRegistry().register(itemHeatProbe = new ItemHeatProbe());
        event.getRegistry().register(itemWrench = new ItemWrench());

        //Register item blocks
        blockReactorCell = addItemBlock(event.getRegistry(), ASBlocks.blockReactorCell);
        blockReactorController = addItemBlock(event.getRegistry(), ASBlocks.blockReactorController);
        blockSteamFunnel = addItemBlock(event.getRegistry(), ASBlocks.blockSteamFunnel);
        blockSteamTurbine = addItemBlock(event.getRegistry(), ASBlocks.blockSteamTurbine);

        blockUraniumOre = addItemBlock(event.getRegistry(), ASBlocks.blockUraniumOre);
        OreDictionary.registerOre("oreUranium", blockUraniumOre);

        blockChemExtractor = addItemBlock(event.getRegistry(), ASBlocks.blockChemExtractor);
        blockChemBoiler = addItemBlock(event.getRegistry(), ASBlocks.blockChemBoiler);
        blockChemCentrifuge = addItemBlock(event.getRegistry(), ASBlocks.blockChemCentrifuge);

        blockRodPipe = addItemBlock(event.getRegistry(), ASBlocks.blockRodPipe);
        blockRodPipeInv = addItemBlock(event.getRegistry(), ASBlocks.blockRodPipeInv);
        blockCapRelay = addItemBlock(event.getRegistry(), ASBlocks.blockCapRelay);

        blockMagnet = addItemBlock(event.getRegistry(), ASBlocks.blockMagnet);
        blockAcceleratorTube = addItemBlock(event.getRegistry(), ASBlocks.blockAcceleratorTube);
        blockAcceleratorGun = addItemBlock(event.getRegistry(), ASBlocks.blockAcceleratorGun);
        blockAcceleratorExit = addItemBlock(event.getRegistry(), ASBlocks.blockAcceleratorExit);
        blockParticleDetector = addItemBlock(event.getRegistry(), ASBlocks.blockParticleDetector);

        blockLaserEmitter = addItemBlock(event.getRegistry(), ASBlocks.blockLaserEmitter);
        blockLaserBooster = addItemBlock(event.getRegistry(), ASBlocks.blockLaserBooster);

        blockItemContainer = addItemBlock(event.getRegistry(), ASBlocks.blockItemContainer);

        blockThermalSensorRedstone = addItemBlock(event.getRegistry(), ASBlocks.blockThermalSensorRedstone);

        if (AtomicScience.runningAsDev)
        {
            new CreativeTabCells();
        }
    }

    static ItemBlock addItemBlock(IForgeRegistry<Item> reg, Block block)
    {
        ItemBlock item = new ItemBlock(block);
        item.setRegistryName(block.getRegistryName());
        reg.register(item);
        return item;
    }
}
