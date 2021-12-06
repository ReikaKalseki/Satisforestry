package Reika.Satisforestry.Biome.Generator;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaPhysicsHelper;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Blocks.BlockPowerSlug;
import Reika.Satisforestry.Registry.SFBlocks;

public class GiantPinkTreeGenerator extends PinkTreeGeneratorBase {

	private final boolean generateImmediately;
	private final Random treeRand = new Random();

	private boolean readyToGenerate = false;
	private long randomSeed = 0;

	private int groundGap;

	public GiantPinkTreeGenerator(boolean force, boolean genImmediate) {
		super(force, PinkTreeTypes.GIANTTREE);
		trunkSize = 3;
		generateImmediately = genImmediate;
		readyToGenerate = generateImmediately;
	}

	@Override
	public boolean generate(World world, Random chunkRand, int x, int y, int z) {
		if (!forceGen) {
			if (y < 108) //was 96
				return false;
		}
		if (chunkRand != null)
			randomSeed = chunkRand.nextLong();
		treeRand.setSeed(randomSeed);
		treeRand.nextBoolean();
		if (!readyToGenerate) {
			readyToGenerate = true;
			forceGen = true;
			//TreeGenCache.instance.addTree(world, x, y, z, this);
			//PinkForestPersistentData.initNetworkData(world).setDirty(true);
			return true;
		}
		int h1 = ReikaRandomHelper.getRandomBetween(10, 18, treeRand); //was 20-30, then 18-25, then 12-24
		int h2 = ReikaRandomHelper.getRandomBetween(64, 80, treeRand); //was 15-30, then 40-72, then 36-64, then 48-72, then 55-80
		groundGap = ReikaRandomHelper.getRandomBetween(3, isSaplingGrowth ? 5 : 6, treeRand); //was 2-5, then 3-6
		/*
		int y1 = groundGap+h1;
		int y2 = y1+h2;
		for (int i = 0; i <= y2; i++) {
			if (!world.getBlock(x-1, y+i, z-1).isAir(world, x-1, y+i, z-1))
				return false;
			if (!world.getBlock(x-1, y+i, z+2).isAir(world, x-1, y+i, z+2))
				return false;
			if (!world.getBlock(x+2, y+i, z-1).isAir(world, x+2, y+i, z-1))
				return false;
			if (!world.getBlock(x+2, y+i, z+2).isAir(world, x+2, y+i, z+2))
				return false;
		}
		/*
		for (int i = 0; i < groundGap; i++) {
			world.setBlock(x+2, y+i, z-1, Satisforestry.log);
			world.setBlock(x-1, y+i, z-1, Satisforestry.log);
			world.setBlock(x+2, y+i, z+2, Satisforestry.log);
			world.setBlock(x-1, y+i, z+2, Satisforestry.log);
		}
		 *//*
		for (int i = groundGap; i < y1; i++) {
			world.setBlock(x, y+i, z, Satisforestry.log);
			world.setBlock(x+1, y+i, z, Satisforestry.log);
			world.setBlock(x, y+i, z+1, Satisforestry.log);
			world.setBlock(x+1, y+i, z+1, Satisforestry.log);
		}
		for (int i = y1; i < y2; i++) {
			world.setBlock(x, y+i, z, Satisforestry.log);
			world.setBlock(x+1, y+i, z, Satisforestry.log);
			world.setBlock(x, y+i, z+1, Satisforestry.log);
			world.setBlock(x+1, y+i, z+1, Satisforestry.log);

			world.setBlock(x+2, y+i, z, Satisforestry.leaves);
			world.setBlock(x-1, y+i, z, Satisforestry.leaves);
			world.setBlock(x, y+i, z+2, Satisforestry.leaves);
			world.setBlock(x, y+i, z-1, Satisforestry.leaves);
		}
		return true;*/
		leafDistanceLimit = treeRand.nextBoolean() ? 4 : 3;
		heightLimitLimit = h1+h2;
		branchSlope = ReikaRandomHelper.getRandomPlusMinus(0, BASE_SLOPE*2.5, treeRand);
		heightAttenuation = BASE_ATTENUATION*1.1;
		//minBranchHeight = hl*0+12;
		minHeight = h1+h2;
		globalOffset[1] = Math.max(h1+groundGap-4, 0);
		leafDensity = 0.625F; //was 0.75
		branchDensity = 0.4F; //was 0.67
		if (super.generate(world, treeRand, x, y, z)) {
			for (int dy = groundGap; dy < globalOffset[1]; dy++) {
				for (int i = -1; i <= 1; i++) {
					for (int k = -1; k <= 1; k++) {
						if (i == 0 || k == 0)
							world.setBlock(x+i, y+dy, z+k, Satisforestry.log, 1, 2);
					}
				}
			}

			int n = ReikaRandomHelper.getRandomBetween(5, 8, treeRand); //was 4-8
			double angsplit = 360D/n;
			for (int i = 0; i < n; i++) {
				double dx = x+0.5;
				double dz = z+0.5;
				double dy = y+groundGap+0.5;
				double phi = ReikaRandomHelper.getRandomPlusMinus(angsplit*i, 15, treeRand);//rand.nextDouble()*360;
				double theta = ReikaRandomHelper.getRandomBetween(-15, 5, treeRand);
				double[] xyz = ReikaPhysicsHelper.polarToCartesian(1.5, theta, phi);
				dx += xyz[0];
				dz += xyz[2];
				double dt = ReikaRandomHelper.getRandomBetween(5, 20, treeRand);
				double dp = ReikaRandomHelper.getRandomPlusMinus(0, 12, treeRand);
				double dpa = ReikaRandomHelper.getRandomPlusMinus(0, 4, treeRand);
				int ix = MathHelper.floor_double(dx);
				int iy = MathHelper.floor_double(dy);
				int iz = MathHelper.floor_double(dz);
				while (dy >= y-0.5 || ReikaWorldHelper.softBlocks(world, ix, iy-1, iz)|| world.getBlock(ix, iy-1, iz) == Satisforestry.leaves) {
					ix = MathHelper.floor_double(dx);
					iy = MathHelper.floor_double(dy);
					iz = MathHelper.floor_double(dz);
					world.setBlock(ix, iy, iz, Satisforestry.log, 1, 2);
					xyz = ReikaPhysicsHelper.polarToCartesian(0.5, theta, phi);
					dx += xyz[0];
					dy += xyz[1];
					dz += xyz[2];
					theta = Math.max(-90, theta-dt);
					phi += dp;
					dp += dpa;
				}
			}
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	protected float layerSize(int layer) {
		float f = 0.5F; //was 1.3, then 0.5
		float h = layer/(float)heightLimitLimit;
		double th = 0.5;//0.67;//0.75;
		if (h > th) {
			double dh = th/h;
			f *= Math.pow(dh, 0.8);
		}
		//ReikaJavaLibrary.pConsole(layer+" of "+heightLimitLimit+" > "+h+" > "+f);
		return super.layerSize(layer)*f;
	}

	@Override
	protected float leafSize(int r) {
		return super.leafSize(r);
	}

	@Override
	protected boolean isValidUnderBlock(World world, int x, int y, int z) {
		return true;
	}

	@Override
	protected boolean isReplaceable(World world, int x, int y, int z) {
		return super.isReplaceable(world, x, y, z) || ReikaWorldHelper.softBlocks(world, x, y, z) || world.getBlock(x, y, z) == SFBlocks.BAMBOO.getBlockInstance();
	}

	@Override
	protected void setBlockAndNotifyAdequately(World world, int x, int y, int z, Block b, int meta) {
		super.setBlockAndNotifyAdequately(world, x, y, z, b, meta);
		//world.setBlock(x+globalOffset[0], y+globalOffset[1], z+globalOffset[2], b, meta, doUpdates ? 3 : 2);
		//TreeGenCache.instance.addBlock(world, x, y, z, b, meta);
	}

	public static GiantPinkTreeGenerator readNBT(NBTTagCompound tag) {
		GiantPinkTreeGenerator gen = new GiantPinkTreeGenerator(false, false);
		gen.forceGen = tag.getBoolean("force");
		gen.randomSeed = tag.getLong("seed");
		gen.readyToGenerate = tag.getBoolean("ready");
		return gen;
	}

	public NBTTagCompound getNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setBoolean("force", forceGen);
		tag.setLong("seed", randomSeed);
		tag.setBoolean("ready", readyToGenerate);
		return tag;
	}

	@Override
	protected void postGenerate(World world, Random rand, int x, int y, int z) {
		super.postGenerate(world, rand, x, y, z);
		if (allowSlugs && rand.nextInt(25) == 0) {
			int tier = rand.nextInt(5) == 0 ? 1 : 0;
			int dy = y+groundGap-1;
			int dx = ReikaRandomHelper.getRandomPlusMinus(x, 1, rand);
			int dz = ReikaRandomHelper.getRandomPlusMinus(z, 1, rand);
			BlockPowerSlug.generatePowerSlugAt(world, dx, dy, dz, rand, tier, false, 0, true, 8, ForgeDirection.UP);
		}
	}

	@Override
	protected int getDifficultyByHeight(int y, int dy, Random rand) {
		return MathHelper.clamp_int(dy/27, 0, 3);
	}

	@Override
	protected int getSlugByHeight(int y, int dy, Random rand) {
		//int reach = dy/32;
		int tier = 0;
		if (dy >= 64) {
			float f = (dy-64)/64F;
			tier = rand.nextFloat() < f ? 2 : 1;
		}
		else if (dy >= 24) {
			float f = (dy-24)/40F;
			tier = rand.nextFloat() < f*0.9 ? 1 : 0;
		}
		return tier;
	}

	@Override
	protected float getTrunkSlugChancePerBlock() {
		return 0.0005F;
	}

	@Override
	protected float getTreeTopSlugChance() {
		return 0.15F/this.getTreeTopSlugAttemptCount();
	}

	@Override
	protected boolean canSpawnLeaftopMobs() {
		return true;
	}

	@Override
	protected int getTreeTopSlugAttemptCount() {
		return 3;
	}

	@Override
	protected float getBranchSlugChancePerBlock() {
		return 0.00004F;
	}

}
