package Reika.Satisforestry.Biome.Generator;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.world.IBlockAccess;

import Reika.DragonAPI.Instantiable.Data.Immutable.BlockKey;
import Reika.DragonAPI.Instantiable.Worldgen.ModifiableBigTree;
import Reika.DragonAPI.Libraries.Rendering.ReikaColorAPI;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Registry.SFBlocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


public abstract class PinkTreeGeneratorBase extends ModifiableBigTree {

	protected boolean forceGen;
	public final PinkTreeTypes type;

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

	public static enum PinkTreeTypes {
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

		public String getDisplayName(String base) {
			switch(this) {
				case TREE:
					return "Pink "+base;
				case GIANTTREE:
					return "Giant Pink "+base;
				case JUNGLE:
					return "Red "+base;
			}
			return base;
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

		public double getSaplingDropChance() {
			switch(this) {
				case TREE:
					return 0.08;
				case GIANTTREE:
					return 0.002;
				case JUNGLE:
					return 0.04;
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
	}

}
