package Reika.Satisforestry.Biome.Generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.apache.commons.lang3.tuple.ImmutablePair;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.Instantiable.Data.ShuffledGrid;
import Reika.DragonAPI.Instantiable.Data.WeightedRandom;
import Reika.DragonAPI.Instantiable.Data.Immutable.BlockKey;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Libraries.ReikaDirectionHelper;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.World.ReikaChunkHelper;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Biome.DecoratorPinkForest;
import Reika.Satisforestry.Biome.Biomewide.UraniumCave;
import Reika.Satisforestry.Blocks.BlockPowerSlug;
import Reika.Satisforestry.Blocks.BlockPowerSlug.TilePowerSlug;
import Reika.Satisforestry.Blocks.BlockTerrain.TerrainType;
import Reika.Satisforestry.Registry.SFBlocks;

public class WorldGenPowerSlugs {

	private final ShuffledGrid[] noise = new ShuffledGrid[3];
	private long seed;

	//private static final HashSet<Coordinate> successes = new HashSet();
	//private static final HashMap<Coordinate, BlockKey> failures = new HashMap();

	public WorldGenPowerSlugs() {

	}
	/*
	public static ArrayList<String> getOutcropData() {
		ArrayList<String> li = new ArrayList();
		int total = successes.size()+failures.size();
		li.add(successes.size()+" of "+total+" attempts succeeded.");
		CountMap<BlockKey> map = new CountMap();
		for (BlockKey bk : failures.values()) {
			map.increment(bk);
		}
		for (BlockKey bk : map.keySet()) {
			li.add(bk.getLocalized()+" ("+bk.blockID+"): "+map.get(bk));
		}
		return li;
	}
	 */

