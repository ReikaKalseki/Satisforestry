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
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemFood;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.BiomeManager.BiomeEntry;
import net.minecraftforge.common.BiomeManager.BiomeType;
import net.minecraftforge.common.MinecraftForge;

import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.DragonOptions;
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.Auxiliary.Trackers.CommandableUpdateChecker;
import Reika.DragonAPI.Base.DragonAPIMod;
import Reika.DragonAPI.Base.DragonAPIMod.LoadProfiler.LoadPhase;
import Reika.DragonAPI.Instantiable.IO.ControlledConfig;
import Reika.DragonAPI.Instantiable.IO.ModLogger;
import Reika.DragonAPI.Libraries.ReikaRegistryHelper;
import Reika.DragonAPI.Libraries.IO.ReikaPacketHelper;
import Reika.DragonAPI.ModInteract.DeepInteract.ReikaThaumHelper;
import Reika.Satisforestry.Biome.BiomePinkForest;
import Reika.Satisforestry.Blocks.BlockDecoration.DecorationType;
import Reika.Satisforestry.Blocks.BlockPinkLeaves;
import Reika.Satisforestry.Blocks.BlockPinkLog;
import Reika.Satisforestry.Blocks.BlockTerrain.TerrainType;
import Reika.Satisforestry.Config.BiomeConfig;
import Reika.Satisforestry.Entity.EntityEliteStinger;
import Reika.Satisforestry.Entity.EntityFlyingManta;
import Reika.Satisforestry.Entity.EntityLizardDoggo;
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
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import thaumcraft.api.aspects.Aspect;

@Mod( modid = "Satisforestry", name="Satisforestry", version = "v@MAJOR_VERSION@@MINOR_VERSION@", certificateFingerprint = "@GET_FINGERPRINT@", dependencies="required-after:DragonAPI;after:CritterPet")

public class Satisforestry extends DragonAPIMod {

	@Instance("Satisforestry")
	public static Satisforestry instance = new Satisforestry();

	public static final String packetChannel = "SFPacketData";

	public static final ControlledConfig config = new ControlledConfig(instance, SFOptions.optionList, null);

	public static BlockPinkLog log;
	public static BlockPinkLeaves leaves;

	public static BiomePinkForest pinkforest;
	//public static BiomePinkRiver pinkriver;

	public static ItemFood paleberry;

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
		MinecraftForge.EVENT_BUS.register(SFEvents.instance);
		FMLCommonHandler.instance().bus().register(SFEvents.instance);

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

		paleberry = new ItemPaleberry();
		paleberry.setUnlocalizedName("paleberry");
		GameRegistry.registerItem(paleberry, "paleberry");

