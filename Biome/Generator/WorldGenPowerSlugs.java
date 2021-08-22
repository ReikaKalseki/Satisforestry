package Reika.Satisforestry.Biome.Generator;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Data.ShuffledGrid;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Libraries.World.ReikaChunkHelper;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Biome.DecoratorPinkForest;
import Reika.Satisforestry.Blocks.BlockPowerSlug;
import Reika.Satisforestry.Blocks.BlockPowerSlug.TilePowerSlug;
import Reika.Satisforestry.Registry.SFBlocks;

public class WorldGenPowerSlugs {

	private final ShuffledGrid[] noise = new ShuffledGrid[3];
	private long seed;

	public WorldGenPowerSlugs() {
		noise[0] = new ShuffledGrid(40, 4, 6, true);
		noise[1] = new ShuffledGrid(40, 6, 9, true);
		noise[2] = new ShuffledGrid(40, 8, 12, true);
	}

	public int generate(World world, Random rand, int chunkX, int chunkZ) {
		this.initNoise(world);
		int flags = 0;
		for (int i = 0; i < 3; i++) {
			if (!noise[i].isValid(chunkX >> 4, chunkZ >> 4))
				continue;
			ArrayList<Coordinate> li = ReikaChunkHelper.getChunkCoords(chunkX, chunkZ);
			while (!li.isEmpty()) {
				Coordinate c = li.remove(rand.nextInt(li.size()));
				if (!Satisforestry.isPinkForest(world, c.xCoord, c.zCoord))
					continue;
				int dy = DecoratorPinkForest.getTrueTopAt(world, c.xCoord, c.zCoord)+1;
				if (dy < 80)
					continue;
				if (this.isReplaceable(world, c.xCoord, dy, c.zCoord) && SFBlocks.SLUG.getBlockInstance().canBlockStay(world, c.zCoord, dy, c.zCoord)) {
					TilePowerSlug te = BlockPowerSlug.generatePowerSlugAt(world, c.xCoord, dy, c.zCoord, rand, i, false, dy > 130 ? 1 : 0, true);
					if (te != null) {
						flags |= (1 << i);
						break;
					}
				}
			}
			if (li.isEmpty()) {

			}
		}

		return flags;
	}

	private void initNoise(World world) {
		if (noise[0] == null || seed != world.getSeed()) {
			seed = world.getSeed();
			noise[0].calculate(seed);
			noise[1].calculate(seed);
			noise[2].calculate(seed);
		}
		/*
		if (noise[0] == null || noise[0].seed != world.getSeed()) {
			noise[0] = new VoronoiNoiseGenerator(world.getSeed());
			noise[1] = new VoronoiNoiseGenerator(-2*world.getSeed()/3);
			noise[2] = new VoronoiNoiseGenerator(~world.getSeed()+622381);
			noise[0].setFrequency(1/150D);
			noise[1].setFrequency(1/250D);
			noise[2].setFrequency(1/400D);
			noise[0].randomFactor = 0.35;
			noise[1].randomFactor = 0.55;
			noise[2].randomFactor = 0.75;
		}*/

	}

	private boolean isReplaceable(World world, int x, int y, int z) {
		return ReikaWorldHelper.softBlocks(world, x, y, z);// || world.getBlock(x, y, z).isLeaves(world, x, y, z);
	}

}
