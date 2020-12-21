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

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemFood;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogColors;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.BiomeManager.BiomeEntry;
import net.minecraftforge.common.BiomeManager.BiomeType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.terraingen.ChunkProviderEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fluids.FluidRegistry;

import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.DragonOptions;
import Reika.DragonAPI.Auxiliary.Trackers.CommandableUpdateChecker;
import Reika.DragonAPI.Auxiliary.Trackers.SpecialDayTracker;
import Reika.DragonAPI.Base.DragonAPIMod;
import Reika.DragonAPI.Base.DragonAPIMod.LoadProfiler.LoadPhase;
import Reika.DragonAPI.Instantiable.Event.BlockStopsPrecipitationEvent;
import Reika.DragonAPI.Instantiable.Event.BlockTickEvent;
import Reika.DragonAPI.Instantiable.Event.GenLayerBeachEvent;
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
import Reika.DragonAPI.Libraries.ReikaRegistryHelper;
import Reika.DragonAPI.Libraries.IO.ReikaColorAPI;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.Satisforestry.Biome.BiomePinkForest;
import Reika.Satisforestry.Biome.Biomewide.BiomewideFeatureGenerator;
import Reika.Satisforestry.Biome.Biomewide.UraniumCave;
import Reika.Satisforestry.Biome.Generator.WorldGenPinkRiver;
import Reika.Satisforestry.Biome.Generator.WorldGenUraniumCave;
import Reika.Satisforestry.Blocks.BlockPinkLeaves;
import Reika.Satisforestry.Blocks.BlockPinkLog;

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
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
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
	public static BlockPinkLeaves leaves;

	public static BiomePinkForest pinkforest;
	//public static BiomePinkRiver pinkriver;

	public static ItemFood paleberry;

	private IIcon biomeGrassIcon;
	private IIcon biomeGrassIconSide;
	private IIcon biomeWaterIcon;
	private IIcon biomeWaterIconFlow;

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

		MinecraftForge.TERRAIN_GEN_BUS.register(this);

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

		paleberry = new ItemPaleberry();
		paleberry.setUnlocalizedName("paleberry");
		GameRegistry.registerItem(paleberry, "paleberry");

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

		LanguageRegistry.addName(paleberry, "Paleberries");

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
		if (!evt.world.isRaining() && evt.world.isDaytime() && !SpecialDayTracker.instance.isWinterEnabled() && this.isPinkForest(evt.world, evt.xCoord, evt.zCoord) && evt.world.canBlockSeeTheSky(evt.xCoord, evt.yCoord+1, evt.zCoord)) {
			if (evt.block == Blocks.snow_layer)
				evt.world.setBlockToAir(evt.xCoord, evt.yCoord, evt.zCoord);
			else if (evt.block == Blocks.ice)
				evt.world.setBlock(evt.xCoord, evt.yCoord, evt.zCoord, Blocks.water);
		}
	}

	@SubscribeEvent
	public void preventNewIce(IceFreezeEvent evt) {
		if (this.isPinkForest(evt.world, evt.xCoord, evt.zCoord)) {
			evt.setResult(Result.DENY);
		}
	}

	@SubscribeEvent
	public void preventSnowGen(SnowOrIceOnGenEvent evt) {
		if (this.isPinkForest(evt.world, evt.xCoord, evt.zCoord)) {
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
		if (this.isPinkForest(evt.getBiome())) {
			evt.icon = evt.isTop ? biomeGrassIcon : biomeGrassIconSide;
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void retextureWater(LiquidBlockIconEvent evt) {
		if (this.isPinkForest(evt.getBiome())) {
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
			biomeGrassIcon = event.map.registerIcon("Satisforestry:terrain/grass_top");
			biomeGrassIconSide = event.map.registerIcon("Satisforestry:terrain/grass_side_overlay");
			biomeWaterIconFlow = event.map.registerIcon("Satisforestry:terrain/water/water_flow");
			biomeWaterIcon = event.map.registerIcon("Satisforestry:terrain/water/water_still");
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

	//@SubscribeEvent(priority = EventPriority.LOWEST)
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
		if (evt.type == EnumCreatureType.monster && isPinkForest(evt.world, evt.x, evt.z)) {
			if (BiomewideFeatureGenerator.instance.isInCave(evt.world, evt.x, evt.y, evt.z)) {
				evt.list.clear();
				evt.list.add(UraniumCave.instance.getRandomSpawn(evt.world.rand));
				//ReikaJavaLibrary.pConsole(evt.list.get(0).entityClass+" @ "+evt.world.getTotalWorldTime());
			}
		}
	}

	@SubscribeEvent
	public void brighterDarkness(LightVisualBrightnessEvent evt) {
		if (isPinkForest(evt.getBiome())) {
			;//evt.brightness = evt.getBrightnessFor(Math.max(1, evt.lightLevel));
		}
	}

	@SubscribeEvent
	public void brighterDarkness(LightMixedBrightnessEvent evt) {
		if (isPinkForest(evt.getBiome())) {
			;//evt.value = evt.getBrightnessFor(evt.blockLight, Math.max(1, evt.skyLight));
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
		else if (evt.block == log && evt.getMetadata()%4 == 1) {
			evt.setResult(Result.DENY);
		}
	}

	@SubscribeEvent
	public void noPumpkins(Decorate evt) {
		if (evt.type == Decorate.EventType.PUMPKIN && this.isPinkForest(evt.world, evt.chunkX+8, evt.chunkZ+8)) {
			evt.setResult(Result.DENY);
		}
	}

	@SubscribeEvent
	public void preventCliffBeaches(GenLayerBeachEvent evt) {
		if (this.isPinkForest(evt.originalBiomeID)) {
			evt.beachIDToPlace = BiomeGenBase.beach.biomeID;
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void updateRendering(ClientTickEvent evt) {
		BiomePinkForest.updateRenderFactor(Minecraft.getMinecraft().thePlayer);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void setBiomeHorizonColor(FogColors evt) {
		if (Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null)
			return;
		if (Minecraft.getMinecraft().thePlayer.isInsideOfMaterial(Material.lava) || Minecraft.getMinecraft().thePlayer.isInsideOfMaterial(Material.water))
			return;
		float f = BiomePinkForest.renderFactor*(float)ReikaMathLibrary.normalizeToBounds(ReikaWorldHelper.getSunIntensity(Minecraft.getMinecraft().theWorld, false, (float)evt.renderPartialTicks), 0, 1, 0.2, 0.8);
		if (f > 0) {
			int color = 0xFFABF6;
			evt.red = evt.red*(1-f)+f*ReikaColorAPI.getRed(color)/255F;
			evt.green = evt.green*(1-f)+f*ReikaColorAPI.getGreen(color)/255F;
			evt.blue = evt.blue*(1-f)+f*ReikaColorAPI.getBlue(color)/255F;
		}
	}

	public static boolean isPinkForest(World world, int x, int z) {
		return isPinkForest(world.getWorldChunkManager().getBiomeGenAt(x, z));
	}

	public static boolean isPinkForest(BiomeGenBase b) {
		return b instanceof BiomePinkForest;
	}

}
