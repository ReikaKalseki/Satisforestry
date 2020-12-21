package Reika.Satisforestry.Biome.Generator;

import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Biome.BiomePinkForest;
import Reika.Satisforestry.Biome.DecoratorPinkForest;
import Reika.Satisforestry.Biome.DecoratorPinkForest.OreClusterType;
import Reika.Satisforestry.Biome.DecoratorPinkForest.OreSpawnLocation;

public class WorldGenOreCluster extends WorldGenerator {

	public WorldGenOreCluster() {

	}

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		int r = 8;
		int r2 = 16;
		boolean flag = false;
		for (int i = 0; i < 18; i++) {
			int dx = ReikaRandomHelper.getRandomPlusMinus(x, r, rand);
			int dz = ReikaRandomHelper.getRandomPlusMinus(z, r, rand);

			if (!Satisforestry.isPinkForest(world, dx, dz))
				continue;

			int edge = BiomePinkForest.getNearestBiomeEdge(world, dx, dz, r2);
			if (edge < 0)
				continue;
			float f = 1F-edge/(float)r2;

			if (rand.nextFloat() > f)
				continue;

			int dy = DecoratorPinkForest.getTrueTopAt(world, dx, dz)+1;

			if (this.isReplaceable(world, dx, dy, dz) && !Satisforestry.pinkforest.isRoad(world, dx, dz)) {
				int size = 2+(int)(f*rand.nextFloat()*1.5F);
				OreClusterType ore = DecoratorPinkForest.generateOreClumpAt(world, dx, dy, dz, rand, OreSpawnLocation.BORDER, size);
				flag |= ore != null;
			}
		}

		return flag;
	}

	private boolean isReplaceable(World world, int x, int y, int z) {
		return ReikaWorldHelper.softBlocks(world, x, y, z);// || world.getBlock(x, y, z).isLeaves(world, x, y, z);
	}
}