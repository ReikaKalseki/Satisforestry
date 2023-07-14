package Reika.Satisforestry.Biome.Generator;

import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

import Reika.DragonAPI.Libraries.Registry.ReikaPlantHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Biome.BiomePinkForest.BiomeSection;


public class PinkTreeChooser extends WorldGenAbstractTree {

	public PinkTreeChooser() {
		super(false);
	}

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		if (!ReikaPlantHelper.SAPLING.canPlantAt(world, x, y, z))
			return false;
		if (Satisforestry.pinkforest.isRoad(world, x, z))
			return false;
		BiomeSection b = Satisforestry.pinkforest.getSubBiome(world, x, z);
		double treeRate = b.treeFrequency();
		if (rand.nextDouble() > treeRate)
			return false;
		PinkTreeGeneratorBase generator = this.getGenerator(world, x, y, z, rand, b);
		if (generator.generate(world, rand, x, y, z)) {
			generator.postGenerate(world, rand, x, y, z);
			return true;
		}
		return false;
	}

	private PinkTreeGeneratorBase getGenerator(World world, int x, int y, int z, Random rand, BiomeSection b) {
		if (rand.nextDouble() <= b.largeTreeFraction())
			return new GiantPinkTreeGenerator(false);
		return rand.nextDouble() <= b.jungleTreeRate() ? new RedJungleTreeGenerator(false) : new PinkTreeGenerator(false);
	}

}
