/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.Satisforestry;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Strings;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.BiomeManager.BiomeEntry;
import net.minecraftforge.common.BiomeManager.BiomeType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.DragonOptions;
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.Auxiliary.WorldGenInterceptionRegistry;
import Reika.DragonAPI.Auxiliary.Trackers.CommandableUpdateChecker;
import Reika.DragonAPI.Auxiliary.Trackers.FurnaceFuelRegistry;
import Reika.DragonAPI.Base.DragonAPIMod;
import Reika.DragonAPI.Base.DragonAPIMod.LoadProfiler.LoadPhase;
import Reika.DragonAPI.Instantiable.IO.ControlledConfig;
import Reika.DragonAPI.Instantiable.IO.ModLogger;
import Reika.DragonAPI.Libraries.ReikaRecipeHelper;
import Reika.DragonAPI.Libraries.ReikaRegistryHelper;
import Reika.DragonAPI.Libraries.IO.ReikaPacketHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaTreeHelper;
import Reika.DragonAPI.ModInteract.ItemStackRepository;
import Reika.DragonAPI.ModInteract.ReikaClimateControl;
import Reika.DragonAPI.ModInteract.ItemHandlers.BCPipeHandler;
import Reika.DragonAPI.ModInteract.ItemHandlers.BCPipeHandler.Pipes;
import Reika.DragonAPI.ModInteract.ItemHandlers.IC2Handler;
import Reika.DragonAPI.ModInteract.ItemHandlers.IC2Handler.IC2Stacks;
import Reika.DragonAPI.ModInteract.RecipeHandlers.ForestryRecipeHelper;
import Reika.DragonAPI.ModRegistry.PowerTypes;
import Reika.RotaryCraft.API.RecipeInterface;
import Reika.Satisforestry.Biome.BiomePinkForest;
import Reika.Satisforestry.Biome.CaveNightvisionHandler;
import Reika.Satisforestry.Biome.Biomewide.PointSpawnSystem;
import Reika.Satisforestry.Biome.Generator.PinkTreeGeneratorBase.PinkTreeTypes;
import Reika.Satisforestry.Blocks.BlockMinerMulti.MinerBlocks;
import Reika.Satisforestry.Blocks.BlockPinkLeaves;
import Reika.Satisforestry.Blocks.BlockPinkLog;
import Reika.Satisforestry.Config.BiomeConfig;
import Reika.Satisforestry.Miner.TileFrackingPressurizer;
import Reika.Satisforestry.Miner.TileNodeHarvester;
import Reika.Satisforestry.Registry.SFBlocks;
import Reika.Satisforestry.Registry.SFEntities;
import Reika.Satisforestry.Registry.SFOptions;
import Reika.Satisforestry.Render.ShaderActivation;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import forestry.api.recipes.IFermenterRecipe;
import forestry.api.recipes.ISqueezerRecipe;
import forestry.api.recipes.RecipeManagers;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.recipe.RecipeManaInfusion;

@Mod( modid = "Satisforestry", name="Satisforestry", version = "v@MAJOR_VERSION@@MINOR_VERSION@", certificateFingerprint = "@GET_FINGERPRINT@", dependencies="required-after:DragonAPI;after:CritterPet;after:RotaryCraft;after:IC2;after:ThermalExpansion;before:climatecontrol")

public class Satisforestry extends DragonAPIMod {

	@Instance("Satisforestry")
	public static Satisforestry instance = new Satisforestry();

	public static final String packetChannel = "SFPacketData";

	public static final ControlledConfig config = new ControlledConfig(instance, SFOptions.optionList, null);

	public static BlockPinkLog log;
	public static BlockPinkLeaves leaves;

	public static final Material slugMaterial = new Material(MapColor.foliageColor) {

		@Override
		public boolean blocksMovement() {
			return true;
		}

		@Override
		public boolean isOpaque() {
			return false;
		}

		@Override
		public boolean isReplaceable() {
			return false;
		}

		@Override
		public boolean getCanBlockGrass() {
			return false;
		}

		@Override
		public boolean isToolNotRequired() {
			return true;
		}

	};

	public static BiomePinkForest pinkforest;
	//public static BiomePinkRiver pinkriver;

	public static ItemFood paleberry;
	public static ItemFood sludge;

	public static CreativeTabs tabCreative = new SatisforestryTab("Satisforestry");

	public static ModLogger logger;

	public static Block[] blocks = new Block[SFBlocks.blockList.length];

