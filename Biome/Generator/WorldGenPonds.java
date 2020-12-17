package Reika.Satisforestry.Biome.Generator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.Instantiable.Data.BlockStruct.AbstractSearch.PropagationCondition;
import Reika.DragonAPI.Instantiable.Data.BlockStruct.AbstractSearch.TerminationCondition;
import Reika.DragonAPI.Instantiable.Data.BlockStruct.BreadthFirstSearch;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.Satisforestry.SFBlocks;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Biome.BiomePinkForest.BiomeSection;
import Reika.Satisforestry.Biome.DecoratorPinkForest;
import Reika.Satisforestry.Blocks.BlockTerrain.TerrainType;

public class WorldGenPonds extends WorldGenerator {

	@Override
	public boolean generate(World world, Random rand, int x0, int y0, int z0) {
		if (Satisforestry.pinkforest.getSubBiome(world, x0, z0) != BiomeSection.SWAMP)
			return false;

		for (int i = 0; i < 3; i++) {
			int x = ReikaRandomHelper.getRandomPlusMinus(x0, 6, rand);
			int z = ReikaRandomHelper.getRandomPlusMinus(z0, 6, rand);
			int y = DecoratorPinkForest.getTrueTopAt(world, x, z);

			if (!isValidBlock(world, x, y, z))
				continue;
			BreadthFirstSearch bfs = new BreadthFirstSearch(x, y, z);
			int r = 9;
			PropagationCondition prop = new PropagationCondition() {

				@Override
				public boolean isValidLocation(World world, int x, int y, int z, Coordinate from) {
					return y >= from.yCoord && bfs.root.isWithinDistOnAllCoords(x, y, z, r) && bfs.root.getDistanceTo(x, y, z) <= r+0.5 && isValidBlock(world, x, y, z);
				}

			};
			TerminationCondition end = new TerminationCondition() {

				@Override
				public boolean isValidTerminus(World world, int x, int y, int z) {
					return false;
				}

			};
			bfs.complete(world, prop, end);

			Set<Coordinate> blocks = bfs.getTotalSearchedCoords();

			if (!blocks.isEmpty()) {
				HashSet<Coordinate> set = new HashSet(blocks);
				HashSet<Coordinate> adj = new HashSet();
				for (Coordinate c : set) {
					adj.addAll(c.getAdjacentCoordinates());
				}
				boolean flag = true;
				while (flag) {
					flag = false;
					adj.removeAll(set);
					for (Coordinate c : adj) {
						if (set.contains(c.offset(0, -1, 0))) {
							set.add(c);
							flag = true;
						}
					}
				}

				for (Coordinate c : blocks) {
					if (blocks.contains(c.offset(0, -1, 0))) {
						set.remove(c);
					}
				}

				for (Coordinate c : set) {
					cutBlock(world, c);
					int dy = 1;
					int depth = c.getDistanceTo(x, y, z) <= 4 ? 1 : 0;
					while (dy < depth && isValidBlock(world, c.xCoord, c.yCoord-dy, c.zCoord)) {
						world.setBlock(c.xCoord, c.yCoord-dy, c.zCoord, Blocks.glass);
						dy++;
					}
				}
				HashMap<Coordinate, Integer> floor = new HashMap();
				for (Coordinate c2 : adj) {
					if (c2.softBlock(world) || !DecoratorPinkForest.isTerrain(world, c2.xCoord, c2.yCoord, c2.zCoord))
						continue;
					cutBlock(world, c2);
					c2.setBlock(world, Blocks.sand);
					Coordinate key = c2.to2D();
					Integer get = floor.get(key);
					if (get == null) {
						get = 255;
					}
					get = Math.min(get, c2.yCoord);
					floor.put(key, get);
				}
				Coordinate ctr = bfs.root.to2D();
				int my = floor.get(ctr);
				int h = ReikaRandomHelper.getRandomBetween(1, 3, rand);
				boolean thick = rand.nextInt(3) == 0;
				for (int dy = my; dy < my+h; dy++) {
					world.setBlock(ctr.xCoord, dy, ctr.zCoord, SFBlocks.TERRAIN.getBlockInstance(), TerrainType.PONDROCK.ordinal(), 2);
					world.setBlock(ctr.xCoord+1, dy, ctr.zCoord, SFBlocks.TERRAIN.getBlockInstance(), TerrainType.PONDROCK.ordinal(), 2);
					world.setBlock(ctr.xCoord-1, dy, ctr.zCoord, SFBlocks.TERRAIN.getBlockInstance(), TerrainType.PONDROCK.ordinal(), 2);
					world.setBlock(ctr.xCoord, dy, ctr.zCoord+1, SFBlocks.TERRAIN.getBlockInstance(), TerrainType.PONDROCK.ordinal(), 2);
					world.setBlock(ctr.xCoord, dy, ctr.zCoord-1, SFBlocks.TERRAIN.getBlockInstance(), TerrainType.PONDROCK.ordinal(), 2);
					if (thick || (h > 1 && dy == my)) {
						world.setBlock(ctr.xCoord+1, dy, ctr.zCoord+1, SFBlocks.TERRAIN.getBlockInstance(), TerrainType.PONDROCK.ordinal(), 2);
						world.setBlock(ctr.xCoord-1, dy, ctr.zCoord+1, SFBlocks.TERRAIN.getBlockInstance(), TerrainType.PONDROCK.ordinal(), 2);
						world.setBlock(ctr.xCoord+1, dy, ctr.zCoord-1, SFBlocks.TERRAIN.getBlockInstance(), TerrainType.PONDROCK.ordinal(), 2);
						world.setBlock(ctr.xCoord-1, dy, ctr.zCoord-1, SFBlocks.TERRAIN.getBlockInstance(), TerrainType.PONDROCK.ordinal(), 2);
					}
				}
				world.setBlock(ctr.xCoord, my+h, ctr.zCoord, SFBlocks.TERRAIN.getBlockInstance(), TerrainType.PONDROCK.ordinal(), 2);
				ReikaJavaLibrary.pConsole("Generated a swamp pond at "+bfs.root);
				return true;
			}
		}

		return false;
	}

	private static void cutBlock(World world, Coordinate c) {
		world.setBlock(c.xCoord, c.yCoord, c.zCoord, Blocks.water, 0, 3);
		if (world.getBlock(c.xCoord, c.yCoord+1, c.zCoord).getMaterial() == Material.plants) {
			world.setBlock(c.xCoord, c.yCoord+1, c.zCoord, Blocks.air, 0, 2);
		}
	}

	private static boolean isValidBlock(World world, int x, int y, int z) {
		return DecoratorPinkForest.isTerrain(world, x, y, z) && hasSolidOnAllSides(world, x, y, z);
	}

	private static boolean hasSolidOnAllSides(World world, int x, int y, int z) {
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