	/** Block coords */
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
				int diff = dy >= 130 ? 1 : 0;
				int cave = -1;
				boolean outcrop = false;
				ForgeDirection side = ForgeDirection.DOWN;
				if (rand.nextInt(4) > 0) {
					ImmutablePair<Coordinate, ForgeDirection> c2 = this.tryFindCaveSpace(world, c.xCoord, c.zCoord, rand, dy);
					if (c2 != null) {
						c = c2.left;
						dy = c.yCoord;
						cave = this.getHeight(world, c.xCoord, dy, c.zCoord);
						diff += Math.min(3, 1+cave/16);
						side = c2.right;
					}
				}
				if (cave == -1 && rand.nextInt(3) == 0) {
					Coordinate c2 = this.tryGenerateOutcrop(world, c.xCoord, dy, c.zCoord, rand);
					if (c2 != null) {
						//successes.add(c2);
						c = c2;
						dy = c2.yCoord;
						diff++;
						outcrop = true;
					}
				}
				if (BlockPowerSlug.canReplace(world, c.xCoord, dy, c.zCoord)) {
					TilePowerSlug te = BlockPowerSlug.generatePowerSlugAt(world, c.xCoord, dy, c.zCoord, rand, i, false, diff, true, Integer.MAX_VALUE, side);
					if (te != null) {
						flags |= (1 << i);
						if (cave >= 12) {
							te.setNoSpawns();
						}
						if (cave >= 0) {
							for (int n = 0; n < 32; n++) {
								int dx = ReikaRandomHelper.getRandomPlusMinus(c.xCoord, rand.nextInt(3) == 0 ? 9 : 5, rand);
								int dz = ReikaRandomHelper.getRandomPlusMinus(c.zCoord, rand.nextInt(3) == 0 ? 9 : 5, rand);
								if (!world.getBlock(dx, dy, dz).isAir(world, dx, dy, dz))
									continue;
								int dy2 = dy;
								while (world.getBlock(dx, dy2-1, dz).isAir(world, dx, dy2-1, dz)) {
									dy2--;
								}
								if (rand.nextInt(4) == 0)
									UraniumCave.instance.generateMushroom(world, dx, dy2, dz, rand);
								else
									UraniumCave.instance.generateStalks(world, dx, dy2, dz, rand);
							}
						}
						break;
					}
				}
			}
			if (li.isEmpty()) {

			}
		}

		return flags;
	}

	private int getHeight(World world, int x, int y, int z) {
		for (int i = 1; i <= y; i++) {
			int dy = y-i;
			Block b = world.getBlock(x, dy, z);
			if (b.getMaterial().blocksMovement() && b.getCollisionBoundingBoxFromPool(world, x, dy, z) != null)
				return i-1;
		}
		return 256;
	}

	private ImmutablePair<Coordinate, ForgeDirection> tryFindCaveSpace(World world, int x, int z, Random rand, int maxY) {
		HashMap<Integer, WeightedRandom<ForgeDirection>> coords = new HashMap();
		for (int y = 12; y <= maxY-5; y++) {
			if (BlockPowerSlug.canReplace(world, x, y, z)) {
				for (int i = 0; i < 6; i++) {
					ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[i];
					if (BlockPowerSlug.canExistOn(world, x+dir.offsetX, y+dir.offsetY, z+dir.offsetZ)) {
						WeightedRandom<ForgeDirection> wr = coords.get(y);
						if (wr == null) {
							wr = new WeightedRandom();
							wr.setRNG(rand);
							coords.put(y, wr);
						}
						wr.addEntry(dir, dir == ForgeDirection.UP ? 30 : (dir == ForgeDirection.DOWN ? 5 : 20));
					}
				}
			}
		}
		if (coords.isEmpty())
			return null;
		Integer y = ReikaJavaLibrary.getRandomCollectionEntry(rand, coords.keySet());
		return new ImmutablePair(new Coordinate(x, y.intValue(), z), coords.get(y).getRandomEntry());
	}

	private Coordinate tryGenerateOutcrop(World world, int x, int y, int z, Random rand) {
		int h = ReikaRandomHelper.getRandomBetween(4, 7, rand);
		int lim2 = h/5;
		for (int j = 0; j < h; j++) {
			int r = j <= lim2 ? 2 : 1;
			for (int i = -r; i < r; i++) {
				for (int k = -r; k < r; k++) {
					if (!this.isOverwritableByOutcrop(world, x+i, y+j, z+k)) {
						if (j < 4) {/*
							Coordinate c = new Coordinate(x+i, y+j, z+k);
							Block b = c.getBlock(world);
							int meta = c.getBlockMetadata(world);
							if (b instanceof BlockRotatedPillar)
								meta = meta%4;
							failures.put(c, new BlockKey(b, meta));
						 */
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
		y--;
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
					widths[i] += ReikaRandomHelper.getRandomPlusMinus(0, 0.8, rand);
					widths[i] = MathHelper.clamp_float(widths[i], 0.2F, j <= lim2 ? 2 : 1);
				}
			}
		}
		return new Coordinate(x, y+h, z);
	}

	private boolean isOverwritableByOutcrop(World world, int x, int y, int z) {
		if (ReikaWorldHelper.softBlocks(world, x, y, z) || DecoratorPinkForest.isTerrain(world, x, y, z))
			return true;
		Block b = world.getBlock(x, y, z);
		return b == SFBlocks.BAMBOO.getBlockInstance() || b.getMaterial() == Material.vine || b.getMaterial() == Material.plants || b == Blocks.gravel || b == Blocks.dirt;
	}

	private void initNoise(World world) {
		if (noise[0] == null || seed != world.getSeed()) {
			seed = world.getSeed();
			int size = ReikaWorldHelper.getBiomeSize(world);
			int ds = size-4;
			noise[0] = new ShuffledGrid(40, 3, 4+ds/2, true);
			noise[1] = new ShuffledGrid(40, 5, 7+ds*2/3, true);
			noise[2] = new ShuffledGrid(40, 7, 9+ds, true);
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

}
