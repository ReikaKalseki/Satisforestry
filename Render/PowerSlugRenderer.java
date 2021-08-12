package Reika.Satisforestry.Render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

import Reika.DragonAPI.Base.ISBRH;

public class PowerSlugRenderer extends ISBRH {

	public PowerSlugRenderer(int id) {
		super(id);
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks rb) {
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block b, int modelId, RenderBlocks rb) {
		boolean flag = false;
		return flag;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return true;
	}

}
