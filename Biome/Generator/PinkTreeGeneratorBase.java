package Reika.Satisforestry.Biome.Generator;

import net.minecraft.block.Block;

import Reika.DragonAPI.Instantiable.Data.Immutable.BlockKey;
import Reika.DragonAPI.Instantiable.Worldgen.ModifiableBigTree;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Blocks.BlockPinkLeaves.LeafTypes;
import Reika.Satisforestry.Registry.SFBlocks;


public abstract class PinkTreeGeneratorBase extends ModifiableBigTree {

	protected boolean forceGen;
	public final LeafTypes type;

	public PinkTreeGeneratorBase(boolean force, LeafTypes leaf) {
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

}
