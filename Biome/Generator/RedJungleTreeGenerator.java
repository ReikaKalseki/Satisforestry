package Reika.Satisforestry.Biome.Generator;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.Instantiable.Data.WeightedRandom;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaPlantHelper;
import Reika.DragonAPI.Libraries.World.ReikaBlockHelper;
import Reika.Satisforestry.Blocks.BlockPinkGrass.GrassTypes;
import Reika.Satisforestry.Blocks.BlockPinkLeaves.LeafTypes;
import Reika.Satisforestry.Registry.SFBlocks;

public class RedJungleTreeGenerator extends WorldGenAbstractTree {

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		if (!ReikaPlantHelper.SAPLING.canPlantAt(world, x, y, z))
			return false;
		int h0 = ReikaRandomHelper.getRandomBetween(8, 12, rand);
		for (int i = 0; i <= 1; i++) {
			world.setBlock(x+1, y+i, z, SFBlocks.LOG.getBlockInstance(), 2, 2);
			world.setBlock(x-1, y+i, z, SFBlocks.LOG.getBlockInstance(), 2, 2);
			world.setBlock(x, y+i, z+1, SFBlocks.LOG.getBlockInstance(), 2, 2);
			world.setBlock(x, y+i, z-1, SFBlocks.LOG.getBlockInstance(), 2, 2);
		}
		for (int i = 0; i < h0; i++) {
			world.setBlock(x, y+i, z, SFBlocks.LOG.getBlockInstance(), 2, 2);
		}
		int dy = y+h0;
		double[][] factors = {{1.125, 1.25}, {1, 1}, {0.8, 1}, {0.25, 0.5}};
		double[] rf = new double[factors.length];
		for (int i = 0; i < rf.length; i++) {
			rf[i] = ReikaRandomHelper.getRandomBetween(factors[i][0], factors[i][1], rand);
			if (i > 0) {
				rf[i] = MathHelper.clamp_double(rf[i], factors[i][0], rf[i-1]*0.9);
			}
		}
		double r0 = ReikaRandomHelper.getRandomBetween(4.75, 6.5, rand);

		int ri = MathHelper.ceiling_double_int(r0);
		HashMap<Coordinate, Double> leaves = new HashMap();
		for (int h = -1; h <= 2; h++) {
			double dr = r0*rf[h+1];
			for (int i = -ri; i <= ri; i++) {
				for (int k = -ri; k <= ri; k++) {
					double dd = i*i+k*k;
					double dr2 = ReikaRandomHelper.getRandomPlusMinus(dr, 0.25, rand);
					if (dd < dr2*dr2) {
						if (h == -1 && dd >= 4.8 && dd <= dr2*dr2*0.5)
							continue;
						if (i != 0 || k != 0 || h >= 0) {
							Coordinate c = new Coordinate(x+i, dy+h, z+k);
							c.setBlock(world, SFBlocks.LEAVES.getBlockInstance(), LeafTypes.JUNGLE.ordinal(), 2);
							if (i != 0 || k != 0) {
								leaves.put(c, dd);
							}
						}
					}
				}
			}
		}
		this.generateVines(world, x, y, z, rand, h0, leaves);
		return true;
	}

	private void generateVines(World world, int x, int y, int z, Random rand, int h0, HashMap<Coordinate, Double> leaves) {
		WeightedRandom<Coordinate> wr = new WeightedRandom();
		wr.setRNG(rand);
		for (Entry<Coordinate, Double> e : leaves.entrySet()) {
			wr.addEntry(e.getKey(), e.getValue());
		}
		double f = 0.83;
		for (int i = 0; i <= h0-2; i++) {
			int d = i >= 2 ? 1 : 2;
			if (rand.nextDouble() < f)
				world.setBlock(x+d, y+i, z, Blocks.vine, ReikaBlockHelper.getVineMetadatasFor(ForgeDirection.WEST), 2);
			if (rand.nextDouble() < f)
				world.setBlock(x-d, y+i, z, Blocks.vine, ReikaBlockHelper.getVineMetadatasFor(ForgeDirection.EAST), 2);
			if (rand.nextDouble() < f)
				world.setBlock(x, y+i, z+d, Blocks.vine, ReikaBlockHelper.getVineMetadatasFor(ForgeDirection.NORTH), 2);
			if (rand.nextDouble() < f)
				world.setBlock(x, y+i, z-d, Blocks.vine, ReikaBlockHelper.getVineMetadatasFor(ForgeDirection.SOUTH), 2);
			if (i < 2) {
				if (rand.nextDouble() < f)
					world.setBlock(x+1, y+i, z+1, Blocks.vine, ReikaBlockHelper.getVineMetadatasFor(ForgeDirection.WEST, ForgeDirection.NORTH), 2);
				if (rand.nextDouble() < f)
					world.setBlock(x+1, y+i, z-1, Blocks.vine, ReikaBlockHelper.getVineMetadatasFor(ForgeDirection.WEST, ForgeDirection.SOUTH), 2);
				if (rand.nextDouble() < f)
					world.setBlock(x-1, y+i, z+1, Blocks.vine, ReikaBlockHelper.getVineMetadatasFor(ForgeDirection.EAST, ForgeDirection.NORTH), 2);
				if (rand.nextDouble() < f)
					world.setBlock(x-1, y+i, z-1, Blocks.vine, ReikaBlockHelper.getVineMetadatasFor(ForgeDirection.EAST, ForgeDirection.SOUTH), 2);
			}
		}
		int n = ReikaRandomHelper.getRandomBetween(15, 20, rand);
		for (int i = 0; i < n; i++) {
			Coordinate c = wr.getRandomEntry().offset(0, -1, 0);
			while (c.getBlock(world) == SFBlocks.LEAVES.getBlockInstance())
				c = c.offset(0, -1, 0);
			while (c.isEmpty(world)) {
				c.setBlock(world, SFBlocks.GRASS.getBlockInstance(), GrassTypes.TREE_VINE.ordinal(), 2);
				c = c.offset(0, -1, 0);
			}
		}
	}

}
