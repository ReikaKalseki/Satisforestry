package Reika.Satisforestry.Biome.Generator;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Biome.BiomePinkForest.BiomeSection;
import Reika.Satisforestry.Biome.DecoratorPinkForest;
import Reika.Satisforestry.Blocks.BlockGasEmitter.TileGasVent;
import Reika.Satisforestry.Blocks.BlockTerrain.TerrainType;
import Reika.Satisforestry.Registry.SFBlocks;

public class WorldGenPoisonRocks extends WorldGenerator {

	private final boolean forceGen;

	private int generationRate = 5;

	public WorldGenPoisonRocks(boolean force) {
		forceGen = force;
	}

	@Override
	public boolean generate(World world, Random rand, int x, int yUnused, int z) {
		boolean flag = false;

		for (int i = 0; i < generationRate; i++) {
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
		for (int d = 2; d < 6; d++) {
			ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[d];
			int dx = x+dir.offsetX;
			int dz = z+dir.offsetZ;
			int dy = y;
			while (this.isReplaceable(world, dx, dy-1, dz)) {
				dy--;
				this.placeBlock(world, dx, dy, dz);
			}
		}
		for (int i = 0; i < h; i++) {
			this.placeBlock(world, x, y+i, z);
			if (bulge[i]) {
				for (int d = 2; d < 6; d++) {
					ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[d];
					int dx = x+dir.offsetX;
					int dz = z+dir.offsetZ;
					this.placeBlock(world, dx, y+i, dz);
				}
			}
		}
		int dy = y+h-1;
		if (dy < 0 || dy >= 256) {
			Satisforestry.logger.logError("Tried to place poison rocks out of bounds ("+dy+") @ "+x+", "+z+"!");
			return false;
		}
		world.setBlock(x, dy, z, SFBlocks.GASEMITTER.getBlockInstance(), 0, 3);
		TileGasVent te = (TileGasVent)world.getTileEntity(x, dy, z);
		te.activeRadius = ReikaRandomHelper.getRandomBetween(3, 5, rand);
		te.activeHeight = h+3;
		te.yOffset = 1-h;
		return true;
	}

	private void placeBlock(World world, int x, int y, int z) {
		world.setBlock(x, y, z, SFBlocks.TERRAIN.getBlockInstance(), TerrainType.POISONROCK.ordinal(), 3);
		//world.setBlock(x, y, z, Blocks.stone);
	}

	private boolean isReplaceable(World world, int x, int y, int z) {
		return ReikaWorldHelper.softBlocks(world, x, y, z) || world.getBlock(x, y, z) == SFBlocks.BAMBOO.getBlockInstance() || !world.getBlock(x, y, z).getMaterial().blocksMovement();
	}

	public void setFrequency(BiomeSection sub) {
		switch(sub) {
			case FOREST:
				generationRate = 3;
				break;
			case STREAMS:
				generationRate = 5;
				break;
			case SWAMP:
				generationRate = 7;
				break;
		}
	}
}