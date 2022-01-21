package Reika.Satisforestry.Biome.Generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.Instantiable.Data.Immutable.BlockKey;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Instantiable.Worldgen.ModifiableBigTree;
import Reika.DragonAPI.Libraries.ReikaDirectionHelper;
import Reika.DragonAPI.Libraries.Rendering.ReikaColorAPI;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.API.PinkTreeType;
import Reika.Satisforestry.Blocks.BlockPowerSlug;
import Reika.Satisforestry.Blocks.BlockPowerSlug.TilePowerSlug;
import Reika.Satisforestry.Registry.SFBlocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


public abstract class PinkTreeGeneratorBase extends ModifiableBigTree {

	protected boolean forceGen;
	public final PinkTreeTypes type;

	private int generationOriginX;
	private int generationOriginY;
	private int generationOriginZ;

	private final HashMap<Coordinate, Integer> logs = new HashMap();
	private final HashMap<Coordinate, Integer> leavesTop = new HashMap();

	public boolean allowSlugs = true;
	public boolean isSaplingGrowth = false;

	public PinkTreeBlockCallback blockCallback = null;

	public PinkTreeGeneratorBase(boolean force, PinkTreeTypes leaf) {
		super(force);
		forceGen = force;
		type = leaf;
	}

	@Override
	protected final BlockKey getLogBlock(int x, int y, int z) {
		return new BlockKey(SFBlocks.LOG.getBlockInstance(), type.ordinal());
	}

	@Override
	protected final BlockKey getLeafBlock(int x, int y, int z) {
		return new BlockKey(Satisforestry.leaves, type.ordinal());
	}

	@Override
	protected final boolean isMatchingLog(Block b) {
		return b == SFBlocks.LOG.getBlockInstance();
	}

	@Override
	protected final boolean isMatchingSapling(Block b) {
		return b == SFBlocks.SAPLING.getBlockInstance();
	}

	@Override
	public boolean generate(World world, Random r, int x, int y, int z) {
		generationOriginX = x;
		generationOriginY = y;
		generationOriginZ = z;
		return super.generate(world, r, x, y, z);
	}

	protected void postGenerate(World world, Random rand, int x, int y, int z) {
		if (!allowSlugs)
			return;
		ArrayList<Entry<Coordinate, Integer>> set = null;
		for (int n = 0; n < this.getTreeTopSlugAttemptCount(); n++) {
			if (rand.nextFloat() < this.getTreeTopSlugChance()) {
				TilePowerSlug te = null;
				if (set == null)
					set = new ArrayList(leavesTop.entrySet());
				while (te == null && !set.isEmpty()) {
					int i = rand.nextInt(set.size());
					Entry<Coordinate, Integer> e = set.remove(i);
					Coordinate c = e.getKey();
					int dy = e.getValue()+1;
					int ddy = dy-generationOriginY;
					int tier = this.getSlugByHeight(dy, ddy, rand);
					te = BlockPowerSlug.generatePowerSlugAt(world, c.xCoord, dy, c.zCoord, rand, tier, false, this.getDifficultyByHeight(dy, ddy, rand), this.canSpawnLeaftopMobs(), 3, ForgeDirection.DOWN);
				}
			}
		}
		for (Entry<Coordinate, Integer> e : logs.entrySet()) {
			if (e.getValue() < 4) {
				Coordinate c = e.getKey();
				if (c.yCoord >= trunkBottom && c.yCoord <= Math.min(trunkTop, bottomLeaf-1)) {
					if (rand.nextFloat() < this.getTrunkSlugChancePerBlock()) {
						ArrayList<ForgeDirection> li = ReikaDirectionHelper.getRandomOrderedDirections(false);
						for (ForgeDirection dir : li) {
							Coordinate c2 = c.offset(dir, -1);
							int dy = c2.yCoord-generationOriginY;
							int tier = this.getSlugByHeight(c2.yCoord, dy, rand);
							TilePowerSlug te = BlockPowerSlug.generatePowerSlugAt(world, c2.xCoord, c2.yCoord, c2.zCoord, rand, tier, false, this.getDifficultyByHeight(c2.yCoord-generationOriginY, dy, rand), false, 1, dir);
							if (te != null) {
								te.setNoSpawns();
								break;
							}
						}
					}
				}
			}
			else if (rand.nextFloat() < this.getBranchSlugChancePerBlock()) {
				Coordinate c = e.getKey().offset(0, 1, 0);
				if (c.isEmpty(world)) {
					int dy = c.yCoord-generationOriginY;
					int tier = this.getSlugByHeight(c.yCoord, dy, rand);
					if (rand.nextFloat() <= 0.4)
						tier = Math.min(2, tier+1); //+1 since hard to find
					int diff = this.getDifficultyByHeight(c.yCoord-generationOriginY, dy, rand)+1; //+1 since hard to find
					BlockPowerSlug.generatePowerSlugAt(world, c.xCoord, c.yCoord, c.zCoord, rand, tier, false, diff, false);
				}
			}
		}
	}

	protected int getTreeTopSlugAttemptCount() {
		return 1;
	}

	protected abstract float getBranchSlugChancePerBlock();
	protected abstract boolean canSpawnLeaftopMobs();
	protected abstract int getDifficultyByHeight(int y, int dy, Random rand);
	protected abstract int getSlugByHeight(int y, int dy, Random rand);

