package Reika.Satisforestry.Biome.Generator;

import java.util.Random;

import net.minecraft.world.World;

import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.Satisforestry.Biome.DecoratorPinkForest;
import Reika.Satisforestry.Biome.Biomewide.UraniumCave;
import Reika.Satisforestry.Blocks.BlockPowerSlug;

public class WorldGenCaveFlora {

	public WorldGenCaveFlora() {

	}

	/** Block coords */
	public boolean generate(World world, Random rand, int x, int z) {
		boolean flag = false;
		for (int i = 0; i < 64; i++) {
			int dx = x + rand.nextInt(8) - rand.nextInt(8);
			int dz = z + rand.nextInt(8) - rand.nextInt(8);
			int top = DecoratorPinkForest.getTrueTopAt(world, dx, dz)-4;
			if (top <= 6)
				continue;
			int dy = ReikaRandomHelper.getRandomBetween(6, top, rand);

			if (rand.nextBoolean()) {
				while (world.getBlock(dx, dy+1, dz).isAir(world, dx, dy+1, dz)) {
					dy++;
				}
				if (!world.getBlock(dx, dy, dz).isAir(world, dx, dy, dz) || !world.getBlock(dx, dy-1, dz).isAir(world, dx, dy-1, dz))
					continue;

				if (!BlockPowerSlug.canExistOn(world, dx, dy+1, dz))
					continue;

				UraniumCave.instance.generateVine(world, dx, dy, dz, rand);
				flag = true;
			}
			else {
				while (world.getBlock(dx, dy-1, dz).isAir(world, dx, dy-1, dz)) {
					dy--;
				}

				if (!world.getBlock(dx, dy, dz).isAir(world, dx, dy, dz) || !world.getBlock(dx, dy+1, dz).isAir(world, dx, dy+1, dz))
					continue;

				if (!BlockPowerSlug.canExistOn(world, dx, dy-1, dz))
					continue;

				if (rand.nextInt(4) == 0)
					UraniumCave.instance.generateMushroom(world, dx, dy, dz, rand);
				else
					UraniumCave.instance.generateStalks(world, dx, dy, dz, rand);
				flag = true;
			}

		}

		return flag;
	}
}