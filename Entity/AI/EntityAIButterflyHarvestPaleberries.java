package Reika.Satisforestry.Entity.AI;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Blocks.BlockPinkGrass.GrassTypes;
import Reika.Satisforestry.Registry.SFBlocks;

import forestry.api.lepidopterology.IEntityButterfly;

public class EntityAIButterflyHarvestPaleberries extends EntityAIButterflyPaleberry {

	public EntityAIButterflyHarvestPaleberries(IEntityButterfly b) {
		super(b);
	}

	@Override
	public boolean continueExecuting() {
		return target != null && this.isValidToStart() && this.isValidTarget(target.xCoord, target.yCoord, target.zCoord);
	}

	@Override
	protected boolean isValidToStart() {
		return butterfly.getExhaustion() >= 25000;
	}

	@Override
	protected boolean isValidTarget(int x, int y, int z) {
		return world.getBlock(x, y, z) == SFBlocks.GRASS.getBlockInstance() && world.getBlockMetadata(x, y, z) == GrassTypes.PALEBERRY_NEW.ordinal();
	}

	@Override
	public void updateTask() {
		super.updateTask();
		int x = MathHelper.floor_double(butterfly.getEntity().posX);
		int y = MathHelper.floor_double(butterfly.getEntity().posY-0.5);
		int z = MathHelper.floor_double(butterfly.getEntity().posZ);
		if (this.isValidTarget(x, y, z)) {
			butterfly.changeExhaustion(-10000);
			ReikaSoundHelper.playBreakSound(world, x, y, z, Blocks.leaves, 0.7F, 0.25F);
			world.setBlockMetadataWithNotify(x, y, z, GrassTypes.PALEBERRY_EMPTY.ordinal(), 2);
			ReikaItemHelper.dropItem(butterfly.getEntity(), new ItemStack(Satisforestry.paleberry));
			target = null;
		}
	}

	@Override
	protected boolean needsPollen() {
		return false;
	}

}
