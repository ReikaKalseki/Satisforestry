package Reika.Satisforestry.Biome.Generator;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.Instantiable.Data.ShuffledGrid;
import Reika.DragonAPI.Instantiable.Data.Immutable.BlockKey;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Libraries.ReikaDirectionHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.World.ReikaChunkHelper;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Biome.DecoratorPinkForest;
import Reika.Satisforestry.Blocks.BlockPowerSlug;
import Reika.Satisforestry.Blocks.BlockPowerSlug.TilePowerSlug;
import Reika.Satisforestry.Blocks.BlockTerrain.TerrainType;
import Reika.Satisforestry.Registry.SFBlocks;

public class WorldGenPowerSlugs {

	private final ShuffledGrid[] noise = new ShuffledGrid[3];
	private long seed;

	public WorldGenPowerSlugs() {
		noise[0] = new ShuffledGrid(40, 4, 6, true);
		noise[1] = new ShuffledGrid(40, 6, 9, true);
		noise[2] = new ShuffledGrid(40, 8, 12, true);
	}

	public int generate(World world, Random rand, int chunkX, int chunkZ) {
		this.initNoise(world);
		int flags = 0;
		for (int i = 0; i < 3; i++) {
			if (!noise[i].isValid(chunkX >> 4, chunkZ >> 4))
				continue;
			ArrayList<Coordinate> li = ReikaChunkHelper.getChunkCoords(chunkX, chunkZ);
			while (!li.isEmpty()) {
				Coordinate c = li.remove(rand.nextInt(li.size()));
				if (!Satisforestry.isPinkForest(world, c.xCoord, c.zCoord))
					continue;
				int dy = DecoratorPinkForest.getTrueTopAt(world, c.xCoord, c.zCoord)+1;
				if (dy < 80)
					continue;
				if (this.isReplaceable(world, c.xCoord, dy, c.zCoord) && SFBlocks.SLUG.getBlockInstance().canBlockStay(world, c.zCoord, dy, c.zCoord)) {
					int diff = dy >= 130 ? 1 : 0;
					if (rand.nextInt(1) == 0) {
						Coordinate c2 = this.tryGenerateOutcrop(world, c.xCoord, dy, c.zCoord, rand);
						if (c2 != null) {
							c = c2;
							dy = c2.yCoord;
							diff++;
						}
					}
					TilePowerSlug te = BlockPowerSlug.generatePowerSlugAt(world, c.xCoord, dy, c.zCoord, rand, i, false, diff, true);
					if (te != null) {
						flags |= (1 << i);
						break;
					}
				}
			}
			if (li.isEmpty()) {

			}
		}

		return flags;
	}

	private Coordinate tryGenerateOutcrop(World world, int x, int y, int z, Random rand) {
		int h = ReikaRandomHelper.getRandomBetween(4, 7, rand);
		int lim2 = h/5;
		for (int j = 0; j < h; j++) {
			int r = j <= lim2 ? 2 : 1;
			for (int i = -r; i < r; i++) {
				for (int k = -r; k < r; k++) {
					if (!this.isOverwritable(world, x+i, y+j, z+k)) {
						if (j < 4) {
							return null;
						}
						else {
							h = j;
							i = k = j = 120;
							break;
						}
					}
				}
			}
		}
		BlockKey type = null;
		switch(rand.nextInt(6)) {
			case 0:
			case 1:
			case 2:
				type = new BlockKey(SFBlocks.TERRAIN.getBlockInstance(), TerrainType.OUTCROP.ordinal());
				break;
			case 3:
			case 4:
				type = new BlockKey(SFBlocks.TERRAIN.getBlockInstance(), TerrainType.PONDROCK.ordinal());
				break;
			case 5:
				type = new BlockKey(Blocks.stone, 0);
				break;
		}
		float[] widths = new float[4];
		for (int i = 0; i < 4; i++) {
			widths[i] = 1+rand.nextFloat()*1.4F;
		}
		for (int j = -3; j < h; j++) {
			int dy = y+j;
			type.place(world, x, dy, z);
			for (int i = 0; i < 4; i++) {
				ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[i+2];
				float w = widths[i];
				int r = Math.round(w);
				if (j == -1)
					r = 2;
				for (int d = 1; d <= r; d++) {
					int dx = x+dir.offsetX*d;
					int dz = z+dir.offsetZ*d;
					type.place(world, dx, dy, dz);
				}
				if (r == 2) {
					ForgeDirection left = ReikaDirectionHelper.getLeftBy90(dir);
					int dx = x+dir.offsetX+left.offsetX;
					int dz = z+dir.offsetZ+left.offsetZ;
					type.place(world, dx, dy, dz);
					dx = x+dir.offsetX-left.offsetX;
					dz = z+dir.offsetZ-left.offsetZ;
					type.place(world, dx, dy, dz);
				}
				if (j >= 0) {
					widths[i] += ReikaRandomHelper.getRandomPlusMinus(0, 0.4, rand);
					widths[i] = MathHelper.clamp_float(widths[i], 0.5F, j <= lim2 ? 2 : 1);
				}
			}
		}
		return new Coordinate(x, y+h, z);
	}

	private boolean isOverwritable(World world, int x, int y, int z) {
		if (ReikaWorldHelper.softBlocks(world, x, y, z) || DecoratorPinkForest.isTerrain(world, x, y, z))
			return true;
		Block b = world.getBlock(x, y, z);
		return b == SFBlocks.BAMBOO.getBlockInstance() || b == Blocks.gravel || b == Blocks.sand || b == Blocks.dirt || b == Blocks.grass;
	}

	private void initNoise(World world) {
		if (noise[0] == null || seed != world.getSeed()) {
			seed = world.getSeed();
			noise[0].calculate(seed);
			noise[1].calculate(seed);
			noise[2].calculate(seed);
		}
		/*
		if (noise[0] == null || noise[0].seed != world.getSeed()) {
			noise[0] = new VoronoiNoiseGenerator(world.getSeed());
			noise[1] = new VoronoiNoiseGenerator(-2*world.getSeed()/3);
			noise[2] = new VoronoiNoiseGenerator(~world.getSeed()+622381);
			noise[0].setFrequency(1/150D);
			noise[1].setFrequency(1/250D);
			noise[2].setFrequency(1/400D);
			noise[0].randomFactor = 0.35;
			noise[1].randomFactor = 0.55;
			noise[2].randomFactor = 0.75;
		}*/

	}

	private boolean isReplaceable(World world, int x, int y, int z) {
		return ReikaWorldHelper.softBlocks(world, x, y, z);// || world.getBlock(x, y, z).isLeaves(world, x, y, z);
	}

}
