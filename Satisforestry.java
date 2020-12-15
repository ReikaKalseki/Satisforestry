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
import java.net.URL;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.BiomeManager.BiomeEntry;
import net.minecraftforge.common.BiomeManager.BiomeType;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.terraingen.ChunkProviderEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fluids.FluidRegistry;

import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.DragonOptions;
import Reika.DragonAPI.Auxiliary.Trackers.CommandableUpdateChecker;
import Reika.DragonAPI.Base.DragonAPIMod;
import Reika.DragonAPI.Base.DragonAPIMod.LoadProfiler.LoadPhase;
import Reika.DragonAPI.Instantiable.Event.BlockStopsPrecipitationEvent;
import Reika.DragonAPI.Instantiable.Event.BlockTickEvent;
import Reika.DragonAPI.Instantiable.Event.GenLayerRiverEvent;
import Reika.DragonAPI.Instantiable.Event.GetYToSpawnMobEvent;
import Reika.DragonAPI.Instantiable.Event.IceFreezeEvent;
import Reika.DragonAPI.Instantiable.Event.LightLevelForSpawnEvent;
import Reika.DragonAPI.Instantiable.Event.LightVisualBrightnessEvent;
import Reika.DragonAPI.Instantiable.Event.LightVisualBrightnessEvent.LightMixedBrightnessEvent;
import Reika.DragonAPI.Instantiable.Event.SnowOrIceOnGenEvent;
import Reika.DragonAPI.Instantiable.Event.Client.GrassIconEvent;
import Reika.DragonAPI.Instantiable.Event.Client.LiquidBlockIconEvent;
import Reika.DragonAPI.Instantiable.Event.Client.SinglePlayerLogoutEvent;
import Reika.DragonAPI.Instantiable.Event.Client.WaterColorEvent;
import Reika.DragonAPI.Instantiable.IO.ControlledConfig;
import Reika.DragonAPI.Instantiable.IO.ModLogger;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.Satisforestry.Biome.BiomePinkForest;
import Reika.Satisforestry.Biome.Biomewide.BiomewideFeatureGenerator;
import Reika.Satisforestry.Biome.Biomewide.UraniumCave;
import Reika.Satisforestry.Biome.Generator.WorldGenPinkRiver;
import Reika.Satisforestry.Biome.Generator.WorldGenUraniumCave;
import Reika.Satisforestry.Blocks.BlockPinkGrass;
import Reika.Satisforestry.Blocks.BlockPinkLeaves;
import Reika.Satisforestry.Blocks.BlockPinkLog;
import Reika.Satisforestry.Blocks.BlockRedBamboo;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod( modid = "Satisforestry", name="Satisforestry", version = "v@MAJOR_VERSION@@MINOR_VERSION@", certificateFingerprint = "@GET_FINGERPRINT@", dependencies="required-after:DragonAPI;after:CritterPet")

public class Satisforestry extends DragonAPIMod {

	@Instance("Satisforestry")
	public static Satisforestry instance = new Satisforestry();

	public static final ControlledConfig config = new ControlledConfig(instance, SFOptions.optionList, null);

	public static BlockPinkLog log;
	public static BlockRedBamboo bamboo;
	public static BlockPinkLeaves leaves;
	public static BlockPinkGrass grass;

	public static BiomePinkForest pinkforest;
	//public static BiomePinkRiver pinkriver;

	private IIcon biomeGrassIcon;
	private IIcon biomeGrassIconSide;
	private IIcon biomeWaterIcon;
	private IIcon biomeWaterIconFlow;

	public static CreativeTabs tabCreative = new SatisforestryTab("Satisforestry");

	public static ModLogger logger;

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

		log = new BlockPinkLog();
		GameRegistry.registerBlock(log, null, "pinklog");
		LanguageRegistry.addName(log, "Pink Birch Log");
		bamboo = new BlockRedBamboo();
		GameRegistry.registerBlock(bamboo, null, "redbamboo");
		LanguageRegistry.addName(bamboo, "Red Bamboo");
		leaves = new BlockPinkLeaves();
		GameRegistry.registerBlock(leaves, null, "pinkleaves");
		LanguageRegistry.addName(leaves, "Pink Birch Leaves");
		grass = new BlockPinkGrass();
		GameRegistry.registerBlock(grass, null, "pinkgrass");
		LanguageRegistry.addName(grass, "Pink Grass");