	@SidedProxy(clientSide="Reika.Satisforestry.SFClient", serverSide="Reika.Satisforestry.SFCommon")
	public static SFCommon proxy;

	@Override
	@EventHandler
	public void preload(FMLPreInitializationEvent evt) {
		this.startTiming(LoadPhase.PRELOAD);
		this.verifyInstallation();
		config.loadSubfolderedConfigFile(evt);
		config.initProps(evt);
		logger = new ModLogger(instance, false);
		if (DragonOptions.FILELOG.getState())
			logger.setOutput("**_Loading_Log.log");

		MinecraftForge.TERRAIN_GEN_BUS.register(SFEvents.instance);
		this.registerEventHandler(SFEvents.instance);
		proxy.loadMusicEngine();

		ReikaPacketHelper.registerPacketHandler(instance, packetChannel, new SFPacketHandler());

		ReikaRegistryHelper.instantiateAndRegisterBlocks(instance, SFBlocks.blockList, blocks);

		log = (BlockPinkLog)SFBlocks.LOG.getBlockInstance();
		leaves = (BlockPinkLeaves)SFBlocks.LEAVES.getBlockInstance();

		for (int i = 0; i < SFBlocks.blockList.length; i++) {
			SFBlocks b = SFBlocks.blockList[i];
			Class c = b.getObjectClass();
			Class[] cs = c.getClasses();
			if (cs != null) {
				for (int k = 0; k < cs.length; k++) {
					Class in = cs[k];
					if (TileEntity.class.isAssignableFrom(in) && (in.getModifiers() & Modifier.ABSTRACT) == 0) {
						String s = "SF"+in.getSimpleName();
						GameRegistry.registerTileEntity(in, s);
					}
				}
			}
		}
		Class[] cs = TileNodeHarvester.class.getClasses();
		for (int k = 0; k < cs.length; k++) {
			Class in = cs[k];
			if (TileEntity.class.isAssignableFrom(in) && (in.getModifiers() & Modifier.ABSTRACT) == 0) {
				String s = "SF"+in.getSimpleName();
				GameRegistry.registerTileEntity(in, s);
			}
		}
		cs = TileFrackingPressurizer.class.getClasses();
		for (int k = 0; k < cs.length; k++) {
			Class in = cs[k];
			if (TileEntity.class.isAssignableFrom(in) && (in.getModifiers() & Modifier.ABSTRACT) == 0) {
				String s = "SF"+in.getSimpleName();
				GameRegistry.registerTileEntity(in, s);
			}
		}

		paleberry = new ItemPaleberry();
		paleberry.setUnlocalizedName("paleberry");
		GameRegistry.registerItem(paleberry, "paleberry");

		sludge = new ItemDoggoSludge();
		sludge.setUnlocalizedName("doggosludge");
		GameRegistry.registerItem(sludge, "doggosludge");

		this.basicSetup(evt);
		FMLCommonHandler.instance().bus().register(this);
		this.finishTiming();
	}

	public static void registerEventHandler(Object o) {
		MinecraftForge.EVENT_BUS.register(o);
		FMLCommonHandler.instance().bus().register(o);
	}

	@Override
	@EventHandler
	public void load(FMLInitializationEvent event) {
		this.startTiming(LoadPhase.LOAD);
		proxy.registerRenderers();
		proxy.registerSounds();

		NetworkRegistry.INSTANCE.registerGuiHandler(this, new SFGuiHandler());

		this.registerEventHandler(PointSpawnSystem.instance);
		this.registerEventHandler(UpgradeHandler.instance);
		this.registerEventHandler(CaveNightvisionHandler.instance);

		APIObjects.load();

		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			this.clientInit();
		}

		pinkforest = new BiomePinkForest(SFOptions.BIOMEID.getValue());
		BiomeManager.addBiome(BiomeType.COOL, new BiomeEntry(pinkforest, 4));
		BiomeManager.addSpawnBiome(pinkforest);
		BiomeManager.addStrongholdBiome(pinkforest);
		//BiomeManager.addVillageBiome(pinkforest, true);
		BiomeManager.removeVillageBiome(pinkforest);
		BiomeDictionary.registerBiomeType(pinkforest, BiomeDictionary.Type.FOREST, BiomeDictionary.Type.MAGICAL, BiomeDictionary.Type.DENSE, BiomeDictionary.Type.LUSH, BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.WET);

