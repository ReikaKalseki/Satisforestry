package Reika.Satisforestry.Biome.Generator;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import Reika.DragonAPI.Instantiable.Interpolation;
import Reika.DragonAPI.Instantiable.Data.WeightedRandom;
import Reika.DragonAPI.Instantiable.Data.WeightedRandom.DynamicWeight;
import Reika.DragonAPI.Instantiable.Data.Immutable.BlockKey;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Instantiable.Math.Noise.SimplexNoiseGenerator;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.Satisforestry.SFBlocks;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Blocks.BlockPinkGrass.GrassTypes;

public class WorldGenPinkGrass extends WorldGenerator {

	private static final GrassType BASE = new GrassType(Blocks.tallgrass, 1, new Interpolation(false).addPoint(60, 50).addPoint(90, 25).addPoint(110, 5).addPoint(120, 0));

	private final WeightedRandom<GrassType> grassTypes = new WeightedRandom();

	private SimplexNoiseGenerator noise;

	public WorldGenPinkGrass() {
		grassTypes.addDynamicEntry(BASE);
		grassTypes.addDynamicEntry(new GrassType(Blocks.tallgrass, 2, new Interpolation(false).addPoint(80, 0).addPoint(100, 20).addPoint(128, 5)));
		Block b = SFBlocks.GRASS.getBlockInstance();
		grassTypes.addDynamicEntry(new GrassType(b, GrassTypes.FERN.ordinal(), new Interpolation(false).addPoint(60, 40).addPoint(100, 15).addPoint(120, 5)));
		grassTypes.addDynamicEntry(new GrassType(b, GrassTypes.PEACH_FRINGE.ordinal(), new Interpolation(false).addPoint(60, 0).addPoint(80, 10).addPoint(100, 30).addPoint(128, 10)));
		grassTypes.addDynamicEntry(new GrassType(b, GrassTypes.RED_STRANDS.ordinal(), new Interpolation(false).addPoint(55, 0).addPoint(64, 20).addPoint(96, 40).addPoint(144, 25)));
		grassTypes.addDynamicEntry(new GrassType(b, GrassTypes.TINY_PINK_LUMPS.ordinal(), new Interpolation(false).addPoint(72, 0).addPoint(80, 10).addPoint(110, 30).addPoint(144, 50)));
	}

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		this.initNoise(world);
		do {
			Block at = world.getBlock(x, y, z);
			if (!(at.isLeaves(world, x, y, z) || at.isAir(world, x, y, z))) {
				break;
			}
			y--;
		} while (y > 0);

		for (int i = 0; i < 128; i++) {
			int dx = x + rand.nextInt(8) - rand.nextInt(8);
			int dy = y + rand.nextInt(4) - rand.nextInt(4);
			int dz = z + rand.nextInt(8) - rand.nextInt(8);

			if (Satisforestry.pinkforest.isRoad(world, dx, dz) && rand.nextBoolean())
				continue;

			BlockKey place = this.getBlockToPlace(world, dx, dy, dz, rand);
			boolean paleberry = dy >= 105 && world.isAirBlock(dx, dy+1, dz) && rand.nextInt(2000) < this.paleberryChance(world, dx, dy, dz);
			if (paleberry) {
				place = new BlockKey(SFBlocks.GRASS.getBlockInstance(), GrassTypes.PALEBERRY_STALK.ordinal());
			}

			if (world.isAirBlock(dx, dy, dz) && place.blockID.canBlockStay(world, dx, dy, dz)) {
				world.setBlock(dx, dy, dz, place.blockID, place.metadata, 2);
				if (paleberry) {
					world.setBlock(dx, dy+1, dz, place.blockID, GrassTypes.PALEBERRY_NEW.ordinal(), 2);
				}
			}
		}

		return true;
	}

	private void initNoise(World world) {
		if (noise == null || noise.seed != world.getSeed()) {
			noise = (SimplexNoiseGenerator)new SimplexNoiseGenerator(-world.getSeed()).setFrequency(1/3.5D);
		}
	}

	private int paleberryChance(World world, int x, int y, int z) { //old 1/200 = 10, >= 2000 for a guarantee
		double val = noise.getValue(x, z);
		return val >= 0.925 ? 5000 : (int)ReikaMathLibrary.normalizeToBounds(val, 5, 25);
	}

	private BlockKey getBlockToPlace(World world, int dx, int dy, int dz, Random rand) {
		if (dy < 62 || !Satisforestry.isPinkForest(world, dx, dz))
			return BASE.block;
		grassTypes.setRNG(rand);
		for (GrassType gr : grassTypes.getValues()) {
			gr.calcWeight(world, dx, dy, dz);
		}
		GrassType type =  grassTypes.getRandomEntry();
		if (type == null) {
			Satisforestry.logger.logError("Null grass type calculated @ "+new Coordinate(dx, dy, dz)+"="+grassTypes.getTotalWeight());
			type = BASE;
		}
		return type.block;
	}

	private static class GrassType implements DynamicWeight {

		private final BlockKey block;
		private final Interpolation weightCurve;

		private double weight = 1;

		private GrassType(Block b, int meta, Interpolation curve) {
			block = new BlockKey(b, meta);
			weightCurve = curve;
			weight = weightCurve.getInitialValue();
		}

		public void calcWeight(World world, int x, int y, int z) {
			weight = weightCurve.getValue(y);
		}

		@Override
		public double getWeight() {
			return weight;
		}

	}
}