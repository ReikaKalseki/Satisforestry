package Reika.Satisforestry.Biome.Generator;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.Instantiable.Data.WeightedRandom;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaPlantHelper;
import Reika.DragonAPI.Libraries.World.ReikaBlockHelper;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.Satisforestry.Blocks.BlockPinkGrass.GrassTypes;
import Reika.Satisforestry.Registry.SFBlocks;

public class RedJungleTreeGenerator extends PinkTreeGeneratorBase {

	public RedJungleTreeGenerator(boolean force) {
		super(force, PinkTreeTypes.JUNGLE);
	}

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		if (!ReikaPlantHelper.SAPLING.canPlantAt(world, x, y, z))
			return false;
		int h0 = ReikaRandomHelper.getRandomBetween(8, 12, rand);
		for (int d = 2; d < 6; d++) {
			ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[d];
			int i = 1;
			while (ReikaWorldHelper.softBlocks(world, x+dir.offsetX, y+i, z+dir.offsetZ)) {
				if (i >= -3)
					this.setBlockAndNotifyAdequately(world, x+dir.offsetX, y+i, z+dir.offsetZ, SFBlocks.LOG.getBlockInstance(), 2);
				else
					this.setBlockAndNotifyAdequately(world, x+dir.offsetX, y+i, z+dir.offsetZ, Blocks.dirt, 0);
				i--;
			}
		}
		for (int i = 0; i < h0; i++) {
			this.setBlockAndNotifyAdequately(world, x, y+i, z, SFBlocks.LOG.getBlockInstance(), 2);
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
							this.setBlockAndNotifyAdequately(world, c.xCoord, c.yCoord, c.zCoord, SFBlocks.LEAVES.getBlockInstance(), PinkTreeTypes.JUNGLE.ordinal());
							if (i != 0 || k != 0) {
								leaves.put(c, dd);
							}
						}
					}
				}
			}
		}
		this.generateVines(world, x, y, z, rand, h0, leaves);
		trunkBottom = y+2;
		trunkTop = y+h0;
		return true;
	}

	private void generateVines(World world, int x, int y, int z, Random rand, int h0, HashMap<Coordinate, Double> leaves) {
		WeightedRandom<Coordinate> wr = new WeightedRandom();
		wr.setRNG(rand);
		for (Entry<Coordinate, Double> e : leaves.entrySet()) {
			wr.addEntry(e.getKey(), e.getValue());
		}
		double f = 0.83;
		for (int i = -2; i <= h0-2; i++) {
			int d = i >= 2 ? 1 : 2;
			this.tryPlaceVine(world, rand, x+d, y+i, z, ForgeDirection.WEST);
			this.tryPlaceVine(world, rand, x-d, y+i, z, ForgeDirection.EAST);
			this.tryPlaceVine(world, rand, x, y+i, z+d, ForgeDirection.NORTH);
			this.tryPlaceVine(world, rand, x, y+i, z-d, ForgeDirection.SOUTH);
			if (i < 2) {
				this.tryPlaceVine(world, rand, x+1, y+i, z+1, ForgeDirection.WEST, ForgeDirection.NORTH);
				this.tryPlaceVine(world, rand, x+1, y+i, z-1, ForgeDirection.WEST, ForgeDirection.SOUTH);
				this.tryPlaceVine(world, rand, x-1, y+i, z+1, ForgeDirection.EAST, ForgeDirection.NORTH);
				this.tryPlaceVine(world, rand, x-1, y+i, z-1, ForgeDirection.EAST, ForgeDirection.SOUTH);
			}
		}
		int n = ReikaRandomHelper.getRandomBetween(15, 20, rand);
		for (int i = 0; i < n; i++) {
			Coordinate c = wr.getRandomEntry().offset(0, -1, 0);
			while (c.getBlock(world) == SFBlocks.LEAVES.getBlockInstance())
				c = c.offset(0, -1, 0);
			while (c.isEmpty(world)) {
				this.setBlockAndNotifyAdequately(world, c.xCoord, c.yCoord, c.zCoord, SFBlocks.GRASS.getBlockInstance(), GrassTypes.TREE_VINE.ordinal());
				c = c.offset(0, -1, 0);
			}
		}
	}

	private void tryPlaceVine(World world, Random rand, int x, int y, int z, ForgeDirection... sides) {
		if (rand.nextDouble() <= 0.9 && world.getBlock(x, y, z).isAir(world, x, y, z))
			this.setBlockAndNotifyAdequately(world, x, y, z, Blocks.vine, ReikaBlockHelper.getVineMetadatasFor(sides));
	}

	@Override
	protected int getDifficultyByHeight(int y, int dy, Random rand) {
		return 0;
	}

	@Override
	protected int getSlugByHeight(int y, int dy, Random rand) {
		return rand.nextInt(4) == 0 && this.getHeightFraction(y) >= 0.8+rand.nextDouble()*0.2 ? 1 : 0;
	}

	@Override
	protected float getTrunkSlugChancePerBlock() {
		return 0.005F;
	}

	@Override
	protected float getTreeTopSlugChance() {
		return 0.4F;
	}

	@Override
	protected boolean canSpawnLeaftopMobs() {
		return true;
	}

}
