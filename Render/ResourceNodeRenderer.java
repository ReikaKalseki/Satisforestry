/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.Satisforestry.Render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;

import Reika.DragonAPI.Interfaces.ISBRH;
import Reika.Satisforestry.Blocks.BlockDecoration.DecorationType;


public class ResourceNodeRenderer implements ISBRH {

	public int renderPass;

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {

	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		DecorationType f = DecorationType.list[world.getBlockMetadata(x, y, z)];
		Tessellator.instance.setColorOpaque_I(0xffffff);
		f.render(world, x, y, z, block, renderer, Tessellator.instance);
		return true;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return false;
	}

	@Override
	public int getRenderId() {
		return 0;
	}

}
