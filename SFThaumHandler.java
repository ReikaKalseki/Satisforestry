package Reika.Satisforestry;

import net.minecraft.util.ResourceLocation;

import Reika.DragonAPI.ModInteract.DeepInteract.ReikaThaumHelper;
import Reika.Satisforestry.Biome.Generator.PinkTreeGeneratorBase.PinkTreeTypes;
import Reika.Satisforestry.Blocks.BlockDecoration.DecorationType;
import Reika.Satisforestry.Blocks.BlockMinerMulti.MinerBlocks;
import Reika.Satisforestry.Blocks.BlockPinkGrass.GrassTypes;
import Reika.Satisforestry.Blocks.BlockTerrain.TerrainType;
import Reika.Satisforestry.Entity.EntityEliteStinger;
import Reika.Satisforestry.Entity.EntityFlyingManta;
import Reika.Satisforestry.Entity.EntityLizardDoggo;
import Reika.Satisforestry.Entity.EntitySpitter;
import Reika.Satisforestry.Registry.SFBlocks;

import thaumcraft.api.aspects.Aspect;

public class SFThaumHandler {

	public static final Aspect FICSIT = new Aspect("praestantia", 0xFA9549, new Aspect[]{Aspect.ELDRITCH, Aspect.MECHANISM}, new ResourceLocation("satisforestry", "textures/aspects/ficsit.png"), 1);