	@Override
	protected void setBlockAndNotifyAdequately(World world, int x, int y, int z, Block b, int meta) {
		boolean log = b == SFBlocks.LOG.getBlockInstance();
		boolean leaf = b == SFBlocks.LEAVES.getBlockInstance();
		if (blockCallback != null) {
			if (log) {
				if (blockCallback.placeLog(world, x+globalOffset[0], y+globalOffset[1], z+globalOffset[2], b, meta))
					return;
			}
			if (leaf) {
				if (blockCallback.placeLeaf(world, x+globalOffset[0], y+globalOffset[1], z+globalOffset[2], b, meta))
					return;
			}
		}
		super.setBlockAndNotifyAdequately(world, x, y, z, b, meta);
		if (log) {
			logs.put(new Coordinate(x+globalOffset[0], y+globalOffset[1], z+globalOffset[2]), meta);
		}
		else if (leaf) {
			Coordinate c = new Coordinate(x+globalOffset[0], 0, z+globalOffset[2]);
			Integer get = leavesTop.get(c);
			int dy = y+globalOffset[1];
			if (get == null || get.intValue() < dy)
				leavesTop.put(c, dy);
		}
	}

	protected abstract float getTrunkSlugChancePerBlock();

	protected abstract float getTreeTopSlugChance();

	public static interface PinkTreeBlockCallback {

		boolean placeLog(World world, int x, int y, int z, Block b, int meta);

		boolean placeLeaf(World world, int x, int y, int z, Block b, int meta);

	}

	public static enum PinkTreeTypes implements PinkTreeType {
		TREE,
		GIANTTREE,
		JUNGLE;

		public final String fastIcon;
		public final String fancyIcon;

		public static final PinkTreeTypes[] list = values();

		private PinkTreeTypes() {
			String base = "satisforestry:pinkleaf";//_"+this.name().toLowerCase(Locale.ENGLISH);
			fancyIcon = base+"_fancy";
			fastIcon = base+"_fast";
		}

		public boolean isValidLogMeta(int meta) {
			return meta%4 == this.ordinal();
		}

		public PinkTreeGeneratorBase constructTreeGenerator() {
			switch(this) {
				case TREE:
					return new PinkTreeGenerator(true);
				case GIANTTREE:
					return new GiantPinkTreeGenerator(true, true);
				case JUNGLE:
					return new RedJungleTreeGenerator(true);
			}
			return null;
		}

		public double getSaplingDropChance() { //5% base
			switch(this) {
				case TREE:
					return 6.67;
				case GIANTTREE:
					return 0.2;
				case JUNGLE:
					return 3;
			}
			return 0;
		}

		public float getHardnessMultiplier() {
			switch(this) {
				case GIANTTREE:
					return 2F;
				case JUNGLE:
					return 1.2F;
				default:
					return 1;
			}
		}

		public PinkTreeTypes getTypeDropped() {
			switch(this) {
				case GIANTTREE:
					return TREE;
				default:
					return this;
			}
		}

		public int getRenderColor(IBlockAccess world, int x, int y, int z) {
			if (this == PinkTreeTypes.GIANTTREE) {
				y -= 60; //was 18 then 24 then 50
			}
			int ret = Satisforestry.pinkforest.getBiomeFoliageColor(x, y, z);
			if (this == PinkTreeTypes.JUNGLE) {
				ret = ReikaColorAPI.getModifiedHue(ret, 355);
				ret = ReikaColorAPI.getModifiedSat(ret, 0.95F);
				ret = ReikaColorAPI.getColorWithBrightnessMultiplier(ret, 0.8F);
			}
			return ret;
		}

		@SideOnly(Side.CLIENT)
		public int getBasicRenderColor() {
			return this.getRenderColor(Minecraft.getMinecraft().theWorld, 0, 118, 0);
		}

		public static PinkTreeTypes getLeafType(IBlockAccess world, int x, int y, int z) {
			return getLeafType(world.getBlockMetadata(x, y, z));
		}

		public static PinkTreeTypes getLeafType(int meta) {
			return PinkTreeTypes.list[meta%4];
		}

		public static PinkTreeTypes getLogType(IBlockAccess world, int x, int y, int z) {
			return getLeafType(world.getBlockMetadata(x, y, z));
		}

		public static PinkTreeTypes getLogType(int meta) {
			return PinkTreeTypes.list[meta%4];
		}

		public int getCharcoalYield() {
			switch(this) {
				case JUNGLE:
					return 2;
				default:
					return 1;
			}
		}

		public float getBerryModifier() {
			switch(this) {
				case TREE:
					return 1.5F;
				case GIANTTREE:
					return 0.03F;
				case JUNGLE:
					return 0.833F;
				default:
					return 1;
			}
		}

		@Override
		public ItemStack getBaseLog() {
			return SFBlocks.LOG.getStackOfMetadata(this.ordinal());
		}

		@Override
		public ItemStack getBaseLeaf() {
			return SFBlocks.LEAVES.getStackOfMetadata(this.ordinal());
		}

		@Override
		public ItemStack getSapling() {
			return SFBlocks.SAPLING.getStackOfMetadata(this.ordinal());
		}
	}

}
