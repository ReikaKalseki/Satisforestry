package Reika.Satisforestry.Entity.AI;

import net.minecraft.world.World;

import Reika.Satisforestry.Blocks.BlockPinkGrass.GrassTypes;
import Reika.Satisforestry.Registry.SFBlocks;
import Reika.Satisforestry.Registry.SFOptions;

import forestry.api.lepidopterology.IEntityButterfly;

public class EntityAIButterflyPollinatePaleberries extends EntityAIButterflyPaleberry {

	public EntityAIButterflyPollinatePaleberries(IEntityButterfly b) {
		super(b);
	}

	@Override
	public boolean continueExecuting() {
		if (target != null && this.isValidToStart() && this.isValidTarget(world, target.xCoord, target.yCoord, target.zCoord)) {
			if (SFOptions.PALEBERRYPOLLEN.getState() && !this.checkPollen()) {
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	protected boolean isValidToStart() {
		return butterfly.getExhaustion() < 20000 && !butterfly.getEntity().onGround;
	}

	@Override
	protected boolean isValidTarget(int x, int y, int z) {
		return isValidTarget(world, x, y, z);
	}

	public static boolean isValidTarget(World world, int x, int y, int z) {
		return world.getBlock(x, y, z) == SFBlocks.GRASS.getBlockInstance() && world.getBlockMetadata(x, y, z) == GrassTypes.PALEBERRY_EMPTY.ordinal();
	}

	@Override
	public void updateTask() {
		super.updateTask();
		butterfly.changeExhaustion(3);
	}

	@Override
	protected boolean needsPollen() {
		return true;
	}

}