		proxy.registerSounds();

		this.basicSetup(evt);
		FMLCommonHandler.instance().bus().register(this);
		this.finishTiming();
	}

	@Override
	@EventHandler
	public void load(FMLInitializationEvent event) {
		this.startTiming(LoadPhase.LOAD);
		proxy.registerRenderers();

		pinkforest = new BiomePinkForest(SFOptions.BIOMEID.getValue());
		BiomeManager.addBiome(BiomeType.COOL, new BiomeEntry(pinkforest, 4));
		BiomeManager.addSpawnBiome(pinkforest);
		BiomeManager.addStrongholdBiome(pinkforest);
		//BiomeManager.addVillageBiome(pinkforest, true);
		BiomeManager.removeVillageBiome(pinkforest);
		BiomeDictionary.registerBiomeType(pinkforest, BiomeDictionary.Type.FOREST, BiomeDictionary.Type.MAGICAL, BiomeDictionary.Type.DENSE, BiomeDictionary.Type.LUSH, BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.WET);

		//pinkriver = new BiomePinkRiver();

		this.finishTiming();
	}

	@Override
	@EventHandler
	public void postload(FMLPostInitializationEvent evt) {
		this.startTiming(LoadPhase.POSTLOAD);

		BiomeConfig.instance.loadConfigs();

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

	@SubscribeEvent
	public void meltSnowIce(BlockTickEvent evt) {
		if (!evt.world.isRaining() && evt.world.isDaytime() && evt.getBiome() instanceof BiomePinkForest && evt.world.canBlockSeeTheSky(evt.xCoord, evt.yCoord+1, evt.zCoord)) {
			if (evt.block == Blocks.snow_layer)
				evt.world.setBlockToAir(evt.xCoord, evt.yCoord, evt.zCoord);
			else if (evt.block == Blocks.ice)
				evt.world.setBlock(evt.xCoord, evt.yCoord, evt.zCoord, Blocks.water);
		}
	}

	@SubscribeEvent
	public void preventNewIce(IceFreezeEvent evt) {
		if (evt.getBiome() instanceof BiomePinkForest) {
			evt.setResult(Result.DENY);
		}
	}

	@SubscribeEvent
	public void preventSnowGen(SnowOrIceOnGenEvent evt) {
		if (evt.getBiome() instanceof BiomePinkForest) {
			evt.setResult(Result.DENY);
		}
	}

	@SubscribeEvent
	public void shapePinkForest(ChunkProviderEvent.ReplaceBiomeBlocks evt) {
		if (evt.world != null && evt.blockArray != null) {
			pinkforest.shapeTerrain(evt.world, evt.chunkX, evt.chunkZ, evt.blockArray, evt.metaArray);
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void retextureGrass(GrassIconEvent evt) {
		if (evt.getBiome() instanceof BiomePinkForest) {
			evt.icon = evt.isTop ? biomeGrassIcon : biomeGrassIconSide;
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void retextureWater(LiquidBlockIconEvent evt) {
		if (evt.getBiome() instanceof BiomePinkForest) {
			if (evt.originalIcon == FluidRegistry.WATER.getFlowingIcon())
				evt.icon = biomeWaterIconFlow;
			else if (evt.originalIcon == FluidRegistry.WATER.getStillIcon())
				evt.icon = biomeWaterIcon;
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void textureHook(TextureStitchEvent.Pre event) {
		if (event.map.getTextureType() == 0) {
			biomeGrassIcon = event.map.registerIcon("Satisforestry:grass_top");
			biomeGrassIconSide = event.map.registerIcon("Satisforestry:grass_side_overlay");
			biomeWaterIconFlow = event.map.registerIcon("Satisforestry:water/water_flow");
			biomeWaterIcon = event.map.registerIcon("Satisforestry:water/water_still");
		}
	}

	@SubscribeEvent
	public void changePinkRivers(GenLayerRiverEvent evt) {
		if (evt.originalBiomeID == pinkforest.biomeID) {
			//evt.riverBiomeID = pinkriver.biomeID;
			evt.setResult(Result.DENY);
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void clearBiomeRiver(SinglePlayerLogoutEvent evt) {
		WorldGenPinkRiver.clearLakeCache();
		WorldGenUraniumCave.clearCaveCache();
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void clearBiomeRiver(ClientDisconnectionFromServerEvent evt) {
		WorldGenPinkRiver.clearLakeCache();
		WorldGenUraniumCave.clearCaveCache();
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void forestWaterColor(WaterColorEvent evt) {
		if (isPinkForest(evt.getBiome())) {
			evt.color = pinkforest.getWaterColor(evt.access, evt.xCoord, evt.yCoord, evt.zCoord, evt.getLightLevel());
		}
	}

	@SubscribeEvent
	public void caveSpawns(WorldEvent.PotentialSpawns evt) {
		if (isPinkForest(evt.world, evt.x, evt.z)) {
			if (BiomewideFeatureGenerator.instance.isInCave(evt.world, evt.x, evt.y, evt.z)) {
				evt.list.clear();
				evt.list.add(UraniumCave.instance.getRandomSpawn());
			}
		}
	}

	@SubscribeEvent
	public void brighterDarkness(LightVisualBrightnessEvent evt) {
		if (isPinkForest(evt.getBiome())) {
			//evt.brightness = evt.getBrightnessFor(Math.min(15, evt.lightLevel+1));
		}
	}

	@SubscribeEvent
	public void brighterDarkness(LightMixedBrightnessEvent evt) {
		if (isPinkForest(evt.getBiome())) {
			//evt.value = evt.getBrightnessFor(Math.min(15, evt.blockLight+1), Math.min(15, evt.skyLight+2));
		}
	}

	/*
	@SubscribeEvent
	public void spidersAtAllBrightness(CheckSpawn evt) {
		if (evt.entity instanceof EntitySpider && this.isPinkForest(evt.entity.worldObj, MathHelper.floor_float(evt.x), MathHelper.floor_float(evt.z))) {
			evt.setResult(Result.ALLOW);
		}
	}
	 */
	@SubscribeEvent
	public void fallproofSpiders(LivingHurtEvent evt) {
		if (evt.entity instanceof EntitySpider && evt.source == DamageSource.fall && this.isPinkForest(evt.entity.worldObj, MathHelper.floor_double(evt.entity.posX), MathHelper.floor_double(evt.entity.posZ))) {
			evt.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void spidersAtAllBrightness(LightLevelForSpawnEvent evt) {
		if (evt.mob instanceof EntitySpider && this.isPinkForest(evt.entity.worldObj, evt.entityX, evt.entityZ)) {
			evt.setResult(Result.ALLOW);
		}
	}

	@SubscribeEvent
	public void mobSpawnY(GetYToSpawnMobEvent evt) {
		if (this.isPinkForest(evt.world, evt.xCoord, evt.zCoord)) {
			int dy = evt.yToTry-1;
			Block at = evt.world.getBlock(evt.xCoord, dy, evt.zCoord);
			while (evt.yToTry > 1 && (at == Blocks.air || at == Satisforestry.log || at == Satisforestry.leaves || at.isWood(evt.world, evt.xCoord, dy, evt.zCoord) || at.isLeaves(evt.world, evt.xCoord, dy, evt.zCoord) || ReikaWorldHelper.softBlocks(evt.world, evt.xCoord, dy, evt.zCoord))) {
				evt.yToTry--;
				dy--;
				at = evt.world.getBlock(evt.xCoord, dy, evt.zCoord);
			}
			//ReikaJavaLibrary.pConsole(evt.yToTry+" from "+evt.yCoord+" > "+evt.world.getBlock(evt.xCoord, evt.yCoord, evt.zCoord)+" to "+evt.world.getBlock(evt.xCoord, evt.yToTry, evt.zCoord), evt.yCoord != evt.yToTry);
			/*
			int top = DecoratorPinkForest.getTrueTopAt(evt.world, evt.xCoord, evt.zCoord)+1;
			evt.yToTry = Math.min(evt.yToTry, top);
			if (false && evt.world.rand.nextInt(2) == 0) {
				evt.yToTry = top;
			}*/
		}
	}

	@SubscribeEvent
	public void snowThroughPinkLeaves(BlockStopsPrecipitationEvent evt) {
		if (evt.block == leaves) {
			evt.setResult(Result.DENY);
		}
	}

	public static boolean isPinkForest(World world, int x, int z) {
		return isPinkForest(world.getWorldChunkManager().getBiomeGenAt(x, z));
	}

	public static boolean isPinkForest(BiomeGenBase b) {
		return b instanceof BiomePinkForest;
	}

}