		if (ModList.CLIMATECONTROL.isLoaded())
			ReikaClimateControl.registerBiome(pinkforest, 3, false, "COOL");

		//pinkriver = new BiomePinkRiver();

		ReikaRegistryHelper.registerModEntities(instance, SFEntities.entityList);

		FMLInterModComms.sendMessage(ModList.ARSMAGICA.modLabel, "bsb", "EntityDryad|"+SFOptions.BIOMEID.getValue());

		if (ModList.THAUMCRAFT.isLoaded()) {
			SFThaumHandler.load();
		}

		this.finishTiming();
	}

	@SideOnly(Side.CLIENT)
	private void clientInit() {
		MinecraftForge.EVENT_BUS.register(ShaderActivation.instance);
		String music = SFOptions.MUSIC.getString();
		if (!Strings.isNullOrEmpty(music))
			SFMusic.instance.loadMusic(music);
	}

	private void addRecipes() {
		float xp =  FurnaceRecipes.smelting().func_151398_b(ReikaItemHelper.charcoal);
		for (PinkTreeTypes type : PinkTreeTypes.list) {
			ItemStack log = type.getBaseLog();
			GameRegistry.addShapelessRecipe(new ItemStack(Blocks.planks, 4), log);
			int yield = type.getCharcoalYield();
			ReikaRecipeHelper.addSmelting(log, ReikaItemHelper.getSizedItemStack(ReikaItemHelper.charcoal, yield), xp);

			FurnaceFuelRegistry.instance.registerItem(log, yield*TileEntityFurnace.getItemBurnTime(ReikaTreeHelper.OAK.getItem().asItemStack()));
			FurnaceFuelRegistry.instance.registerItem(type.getSapling(), yield*TileEntityFurnace.getItemBurnTime(ReikaTreeHelper.OAK.getSapling().asItemStack()));
			FurnaceFuelRegistry.instance.registerItem(type.getBaseLeaf(), TileEntityFurnace.getItemBurnTime(ReikaTreeHelper.OAK.getBasicLeaf().asItemStack()));
		}
		OreDictionary.registerOre("logWood", SFBlocks.LOG.getAnyMetaStack());
		OreDictionary.registerOre("treeLeaves", SFBlocks.LEAVES.getAnyMetaStack());
		OreDictionary.registerOre("treeSapling", SFBlocks.SAPLING.getAnyMetaStack());

		ItemStack dark = SFBlocks.MINERMULTI.getStackOfMetadata(MinerBlocks.DARK.ordinal());
		ItemStack silver = SFBlocks.MINERMULTI.getStackOfMetadata(MinerBlocks.SILVER.ordinal());
		ItemStack drillbit = SFBlocks.MINERMULTI.getStackOfMetadata(MinerBlocks.DRILL.ordinal());
		Object steel = getItemWithFallback(ItemStackRepository.instance.getItem(ModList.ROTARYCRAFT, "steelingot"), Items.iron_ingot);
		Object gear = getItemWithFallback(ItemStackRepository.instance.getItem(ModList.ROTARYCRAFT, "steelgear"), Blocks.piston);
		Object impeller = getItemWithFallback(ItemStackRepository.instance.getItem(ModList.ROTARYCRAFT, "impeller"), Blocks.piston);
		Object shaft = getItemWithFallback(ItemStackRepository.instance.getItem(ModList.ROTARYCRAFT, "shaftitem"), Items.iron_ingot);
		Object plate = getItemWithFallback(ItemStackRepository.instance.getItem(ModList.ROTARYCRAFT, "basepanel"), Items.iron_ingot);
		Object panel2 = getItemWithFallback(ItemStackRepository.instance.getItem(ModList.ROTARYCRAFT, "basepanel"), silver);
		Object drill = getItemWithFallback(ItemStackRepository.instance.getItem(ModList.ROTARYCRAFT, "drill"), Items.diamond);
		Object rfcoil = getItemWithFallback(GameRegistry.findItemStack(ModList.THERMALEXPANSION.modLabel, "powerCoilElectrum", 1), Items.redstone);
		Object redblock = getItemWithFallback(ReikaItemHelper.lookupItem("ThermalExpansion:Frame:7"), Blocks.redstone_block);
		Object eucoil = getItemWithFallback(IC2Handler.IC2Stacks.ENERGIUM.getItem(), Items.redstone);
		ItemStack alloy = (ItemStack)getItemWithFallback(IC2Handler.IC2Stacks.ADVANCEDALLOY.getItem(), new ItemStack(Items.iron_ingot));
		Object alloy2 = getItemWithFallback(ItemStackRepository.instance.getItem(ModList.ROTARYCRAFT, "springtungsten"), alloy);
		List<ItemStack> bronze = OreDictionary.getOres("ingotBronze");
		Object orange = getItemWithFallback(bronze.isEmpty() ? null : "ingotBronze", ReikaItemHelper.orangeDye);
		addRecipe(SFBlocks.MINERMULTI.getStackOfMetadata(MinerBlocks.ORANGE.ordinal()), "ioi", "ibi", "ioi", 'b', Blocks.iron_bars, 'o', orange, 'i', Items.iron_ingot);
		addRecipe(dark, "isi", "sbs", "isi", 'b', Blocks.iron_bars, 's', steel, 'i', Items.iron_ingot);
		addRecipe(silver, "bib", "ibi", "bib", 'b', Blocks.iron_bars, 'i', Items.iron_ingot);
		addRecipe(SFBlocks.MINERMULTI.getStackOfMetadata(MinerBlocks.GRAY.ordinal()), "i i", " s ", "i i", 's', steel, 'i', Items.iron_ingot);
		addRecipe(drillbit, "aDa", "dBd", "iRi", 'a', alloy2, 'R', drill, 'D', dark, 'B', Blocks.obsidian, 'd', Items.diamond, 'i', Items.iron_ingot);
		addRecipe(SFBlocks.MINERMULTI.getStackOfMetadata(MinerBlocks.CONVEYOR.ordinal()), "ihi", "pcp", "ihi", 'h', Blocks.hopper, 'c', Blocks.chest, 'p', panel2, 'i', silver);
		addRecipe(SFBlocks.MINERMULTI.getStackOfMetadata(MinerBlocks.HUB.ordinal()), "ihi", "dgd", "ihi", 'g', gear, 'h', shaft, 'd', dark, 'i', plate);
		addRecipe(SFBlocks.MINERMULTI.getStackOfMetadata(MinerBlocks.POWER.ordinal()), "iri", "iri", "iri", 'r', Items.redstone, 'i', Items.iron_ingot);

		Object rfcoil2 = getItemWithFallback(GameRegistry.findItemStack(ModList.THERMALEXPANSION.modLabel, "powerCoilGold", 1), Items.redstone);

		if (PowerTypes.RF.isLoaded())
			addWRRecipe(SFBlocks.HARVESTER.getStackOfMetadata(0), "ici", "iri", "idi", 'c', rfcoil2, 'r', redblock, 'i', Items.iron_ingot, 'd', drillbit);
		if (PowerTypes.EU.isLoaded())
			addWRRecipe(SFBlocks.HARVESTER.getStackOfMetadata(1), "iei", "aca", "idi", 'a', alloy, 'e', eucoil, 'c', ReikaItemHelper.getAnyMetaStack(IC2Stacks.LAPOTRON.getItem()), 'i', Items.iron_ingot, 'd', drillbit);
		if (PowerTypes.ROTARYCRAFT.isLoaded())
			addWRRecipe(SFBlocks.HARVESTER.getStackOfMetadata(2), "bgb", "bGb", "sds", 's', steel, 'b', plate, 'g', gear, 'G', ItemStackRepository.instance.getItem(ModList.ROTARYCRAFT, "gearunit4"), 'd', drillbit);

		if (PowerTypes.RF.isLoaded())
			addWRRecipe(SFBlocks.FRACKER.getStackOfMetadata(0), "ici", "iri", "ipi", 'c', rfcoil2, 'r', redblock, 'i', Items.iron_ingot, 'p', getPipe(false));
		if (PowerTypes.EU.isLoaded())
			addWRRecipe(SFBlocks.FRACKER.getStackOfMetadata(1), "iei", "aca", "ipi", 'a', alloy, 'e', eucoil, 'c', ReikaItemHelper.getAnyMetaStack(IC2Stacks.LAPOTRON.getItem()), 'i', Items.iron_ingot, 'p', getPipe(false));
		if (PowerTypes.ROTARYCRAFT.isLoaded())
			addWRRecipe(SFBlocks.FRACKER.getStackOfMetadata(2), "bGb", "bgb", "sps", 's', steel, 'b', plate, 'G', ItemStackRepository.instance.getItem(ModList.ROTARYCRAFT, "gearunit8"), 'g', impeller, 'p', getPipe(true));

		/*
		if (ModList.THERMALEXPANSION.isLoaded()) {
			ItemStack silverCoil = GameRegistry.findItemStack(ModList.THERMALEXPANSION.modLabel, "powerCoilSilver", 1);
			GameRegistry.addRecipe(ReikaItemHelper.lookupItem("ThermalExpansion:augment:128"), "rcr", "rsr", 'r', Items.redstone, 'c', silverCoil, 's', SFBlocks.SLUG.getStackOfMetadata(0));
			GameRegistry.addRecipe(ReikaItemHelper.lookupItem("ThermalExpansion:augment:129"), "rcr", "rsr", 'r', Items.redstone, 'c', silverCoil, 's', SFBlocks.SLUG.getStackOfMetadata(1));
			GameRegistry.addRecipe(ReikaItemHelper.lookupItem("ThermalExpansion:augment:130"), "rcr", "rsr", 'r', Items.redstone, 'c', silverCoil, 's', SFBlocks.SLUG.getStackOfMetadata(2));

			GameRegistry.addRecipe(ReikaItemHelper.lookupItem("ThermalExpansion:augment:80"), "rcr", "rsr", 'r', Items.redstone, 'c', rfcoil2, 's', SFBlocks.SLUG.getStackOfMetadata(0));
			GameRegistry.addRecipe(ReikaItemHelper.lookupItem("ThermalExpansion:augment:81"), "rcr", "rsr", 'r', Items.redstone, 'c', rfcoil2, 's', SFBlocks.SLUG.getStackOfMetadata(1));
			GameRegistry.addRecipe(ReikaItemHelper.lookupItem("ThermalExpansion:augment:82"), "rcr", "rsr", 'r', Items.redstone, 'c', rfcoil2, 's', SFBlocks.SLUG.getStackOfMetadata(2));
		}
		if (ModList.IC2.isLoaded()) {
			ItemStack is = IC2Handler.IC2Stacks.OVERCLOCK.getItem();
			GameRegistry.addShapelessRecipe(is.copy(), SFBlocks.SLUG.getStackOfMetadata(0));
			GameRegistry.addShapelessRecipe(ReikaItemHelper.getSizedItemStack(is, 2), SFBlocks.SLUG.getStackOfMetadata(1));
			GameRegistry.addShapelessRecipe(ReikaItemHelper.getSizedItemStack(is, 5), SFBlocks.SLUG.getStackOfMetadata(2));
		}
		if (ModList.ENDERIO.isLoaded()) {
			GameRegistry.addShapelessRecipe(ReikaItemHelper.lookupItem("EnderIO:itemBasicCapacitor:0"), SFBlocks.SLUG.getStackOfMetadata(0));
			GameRegistry.addShapelessRecipe(ReikaItemHelper.lookupItem("EnderIO:itemBasicCapacitor:1"), SFBlocks.SLUG.getStackOfMetadata(1));
			GameRegistry.addShapelessRecipe(ReikaItemHelper.lookupItem("EnderIO:itemBasicCapacitor:2"), SFBlocks.SLUG.getStackOfMetadata(2));
		}
		 */
	}

	private static Object getPipe(boolean preferRC) {
		List<Object> li = new ArrayList(Arrays.asList(
				ReikaItemHelper.lookupItem("ImmersiveEngineering:metalDevice2:5"),
				BCPipeHandler.getInstance().getPipe(Pipes.pipeFluidsEmerald),
				ItemStackRepository.instance.getItem(ModList.ROTARYCRAFT, "pipe"),
				Items.bucket
				));
		if (preferRC) {
			li.add(0, li.remove(2));
		}
		Object o = li.remove(0);
		while (o == null)
			o = li.remove(0);
		return o;
	}

	private static void addWRRecipe(ItemStack out, Object... in) {
		if (ModList.ROTARYCRAFT.isLoaded()) {
			RecipeInterface.worktable.addAPIRecipe(new ShapedOreRecipe(out, in));
		}
		else {
			addRecipe(out, in);
		}
	}

	private static void addRecipe(ItemStack out, Object... in) {
		GameRegistry.addRecipe(new ShapedOreRecipe(out, in));
	}

	private static Object getItemWithFallback(Object item, Block back) {
		return getItemWithFallback(item, new ItemStack(back));
	}

	private static Object getItemWithFallback(Object item, Item back) {
		return getItemWithFallback(item, new ItemStack(back));
	}

	private static Object getItemWithFallback(Object item, ItemStack back) {
		return item != null ? item : back;
	}

	@Override
	@EventHandler
	public void postload(FMLPostInitializationEvent evt) {
		this.startTiming(LoadPhase.POSTLOAD);

		this.addRecipes();

		BiomeConfig.instance.loadConfigs();

		//((BlockPowerSlug)SFBlocks.SLUG.getBlockInstance()).updateStepSounds();

		proxy.registerRenderCullingSystem();

		WorldGenInterceptionRegistry.instance.addWatcher(SFAux.populationWatcher);
		WorldGenInterceptionRegistry.instance.addIWGWatcher(SFAux.slimeIslandBlocker);

		if (ModList.BOTANIA.isLoaded()) {
			ItemStack unpacked = PinkTreeTypes.TREE.getBaseLog();
			unpacked.stackTagCompound = new NBTTagCompound();
			unpacked.stackTagCompound.setBoolean("unpacking", true);
			RecipeManaInfusion rec = BotaniaAPI.registerManaInfusionRecipe(unpacked, PinkTreeTypes.JUNGLE.getBaseLog(), 10);
			rec.setAlchemy(true);
		}

		if (ModList.FORESTRY.isLoaded()) {
			ISqueezerRecipe rec = ForestryRecipeHelper.getInstance().getSqueezerOutput(new ItemStack(Items.apple));
			if (rec != null) {
				FluidStack fs = rec.getFluidOutput().copy();
				fs.amount *= 0.75;
				float chance = rec.getRemnantsChance()*100;
				RecipeManagers.squeezerManager.addRecipe(rec.getProcessingTime(), new ItemStack[] {new ItemStack(paleberry)}, fs.copy(), rec.getRemnants(), (int)(chance*1.5));
				fs.amount *= 0.2;
				RecipeManagers.squeezerManager.addRecipe(rec.getProcessingTime(), new ItemStack[] {new ItemStack(paleberry, 1, 1)}, fs.copy(), rec.getRemnants(), (int)(chance*0.25));
			}
			IFermenterRecipe rec2 = ForestryRecipeHelper.getInstance().getFermenterOutput(new ItemStack(Blocks.sapling));
			if (rec2 != null) {
				for (PinkTreeTypes type : PinkTreeTypes.list) {
					float f = Math.max(type.getBerryModifier()*1.2F, 0.2F);
					ForestryRecipeHelper.getInstance().addStandardFermenterRecipes(type.getSapling(), (int)(rec2.getFermentationValue()*f));
				}
			}

			try {
				SFBees.register();
			}
			catch (IncompatibleClassChangeError e) {
				e.printStackTrace();
				logger.logError("Could not add custom bee species. Check your versions; if you are up-to-date with both mods, notify Reika.");
			}
			catch (Exception e) {
				e.printStackTrace();
				logger.logError("Could not add custom bee species. Check your versions; if you are up-to-date with both mods, notify Reika.");
			}
			catch (LinkageError e) {
				e.printStackTrace();
				logger.logError("Could not add custom bee species. Check your versions; if you are up-to-date with both mods, notify Reika.");
			}

		}

		this.finishTiming();
	}

	@EventHandler
	public void lastLoad(FMLServerAboutToStartEvent evt) {
		BiomeConfig.instance.loadConfigs();
	}

	@Override
	public String getDisplayName() {
		return "Satisforestry";
	}

	@Override
	public String getModAuthorName() {
		return "Reika";
	}

	@Override
	public URL getDocumentationSite() {
		return DragonAPICore.getReikaForumPage();
	}

	@Override
	public URL getBugSite() {
		return DragonAPICore.getReikaGithubPage();
	}

	@Override
	public String getWiki() {
		return null;
	}

	@Override
	public String getUpdateCheckURL() {
		return CommandableUpdateChecker.reikaURL;
	}

	@Override
	public ModLogger getModLogger() {
		return logger;
	}

	@Override
	public File getConfigFolder() {
		return config.getConfigFolder();
	}

	public static boolean isPinkForest(World world, int x, int z) {
		return isPinkForest(world.isRemote ? world.getBiomeGenForCoords(x, z) : world.getWorldChunkManager().getBiomeGenAt(x, z));
	}

	public static boolean isPinkForest(BiomeGenBase b) {
		return b instanceof BiomePinkForest;
	}

}
