package Reika.Satisforestry.Biome.Generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Data.Immutable.BlockKey;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Instantiable.Worldgen.ModifiableBigTree;
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

	private int topLeaf = -1;
	private int bottomLeaf = 256;

	private final HashMap<Coordinate, Integer> logs = new HashMap();
	private final HashMap<Coordinate, Integer> leavesTop = new HashMap();

	public PinkTreeGeneratorBase(boolean force, PinkTreeTypes leaf) {
		super(false);
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
		if (rand.nextFloat() < this.getTreeTopSlugChance()) {
			TilePowerSlug te = null;
			ArrayList<Entry<Coordinate, Integer>> set = new ArrayList(leavesTop.keySet());
			while (te == null && !set.isEmpty()) {
				int i = rand.nextInt(set.size());
				Entry<Coordinate, Integer> e = set.remove(i);
				Coordinate c = e.getKey();
				int dy = e.getValue();
				int reach = (dy-generationOriginY)/32;
				int tier = 0;
				if (reach >= 64) {
					float f = (reach-64)/64F;
					tier = rand.nextFloat() < f ? 3 : 2;
				}
				else if (reach >= 24) {
					float f = (reach-24)/40F;
					tier = rand.nextFloat() < f*0.9 ? 2 : 1;
				}
				te = BlockPowerSlug.generatePowerSlugAt(world, c.xCoord, dy, c.zCoord, rand, tier, false, MathHelper.clamp_int(reach, 0, 3), true);
			}
		}
		for (Entry<Coordinate, Integer> e : logs.entrySet()) {
			if (e.getValue() < 4) {
				Coordinate c = e.getKey();
				int[] trunk = this.getTrunkRange();
				if (c.yCoord >= trunk[0] && c.yCoord <= trunk[1]) {
					if (rand.nextFloat() < this.getTrunkSlugChancePerBlock()) {

					}
				}
			}
			else {

			}
		}
	}

	@Override
	protected void setBlockAndNotifyAdequately(World world, int x, int y, int z, Block b, int meta) {
		super.setBlockAndNotifyAdequately(world, x, y, z, b, meta);
		if (b == SFBlocks.LOG.getBlockInstance()) {
			logs.put(new Coordinate(x, y, z), meta);
		}
		else if (b == SFBlocks.LEAVES.getBlockInstance()) {
			Coordinate c = new Coordinate(x, 0, z);
			Integer get = leavesTop.get(c);
			if (get == null || get.intValue() < y)
				leavesTop.put(c, y);
			topLeaf = Math.max(y, topLeaf);
			bottomLeaf = Math.min(y, bottomLeaf);
		}
	}

	protected abstract float getTrunkSlugChancePerBlock();

	protected abstract float getTreeTopSlugChance();

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
					return 8;
				case GIANTTREE:
					return 0.2;
				case JUNGLE:
					return 4;
			}
			return 0;
		}

		public PinkTreeTypes getTypeDropped() {
			switch(this) {
				case GIANTTREE:
					return TREE;
				default:
					return this;
			}
		}

		@SideOnly(Side.CLIENT)
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
	}

}
