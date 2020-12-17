package Reika.Satisforestry.Biome.Generator;

import java.util.Random;
import java.util.Set;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.Instantiable.Data.BlockStruct.AbstractSearch.PropagationCondition;
import Reika.DragonAPI.Instantiable.Data.BlockStruct.AbstractSearch.TerminationCondition;
import Reika.DragonAPI.Instantiable.Data.BlockStruct.BreadthFirstSearch;
import Reika.DragonAPI.Instantiable.Data.Immutable.BlockBox;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Biome.BiomePinkForest.BiomeSection;
import Reika.Satisforestry.Biome.DecoratorPinkForest;

public class WorldGenPonds extends WorldGenerator {

	@Override
	public boolean generate(World world, Random rand, int x0, int y0, int z0) {
		if (Satisforestry.pinkforest.getSubBiome(world, x0, z0) != BiomeSection.SWAMP)
			return false;

		for (int i = 0; i < 3; i++) {
			int x = ReikaRandomHelper.getRandomPlusMinus(x0, 6, rand);
			int z = ReikaRandomHelper.getRandomPlusMinus(z0, 6, rand);
			int y = DecoratorPinkForest.getTrueTopAt(world, x, z);

			if (!DecoratorPinkForest.isTerrain(world, x, y, z) || !this.hasSolidOnAllSides(world, x, y, z))
				continue;
			BreadthFirstSearch bfs = new BreadthFirstSearch(x, y, z);
			int r = 9;
			BlockBox box = new BlockBox(x-r, y-6, z-r, x+r, y, z+r);
			PropagationCondition prop = new PropagationCondition() {

				@Override
				public boolean isValidLocation(World world, int x, int y, int z, Coordinate from) {
					return box.isBlockInside(x, y, z) && DecoratorPinkForest.isTerrain(world, x, y, z) && bfs.root.getDistanceTo(x, y, z) <= r+0.5 && WorldGenPonds.this.hasSolidOnAllSides(world, x, y, z);
				}

			};
			TerminationCondition end = new TerminationCondition() {

				@Override
				public boolean isValidTerminus(World world, int x, int y, int z) {
					return false;
				}

			};
			bfs.complete(world, prop, end);

			Set<Coordinate> set = bfs.getTotalSearchedCoords();

			if (!set.isEmpty()) {
				for (Coordinate c : set) {
					c.setBlock(world, Blocks.glass);
					for (Coordinate c2 : c.getAdjacentCoordinates()) {
						if (set.contains(c2) || c2.softBlock(world) || !DecoratorPinkForest.isTerrain(world, c2.xCoord, c2.yCoord, c2.zCoord))
							continue;
						c2.setBlock(world, Blocks.stone);
					}
				}
				bfs.root.setBlock(world, Blocks.glowstone);
				ReikaJavaLibrary.pConsole("Generated a swamp pond at "+bfs.root);
				return true;
			}
		}

		return false;
	}

	private boolean hasSolidOnAllSides(World world, int x, int y, int z) {
		for (int i = 0; i < 6; i++) {
			ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[i];
			if (dir == ForgeDirection.UP)
				continue;
			int dx = x+dir.offsetX;
			int dy = y+dir.offsetY;
			int dz = z+dir.offsetZ;
			if (ReikaWorldHelper.softBlocks(world, dx, dy, dz) || !world.getBlock(dx, dy, dz).getMaterial().blocksMovement())
				return false;
		}
		return true;
	}

}
