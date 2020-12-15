package Reika.Satisforestry.Biome.Generator;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.Satisforestry.SFBlocks;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Biome.BiomePinkForest.BiomeSection;

public class WorldGenRedBamboo extends WorldGenerator {

	private int generationRate = 128;

	public WorldGenRedBamboo() {

	}

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		do {
			Block at = world.getBlock(x, y, z);
			if (!(at.isLeaves(world, x, y, z) || at.isAir(world, x, y, z))) {
				break;
			}
			y--;
		} while (y > 0);

		for (int i = 0; i < generationRate; i++) {
			int dx = x + rand.nextInt(8) - rand.nextInt(8);
			int dy = y + rand.nextInt(4) - rand.nextInt(4);
			int dz = z + rand.nextInt(8) - rand.nextInt(8);

			if (dy < 62 || !Satisforestry.isPinkForest(world, dx, dz))
				continue;

			if (world.isAirBlock(dx, dy, dz) && !Satisforestry.pinkforest.isRoad(world, dx, dz) && SFBlocks.BAMBOO.getBlockInstance().canBlockStay(world, dx, dy, dz)) {
				int h = ReikaRandomHelper.getRandomBetween(3, 7, rand); //TODO noisemap?
				for (int d = 0; d < h; d++) {
					if (world.isAirBlock(dx, dy+d, dz)) {
						world.setBlock(dx, dy+d, dz, SFBlocks.BAMBOO.getBlockInstance());
					}
					else {
						break;
					}
				}
			}
		}

		return true;
	}

	public void setFrequency(BiomeSection sub) {
		switch(sub) {
			case FOREST:
				generationRate = 32;
				break;
			case STREAMS:
				generationRate = 240;
				break;
			case SWAMP:
				generationRate = 72;
				break;
		}
	}
}