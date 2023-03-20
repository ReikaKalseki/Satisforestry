package Reika.Satisforestry.Biome.Generator;

import java.util.HashSet;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.World.ReikaBlockHelper;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Biome.DecoratorPinkForest;
import Reika.Satisforestry.Blocks.BlockFrackingAux.TileFrackingAux;
import Reika.Satisforestry.Blocks.BlockFrackingNode.TileFrackingNode;
import Reika.Satisforestry.Blocks.BlockTerrain.TerrainType;
import Reika.Satisforestry.Config.NodeResource.Purity;
import Reika.Satisforestry.Config.ResourceFluid;
import Reika.Satisforestry.Registry.SFBlocks;

public class WorldGenFrackingNode extends WorldGenerator {

	private final boolean forceGen;

	public WorldGenFrackingNode(boolean force) {
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
		int a = 9;
		int minY = 999;
		int maxY = 0;
		for (int i = -a; i <= a; i++) {
			for (int k = -a; k <= a; k++) {
				int dx = x+i;
				int dz = z+k;
				int y = DecoratorPinkForest.getTrueTopAt(world, dx, dz);
				minY = Math.min(minY, y);
				maxY = Math.max(maxY, y);
				if (!this.isValidGroundBlock(world, dx, y, dz))
					return false;
				if (Math.abs(a) <= 4) {
					for (int h = 1; h <= 8; h++) {
						int dy = y+h;
						if (!this.isValidAirBlock(world, dx, dy, dz))
							return false;
					}
				}
			}
		}
		if (maxY-minY > 2)
			return false;
		ResourceFluid rf = TileFrackingNode.selectResource(rand);
		HashSet<Coordinate> nodes = new HashSet();
		for (int ang = 0; ang < 360; ang += 360/rf.maxNodes) {
			double cos = Math.cos(Math.toRadians(ang));
			double sin = Math.sin(Math.toRadians(ang));
			for (int i = 0; i < 20; i++) {
				double r = ReikaRandomHelper.getRandomBetween(7D, 9D, rand);
				int dx = ReikaRandomHelper.getRandomPlusMinus(MathHelper.floor_double(x+r*cos), 2, rand);
				int dz = ReikaRandomHelper.getRandomPlusMinus(MathHelper.floor_double(z+r*sin), 2, rand);
				int y = DecoratorPinkForest.getTrueTopAt(world, dx, dz);
				if (y >= minY && this.isValidGroundBlock(world, dx, y, dz) && this.isValidGroundBlock(world, dx, y-1, dz) && this.isValidAirBlock(world, dx, y+1, dz)) {
					nodes.add(new Coordinate(dx, (maxY+minY)/2, dz));
					break;
				}
			}
		}
		if (nodes.size() < 4)
			return false;
		int y = Math.max(minY, (maxY+minY)/2-1);
		this.clearArea(world, x, y, z, 4);
		world.setBlock(x, y, z, SFBlocks.FRACKNODE.getBlockInstance(), 0, 2);
		TileFrackingNode te = (TileFrackingNode)world.getTileEntity(x, y, z);
		Coordinate root = new Coordinate(te);
		te.generate(rf, rand);
		Purity p = te.getPurity();
		for (Coordinate c : nodes) {
			c = c.setY(y);
			this.clearArea(world, c.xCoord, y, c.zCoord, 2.5);
			this.clearArea(world, (c.xCoord+x)/2, y, (c.zCoord+z)/2, 2);
			Purity p2 = TileFrackingNode.getRelativePurity(p, rand);
			c.setBlock(world, SFBlocks.FRACKNODEAUX.getBlockInstance(), p2.ordinal(), 2);
			TileFrackingAux te2 = (TileFrackingAux)c.getTileEntity(world);
			te2.linkTo(root);
		}
		return true;
	}

	private void clearArea(World world, int x, int y, int z, double r) {
		for (int i = -(int)r; i <= r; i++) {
			for (int k = -(int)r; k <= r; k++) {
				if (i*i+k*k <= r*r+0.5) {
					for (int dy = y-2; dy <= y; dy++) {
						world.setBlock(x+i, dy, z+k, y == dy ? SFBlocks.CAVESHIELD.getBlockInstance() : Blocks.dirt, 0, 2);
					}
					for (int dy = y+1; dy <= y+4; dy++) {
						world.setBlock(x+i, dy, z+k, Blocks.air, 0, 2);
					}
				}
			}
		}
	}

	private boolean isValidAirBlock(World world, int x, int y, int z) {
		Block b = world.getBlock(x, y, z);
		if (ReikaBlockHelper.isLiquid(b))
			return false;
		return ReikaWorldHelper.softBlocks(world, x, y, z) || b == SFBlocks.BAMBOO.getBlockInstance();
	}

	private boolean isValidGroundBlock(World world, int x, int y, int z) {
		Block b = world.getBlock(x, y, z);
		return b == Blocks.grass || b == Blocks.dirt || b == Blocks.sand || b == Blocks.stone || (b == SFBlocks.TERRAIN.getBlockInstance() && world.getBlockMetadata(x, y, z) == TerrainType.OUTCROP.ordinal());
	}

	private void placeBlock(World world, int x, int y, int z) {
		world.setBlock(x, y, z, SFBlocks.TERRAIN.getBlockInstance(), TerrainType.POISONROCK.ordinal(), 3);
		//world.setBlock(x, y, z, Blocks.stone);
	}
}