		this.basicSetup(evt);
		FMLCommonHandler.instance().bus().register(this);
		this.finishTiming();
	}

	@Override
	@EventHandler
	public void load(FMLInitializationEvent event) {
		this.startTiming(LoadPhase.LOAD);
		proxy.registerRenderers();
		proxy.registerSounds();

		LanguageRegistry.addName(paleberry, "Paleberries");

		APIObjects.load();

		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			MinecraftForge.EVENT_BUS.register(ShaderActivation.instance);
		}

		pinkforest = new BiomePinkForest(SFOptions.BIOMEID.getValue());
		BiomeManager.addBiome(BiomeType.COOL, new BiomeEntry(pinkforest, 4));
		BiomeManager.addSpawnBiome(pinkforest);
		BiomeManager.addStrongholdBiome(pinkforest);
		//BiomeManager.addVillageBiome(pinkforest, true);
		BiomeManager.removeVillageBiome(pinkforest);
		BiomeDictionary.registerBiomeType(pinkforest, BiomeDictionary.Type.FOREST, BiomeDictionary.Type.MAGICAL, BiomeDictionary.Type.DENSE, BiomeDictionary.Type.LUSH, BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.WET);

		//pinkriver = new BiomePinkRiver();

		ReikaRegistryHelper.registerModEntities(instance, SFEntities.entityList);

		this.finishTiming();
	}

	@Override
	@EventHandler
	public void postload(FMLPostInitializationEvent evt) {
		this.startTiming(LoadPhase.POSTLOAD);

		BiomeConfig.instance.loadConfigs();

		if (ModList.THAUMCRAFT.isLoaded()) {
			ReikaThaumHelper.addAspectsToItem(paleberry, Aspect.HEAL, 4, Aspect.HUNGER, 1, Aspect.EXCHANGE, 1);

			ReikaThaumHelper.addAspects(EntityEliteStinger.class, Aspect.BEAST, 12, Aspect.POISON, 10, Aspect.HUNGER, 6, Aspect.FLIGHT, 2);
			ReikaThaumHelper.addAspects(EntityFlyingManta.class, Aspect.AIR, 6, Aspect.FLIGHT, 24, Aspect.TRAVEL, 18);
			ReikaThaumHelper.addAspects(EntityLizardDoggo.class, Aspect.BEAST, 1, Aspect.GREED, 1, Aspect.HARVEST, 2);

			ReikaThaumHelper.addAspectsToBlock(SFBlocks.BAMBOO.getBlockInstance(), Aspect.PLANT, 3, Aspect.TREE, 1);
			ReikaThaumHelper.addAspectsToBlock(SFBlocks.LOG.getBlockInstance(), Aspect.TREE, 6, Aspect.ARMOR, 4, Aspect.TRAVEL, 1);
			ReikaThaumHelper.addAspectsToBlock(SFBlocks.LEAVES.getBlockInstance(), Aspect.TREE, 4, Aspect.PLANT, 4, Aspect.SENSES, 2);
			ReikaThaumHelper.addAspectsToBlock(SFBlocks.GRASS.getBlockInstance(), Aspect.PLANT, 2, Aspect.LIFE, 1);

			ReikaThaumHelper.addAspectsToBlock(SFBlocks.CAVESHIELD.getBlockInstance(), Aspect.DARKNESS, 8, Aspect.ARMOR, 8, Aspect.VOID, 4, Aspect.EARTH, 6, Aspect.MINE, 3);
			ReikaThaumHelper.addAspectsToBlock(SFBlocks.GASEMITTER.getBlockInstance(), Aspect.POISON, 8, Aspect.TRAP, 6, Aspect.AURA, 4);
			ReikaThaumHelper.addAspectsToBlock(SFBlocks.RESOURCENODE.getBlockInstance(), Aspect.GREED, 12, Aspect.MINE, 8, Aspect.HARVEST, 8);
			ReikaThaumHelper.addAspectsToBlock(SFBlocks.SPAWNER.getBlockInstance(), Aspect.DARKNESS, 8, Aspect.ARMOR, 8, Aspect.BEAST, 12);

			ReikaThaumHelper.addAspectsToBlockMeta(SFBlocks.TERRAIN.getBlockInstance(), TerrainType.POISONROCK.ordinal(), Aspect.EARTH, 3, Aspect.POISON, 1);
			ReikaThaumHelper.addAspectsToBlockMeta(SFBlocks.TERRAIN.getBlockInstance(), TerrainType.PONDROCK.ordinal(), Aspect.EARTH, 3, Aspect.WATER, 2);
			ReikaThaumHelper.addAspectsToBlockMeta(SFBlocks.DECORATION.getBlockInstance(), DecorationType.TENDRILS.ordinal(), Aspect.EARTH, 4, Aspect.TRAP, 2);
			ReikaThaumHelper.addAspectsToBlockMeta(SFBlocks.DECORATION.getBlockInstance(), DecorationType.STALACTITE.ordinal(), Aspect.EARTH, 3, Aspect.DARKNESS, 1);
			ReikaThaumHelper.addAspectsToBlockMeta(SFBlocks.DECORATION.getBlockInstance(), DecorationType.STALAGMITE.ordinal(), Aspect.EARTH, 3, Aspect.DARKNESS, 1);
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
