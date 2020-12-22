package Reika.Satisforestry.Biome.Generator;

import java.util.Random;

import net.minecraft.world.World;

import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.Satisforestry.SFBlocks;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Biome.BiomePinkForest.BiomeSection;
import Reika.Satisforestry.Biome.DecoratorPinkForest;

public class WorldGenRedBamboo {

	private float generationRate = 0.2F;

	/** Block coords */
	public boolean generate(World world, Random rand, int chunkX, int chunkZ) {
		int amt = 0;
		for (int dx = chunkX; dx < chunkX+16; dx++) {
			for (int dz = chunkZ; dz < chunkZ+16; dz++) {
				if (!Satisforestry.isPinkForest(world, dx, dz))
					continue;
				this.setFrequency(Satisforestry.pinkforest.getSubBiome(world, dx, dz));
				if (rand.nextFloat() > generationRate)
					continue;

				int dy = DecoratorPinkForest.getTrueTopAt(world, dx, dz)+1;
				if (dy < 62)
					continue;

				if (this.isReplaceable(world, dx, dy, dz) && !Satisforestry.pinkforest.isRoad(world, dx, dz) && SFBlocks.BAMBOO.getBlockInstance().canBlockStay(world, dx, dy, dz)) {
					int h = ReikaRandomHelper.getRandomBetween(3, 7, rand); //TODO noisemap?
					for (int d = 0; d < h; d++) {
						if (this.isReplaceable(world, dx, dy+d, dz)) {
							world.setBlock(dx, dy+d, dz, SFBlocks.BAMBOO.getBlockInstance(), 15, 2);
						}
						else {
							break;
						}
					}
				}
			}
		}

		return amt > 0;
	}

	private boolean isReplaceable(World world, int x, int y, int z) {
		return ReikaWorldHelper.softBlocks(world, x, y, z);// || world.getBlock(x, y, z).isLeaves(world, x, y, z);
	}

	public void setFrequency(BiomeSection sub) {
		switch(sub) {
			case FOREST:
				generationRate = 0.1F;
				break;
			case STREAMS:
				generationRate = 0.67F;
				break;
			case SWAMP:
				generationRate = 0.2F;
				break;
		}
	}
}