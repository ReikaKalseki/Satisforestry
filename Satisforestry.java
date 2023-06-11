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
import java.util.HashSet;

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
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

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
import Reika.DragonAPI.ModInteract.ReikaClimateControl;
import Reika.DragonAPI.ModInteract.Power.ReikaBuildCraftHelper;
import Reika.DragonAPI.ModInteract.RecipeHandlers.ForestryRecipeHelper;
import Reika.Satisforestry.Biome.BiomePinkForest;
import Reika.Satisforestry.Biome.CaveNightvisionHandler;
import Reika.Satisforestry.Biome.Biomewide.PointSpawnSystem;
import Reika.Satisforestry.Biome.Generator.PinkTreeGeneratorBase.PinkTreeTypes;
import Reika.Satisforestry.Blocks.BlockFrackerMulti.FrackerBlocks;
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

import blusunrize.immersiveengineering.api.energy.DieselHandler;
import buildcraft.energy.fuels.FuelManager;
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
import ic2.api.recipe.ISemiFluidFuelManager.BurnProperty;
import ic2.api.recipe.Recipes;
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
	public static Item compactedCoal;
	public static Item multiblockPage;

	public static Fluid turbofuel;

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

		HashSet<Class> registered = new HashSet();
		for (int i = 0; i < SFBlocks.blockList.length; i++) {
			SFBlocks b = SFBlocks.blockList[i];
			Class c = b.getObjectClass();
			Class[] cs = c.getClasses();
			if (cs != null) {
				for (int k = 0; k < cs.length; k++) {
					Class in = cs[k];
					if (registered.contains(in))
						continue;
					if (TileEntity.class.isAssignableFrom(in) && (in.getModifiers() & Modifier.ABSTRACT) == 0) {
						String s = "SF"+in.getSimpleName();
						GameRegistry.registerTileEntity(in, s);
						registered.add(in);
					}
				}
			}
		}
		Class[] cs = TileNodeHarvester.class.getClasses();
		for (int k = 0; k < cs.length; k++) {
			Class in = cs[k];
			if (registered.contains(in))
				continue;
			if (TileEntity.class.isAssignableFrom(in) && (in.getModifiers() & Modifier.ABSTRACT) == 0) {
				String s = "SF"+in.getSimpleName();
				GameRegistry.registerTileEntity(in, s);
				registered.add(in);
			}
		}
		cs = TileFrackingPressurizer.class.getClasses();
		for (int k = 0; k < cs.length; k++) {
			Class in = cs[k];
			if (registered.contains(in))
				continue;
			if (TileEntity.class.isAssignableFrom(in) && (in.getModifiers() & Modifier.ABSTRACT) == 0) {
				String s = "SF"+in.getSimpleName();
				GameRegistry.registerTileEntity(in, s);
				registered.add(in);
			}
		}

		paleberry = new ItemPaleberry();
		paleberry.setUnlocalizedName("paleberry");
		GameRegistry.registerItem(paleberry, "paleberry");

		sludge = new ItemDoggoSludge();
		sludge.setUnlocalizedName("doggosludge");
		GameRegistry.registerItem(sludge, "doggosludge");

		compactedCoal = new ItemCompactedCoal();
		compactedCoal.setUnlocalizedName("compactedcoal");
		GameRegistry.registerItem(compactedCoal, "compactedcoal");

		multiblockPage = new ItemMultiblockDisplay();
		multiblockPage.setUnlocalizedName("multiblockpage");
		GameRegistry.registerItem(multiblockPage, "multiblockpage");

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

		Fluid f = FluidRegistry.getFluid("fuel");
		if (f != null) {
			turbofuel = new Fluid("turbofuel").setDensity((int)Math.min(f.getDensity()*1.1, 800)).setViscosity(f.getViscosity());
			FluidRegistry.registerFluid(turbofuel);

		}

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
		FurnaceFuelRegistry.instance.registerItem(new ItemStack(compactedCoal), TileEntityFurnace.getItemBurnTime(new ItemStack(Items.coal))*4);

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

		GameRegistry.addShapelessRecipe(new ItemStack(multiblockPage, 1, 0), Items.paper, Items.paper, Items.paper, SFBlocks.MINERMULTI.getStackOfMetadata(MinerBlocks.ORANGE.ordinal()));
		GameRegistry.addShapelessRecipe(new ItemStack(multiblockPage, 1, 1), Items.paper, Items.paper, Items.paper, SFBlocks.FRACKERMULTI.getStackOfMetadata(FrackerBlocks.ORANGE.ordinal()));

		SFMachineRecipes.instance.addRecipes();
	}

	@Override
	@EventHandler
	public void postload(FMLPostInitializationEvent evt) {
		this.startTiming(LoadPhase.POSTLOAD);

		this.addRecipes();

		BiomeConfig.instance.loadConfigs();

		if (turbofuel != null) {//generates same power as fuel, but lasts 2.5x as long
			Fluid ref = FluidRegistry.getFluid("fuel");
			if (ModList.BCENERGY.isLoaded()) {
				FuelManager.INSTANCE.addFuel(turbofuel, ReikaBuildCraftHelper.getFuelRFPerTick(), ReikaBuildCraftHelper.getFuelBucketDuration()*5/2);
			}
			if (ModList.IC2.isLoaded() && !Recipes.semiFluidGenerator.getBurnProperties().isEmpty()) {
				BurnProperty prop = Recipes.semiFluidGenerator.getBurnProperties().get(ref.getName());
				if (prop != null) {
					Recipes.semiFluidGenerator.addFluid(turbofuel.getName(), prop.amount*2/5, prop.power);
				}
			}
			if (ModList.IMMERSIVEENG.isLoaded()) {
				DieselHandler.registerFuel(turbofuel, DieselHandler.getBurnTime(ref)*5/2);
			}
			if (ModList.RAILCRAFT.isLoaded()) {
				mods.railcraft.api.fuel.FuelManager.addBoilerFuel(turbofuel, mods.railcraft.api.fuel.FuelManager.getBoilerFuelValue(ref)*5/2);
			}
		}

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