	public static void load() {
		ReikaThaumHelper.addAspectsToItem(Satisforestry.paleberry, Aspect.HEAL, 4, Aspect.HUNGER, 1, Aspect.EXCHANGE, 1);

		ReikaThaumHelper.addAspects(EntityEliteStinger.class, Aspect.BEAST, 12, Aspect.POISON, 10, Aspect.HUNGER, 6, Aspect.FLIGHT, 2);
		ReikaThaumHelper.addAspects(EntityFlyingManta.class, Aspect.AIR, 6, Aspect.FLIGHT, 24, Aspect.TRAVEL, 18);
		ReikaThaumHelper.addAspects(EntityLizardDoggo.class, Aspect.BEAST, 1, Aspect.GREED, 1, Aspect.HARVEST, 2, FICSIT, 2);
		ReikaThaumHelper.addAspects(EntitySpitter.class, Aspect.BEAST, 8, Aspect.FIRE, 12, Aspect.MIND, 1, Aspect.ELDRITCH, 3);

		ReikaThaumHelper.addAspectsToBlock(SFBlocks.BAMBOO.getBlockInstance(), Aspect.PLANT, 3);

		ReikaThaumHelper.addAspectsToBlockMeta(SFBlocks.GRASS.getBlockInstance(), GrassTypes.FERN.ordinal(), Aspect.PLANT, 2);
		ReikaThaumHelper.addAspectsToBlockMeta(SFBlocks.GRASS.getBlockInstance(), GrassTypes.PEACH_FRINGE.ordinal(), Aspect.PLANT, 2);
		ReikaThaumHelper.addAspectsToBlockMeta(SFBlocks.GRASS.getBlockInstance(), GrassTypes.TINY_PINK_LUMPS.ordinal(), Aspect.PLANT, 2);
		ReikaThaumHelper.addAspectsToBlockMeta(SFBlocks.GRASS.getBlockInstance(), GrassTypes.RED_STRANDS.ordinal(), Aspect.PLANT, 2);
		ReikaThaumHelper.addAspectsToBlockMeta(SFBlocks.GRASS.getBlockInstance(), GrassTypes.VINE.ordinal(), Aspect.PLANT, 2);
		ReikaThaumHelper.addAspectsToBlockMeta(SFBlocks.GRASS.getBlockInstance(), GrassTypes.TREE_VINE.ordinal(), Aspect.PLANT, 2);

		ReikaThaumHelper.addAspectsToBlockMeta(SFBlocks.GRASS.getBlockInstance(), GrassTypes.BLUE_MUSHROOM_STALK.ordinal(), Aspect.DARKNESS, 2, Aspect.LIGHT, 2);
		ReikaThaumHelper.addAspectsToBlockMeta(SFBlocks.GRASS.getBlockInstance(), GrassTypes.BLUE_MUSHROOM_TOP.ordinal(), Aspect.DARKNESS, 2, Aspect.LIGHT, 2);

		ReikaThaumHelper.addAspectsToBlockMeta(SFBlocks.GRASS.getBlockInstance(), GrassTypes.PALEBERRY_NEW.ordinal(), Aspect.PLANT, 2, Aspect.HEAL, 2);

		for (PinkTreeTypes type : PinkTreeTypes.list) {
			int f = type == PinkTreeTypes.GIANTTREE ? 2 : 1;
			ReikaThaumHelper.addAspects(type.getBaseLog(), Aspect.TREE, 5*f, Aspect.ARMOR, 2*f, Aspect.TRAVEL, 1);
			ReikaThaumHelper.addAspects(type.getSapling(), Aspect.TREE, f);
			ReikaThaumHelper.addAspects(type.getBaseLeaf(), Aspect.PLANT, 3*f, Aspect.GREED, f);
		}

		ReikaThaumHelper.addAspects(SFBlocks.SLUG.getStackOfMetadata(0), Aspect.ENERGY, 6, Aspect.GREED, 3, Aspect.MECHANISM, 5, FICSIT, 1);
		ReikaThaumHelper.addAspects(SFBlocks.SLUG.getStackOfMetadata(1), Aspect.ENERGY, 18, Aspect.GREED, 8, Aspect.MECHANISM, 5, FICSIT, 2);
		ReikaThaumHelper.addAspects(SFBlocks.SLUG.getStackOfMetadata(2), Aspect.ENERGY, 24, Aspect.GREED, 15, Aspect.MECHANISM, 5, FICSIT, 5);

		ReikaThaumHelper.addAspectsToBlock(SFBlocks.CAVESHIELD.getBlockInstance(), Aspect.DARKNESS, 8, Aspect.ARMOR, 8, Aspect.VOID, 4, Aspect.EARTH, 6, Aspect.MINE, 3);
		ReikaThaumHelper.addAspectsToBlock(SFBlocks.GASEMITTER.getBlockInstance(), Aspect.POISON, 8, Aspect.TRAP, 6, Aspect.AURA, 4);
		ReikaThaumHelper.addAspectsToBlock(SFBlocks.RESOURCENODE.getBlockInstance(), Aspect.GREED, 20, Aspect.MINE, 15, Aspect.HARVEST, 8, FICSIT, 2);
		ReikaThaumHelper.addAspectsToBlock(SFBlocks.SPAWNER.getBlockInstance(), Aspect.DARKNESS, 8, Aspect.ARMOR, 8, Aspect.BEAST, 12);

		ReikaThaumHelper.addAspectsToBlockMeta(SFBlocks.MINERMULTI.getBlockInstance(), MinerBlocks.ORANGE.ordinal(), Aspect.MECHANISM, 2, Aspect.METAL, 3, FICSIT, 10);
		ReikaThaumHelper.addAspectsToBlockMeta(SFBlocks.MINERMULTI.getBlockInstance(), MinerBlocks.DARK.ordinal(), Aspect.MECHANISM, 2, Aspect.METAL, 3, FICSIT, 10);
		ReikaThaumHelper.addAspectsToBlockMeta(SFBlocks.MINERMULTI.getBlockInstance(), MinerBlocks.SILVER.ordinal(), Aspect.MECHANISM, 2, Aspect.METAL, 3, FICSIT, 10);
		ReikaThaumHelper.addAspectsToBlockMeta(SFBlocks.MINERMULTI.getBlockInstance(), MinerBlocks.GRAY.ordinal(), Aspect.MECHANISM, 3, Aspect.METAL, 3, Aspect.MINE, 2, FICSIT, 10);
		ReikaThaumHelper.addAspectsToBlockMeta(SFBlocks.MINERMULTI.getBlockInstance(), MinerBlocks.DRILL.ordinal(), Aspect.MECHANISM, 5, Aspect.METAL, 3, Aspect.MINE, 5, FICSIT, 10);
		ReikaThaumHelper.addAspectsToBlockMeta(SFBlocks.MINERMULTI.getBlockInstance(), MinerBlocks.CONVEYOR.ordinal(), Aspect.MECHANISM, 5, Aspect.METAL, 3, FICSIT, 10, Aspect.TRAVEL, 5);
		ReikaThaumHelper.addAspectsToBlockMeta(SFBlocks.MINERMULTI.getBlockInstance(), MinerBlocks.HUB.ordinal(), Aspect.MECHANISM, 5, Aspect.METAL, 3, Aspect.MINE, 5, FICSIT, 10, Aspect.MOTION, 5);
		ReikaThaumHelper.addAspectsToBlockMeta(SFBlocks.MINERMULTI.getBlockInstance(), MinerBlocks.POWER.ordinal(), Aspect.MECHANISM, 5, Aspect.METAL, 3, Aspect.MINE, 5, FICSIT, 10, Aspect.ENERGY, 5);

		ReikaThaumHelper.addAspectsToBlockMeta(SFBlocks.TERRAIN.getBlockInstance(), TerrainType.POISONROCK.ordinal(), Aspect.EARTH, 3, Aspect.POISON, 1);
		ReikaThaumHelper.addAspectsToBlockMeta(SFBlocks.TERRAIN.getBlockInstance(), TerrainType.PONDROCK.ordinal(), Aspect.EARTH, 3, Aspect.WATER, 2);
		ReikaThaumHelper.addAspectsToBlockMeta(SFBlocks.DECORATION.getBlockInstance(), DecorationType.TENDRILS.ordinal(), Aspect.EARTH, 4, Aspect.TRAP, 2);
		ReikaThaumHelper.addAspectsToBlockMeta(SFBlocks.DECORATION.getBlockInstance(), DecorationType.STALACTITE.ordinal(), Aspect.EARTH, 3, Aspect.DARKNESS, 1);
		ReikaThaumHelper.addAspectsToBlockMeta(SFBlocks.DECORATION.getBlockInstance(), DecorationType.STALAGMITE.ordinal(), Aspect.EARTH, 3, Aspect.DARKNESS, 1);
	}

}
