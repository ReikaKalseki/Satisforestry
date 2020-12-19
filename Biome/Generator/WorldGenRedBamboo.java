package Reika.Satisforestry.Biome.Generator;

import java.util.Random;

import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.Satisforestry.SFBlocks;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Biome.BiomePinkForest.BiomeSection;
import Reika.Satisforestry.Biome.DecoratorPinkForest;

public class WorldGenRedBamboo extends WorldGenerator {

	private int generationRate = 128;

	public WorldGenRedBamboo() {

	}

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		for (int i = 0; i < generationRate; i++) {
			int r = MathHelper.ceiling_double_int(generationRate);
			int dx = ReikaRandomHelper.getRandomPlusMinus(x, r, rand);
			int dz = ReikaRandomHelper.getRandomPlusMinus(z, r, rand);

			if (!Satisforestry.isPinkForest(world, dx, dz))
				continue;

			int dy = DecoratorPinkForest.getTrueTopAt(world, dx, dz)+1;

			if (dy < 62)
				continue;

			if (this.isReplaceable(world, dx, dy, dz) && !Satisforestry.pinkforest.isRoad(world, dx, dz) && SFBlocks.BAMBOO.getBlockInstance().canBlockStay(world, dx, dy, dz)) {
				int h = ReikaRandomHelper.getRandomBetween(3, 7, rand); //TODO noisemap?
				for (int d = 0; d < h; d++) {
					if (this.isReplaceable(world, dx, dy+d, dz)) {
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

	private boolean isReplaceable(World world, int x, int y, int z) {
		return ReikaWorldHelper.softBlocks(world, x, y, z);// || world.getBlock(x, y, z).isLeaves(world, x, y, z);
	}

	public void setFrequency(BiomeSection sub) {
		switch(sub) {
			case FOREST:
				generationRate = 40;
				break;
			case STREAMS:
				generationRate = 320;
				break;
			case SWAMP:
				generationRate = 120;
				break;
		}
	}
}