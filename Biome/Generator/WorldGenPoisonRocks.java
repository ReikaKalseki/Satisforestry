package Reika.Satisforestry.Biome.Generator;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.Satisforestry.SFBlocks;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Biome.DecoratorPinkForest;
import Reika.Satisforestry.Blocks.BlockGasEmitter.TileGasVent;

public class WorldGenPoisonRocks extends WorldGenerator {

	private final boolean forceGen;

	public WorldGenPoisonRocks(boolean force) {
		forceGen = force;
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

		boolean flag = false;

		for (int i = 0; i < 5; i++) {
			int dx = ReikaRandomHelper.getRandomPlusMinus(x, 5, rand);
			int dz = ReikaRandomHelper.getRandomPlusMinus(z, 5, rand);
			int dy = DecoratorPinkForest.getTrueTopAt(world, dx, dz)+1;

			if (!forceGen) {
				if (dy < 62 || !Satisforestry.isPinkForest(world, dx, dz))
					continue;
			}

			if (forceGen || (this.isReplaceable(world, dx, dy, dz) && !Satisforestry.pinkforest.isRoad(world, dx, dz))) {
				flag |= this.tryPlace(world, dx, dy, dz, rand);
			}
		}

		return flag;
	}

	private boolean tryPlace(World world, int x, int y, int z, Random rand) {
		Block b = world.getBlock(x, y-1, z);
		if (b != Blocks.grass && b != Blocks.dirt && b != Blocks.sand && b != Blocks.gravel && b != Blocks.clay)
			return false;
		int h = ReikaRandomHelper.getRandomBetween(2, 6, rand);
		boolean[] bulge = new boolean[h];
		bulge[0] = true;
		for (int i = 0; i < (h == 6 ? 3 : 2); i++) {
			bulge[rand.nextInt(bulge.length)] = true;
			bulge[rand.nextInt(bulge.length)] = true;
		}
		bulge[bulge.length-1] = false;
		for (int i = 0; i < h; i++) {
			if (!this.isReplaceable(world, x, y+i, z)) {
				return false;
			}
			if (bulge[i]) {
				if (!this.isReplaceable(world, x-1, y+i, z) || !this.isReplaceable(world, x+1, y+i, z) || !this.isReplaceable(world, x, y+i, z-1) || !this.isReplaceable(world, x, y+i, z+1)) {
					return false;
				}
			}
		}
		for (int i = 0; i < h; i++) {
			world.setBlock(x, y+i, z, Blocks.stone);
			if (bulge[i]) {
				for (int d = 2; d < 6; d++) {
					ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[d];
					int dx = x+dir.offsetX;
					int dz = z+dir.offsetZ;
					world.setBlock(dx, y+i, dz, Blocks.stone);
					if (i == 0) {
						while (ReikaWorldHelper.softBlocks(world, x, y+i-1, z)) {
							y--;
							world.setBlock(dx, y+i, dz, Blocks.stone);
						}
					}
				}
			}
		}
		int dy = y+h-1;
		world.setBlock(x, dy, z, SFBlocks.GASEMITTER.getBlockInstance(), 1, 3);
		TileGasVent te = (TileGasVent)world.getTileEntity(x, dy, z);
		te.activeRadius = 4;
		te.yOffset = 1-h;
		return true;
	}

	private boolean isReplaceable(World world, int x, int y, int z) {
		return ReikaWorldHelper.softBlocks(world, x, y, z) || world.getBlock(x, y, z) == SFBlocks.BAMBOO.getBlockInstance() || !world.getBlock(x, y, z).getMaterial().blocksMovement();
	}
}