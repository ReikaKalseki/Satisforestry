package Reika.Satisforestry.Biome.Generator;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.Instantiable.Data.BlockStruct.FilledBlockArray;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Libraries.ReikaDirectionHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.AlternateRecipes.CrashSiteStructure;
import Reika.Satisforestry.Biome.DecoratorPinkForest;
import Reika.Satisforestry.Blocks.BlockCrashSite.TileCrashSite;
import Reika.Satisforestry.Blocks.BlockTerrain.TerrainType;
import Reika.Satisforestry.Registry.SFBlocks;

public class WorldGenCrashSite extends WorldGenerator {

	private final boolean forceGen;

	public WorldGenCrashSite(boolean force) {
		forceGen = force;
	}

	@Override
	public boolean generate(World world, Random rand, int x, int yUnused, int z) {
		int dx = ReikaRandomHelper.getRandomPlusMinus(x, 5, rand);
		int dz = ReikaRandomHelper.getRandomPlusMinus(z, 5, rand);
		if (!forceGen && !Satisforestry.isPinkForest(world, dx, dz))
			return false;
		//int dy = DecoratorPinkForest.getTrueTopAt(world, dx, dz);

		return this.tryPlace(world, dx, dz, rand);
	}

	private boolean tryPlace(World world, int x, int z, Random rand) {
		int minY = 999;
		int maxY = 0;
		ForgeDirection dir = rand.nextInt(6) == 0 ? ForgeDirection.UP : ReikaDirectionHelper.getRandomDirection(false, rand);
		int a = dir.offsetY == 0 ? 6 : 2;
		int n = dir.offsetY == 0 ? 3 : 8;
		for (int i = -a; i <= a; i++) {
			for (int k = -a; k <= a; k++) {
				int dx = x+i;
				int dz = z+k;
				int y = DecoratorPinkForest.getTrueTopAt(world, dx, dz);
				minY = Math.min(minY, y);
				maxY = Math.max(maxY, y);
				if (!this.isValidGroundBlock(world, dx, y, dz))
					return false;
				for (int h = 1; h <= n; h++) {
					int dy = y+h;
					if (!this.isValidAirBlock(world, dx, dy, dz))
						return false;
				}
			}
		}
		if (maxY-minY > 3)
			return false;
		int y0 = (minY+maxY)/2;
		int y = dir.offsetY == 0 ? y0+2 : y0+7;
		FilledBlockArray arr = CrashSiteStructure.getStructure(world, x, y, z, dir);
		arr.place();
		for (Coordinate c : arr.keySet())
			c.setY(y0-2).setBlock(world, Blocks.dirt);
		TileCrashSite te = (TileCrashSite)world.getTileEntity(x, y, z);
		te.generate(rand);
		return true;
	}

	private boolean isValidAirBlock(World world, int x, int y, int z) {
		Block b = world.getBlock(x, y, z);
		return ReikaWorldHelper.softBlocks(world, x, y, z) || b == SFBlocks.BAMBOO.getBlockInstance();
	}

	private boolean isValidGroundBlock(World world, int x, int y, int z) {
		Block b = world.getBlock(x, y, z);
		return b == Blocks.grass || b == Blocks.dirt || b == Blocks.sand || b == Blocks.stone || (b == SFBlocks.TERRAIN.getBlockInstance() && world.getBlockMetadata(x, y, z) == TerrainType.OUTCROP.ordinal());
	}
}