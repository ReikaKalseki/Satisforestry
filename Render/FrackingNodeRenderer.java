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

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import Reika.DragonAPI.Base.ISBRH;
import Reika.DragonAPI.Instantiable.Rendering.StructureRenderer;
import Reika.DragonAPI.Libraries.Rendering.ReikaColorAPI;
import Reika.Satisforestry.Blocks.BlockFrackingNode;
import Reika.Satisforestry.Blocks.BlockFrackingNode.TileFrackingNode;
import Reika.Satisforestry.Config.ResourceFluid;


public class FrackingNodeRenderer extends ISBRH {

	public FrackingNodeRenderer(int id) {
		super(id);
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
		Tessellator v5 = Tessellator.instance;

		GL11.glColor4f(1, 1, 1, 1);
		GL11.glDisable(GL11.GL_LIGHTING);

		GL11.glPushMatrix();
		GL11.glRotated(45, 0, 1, 0);
		GL11.glRotated(-30, 1, 0, 0);
		double s = 1.6;
		GL11.glScaled(s, s, s);
		double x = -0.5;
		double y = -0.5;
		double z = 0;

		GL11.glTranslated(x, y, z);
		v5.startDrawingQuads();
		v5.setColorOpaque_I(0xffffff);
		v5.setBrightness(240);

		IIcon ico = BlockFrackingNode.getItem();
		float u = ico.getMinU();
		float v = ico.getMinV();
		float du = ico.getMaxU();
		float dv = ico.getMaxV();

		v5.addVertexWithUV(0, 0, 0, u, dv);
		v5.addVertexWithUV(1, 0, 0, du, dv);
		v5.addVertexWithUV(1, 1, 0, du, v);
		v5.addVertexWithUV(0, 1, 0, u, v);

		v5.draw();
		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_LIGHTING);
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		Tessellator v5 = Tessellator.instance;
		TileFrackingNode te = (TileFrackingNode)world.getTileEntity(x, y, z);
		ResourceFluid ri = te.getResource();
		int c = ri == null ? 0xffffff : ri.color;
		if (renderPass == 0 || StructureRenderer.isRenderingTiles()) {
			v5.setColorOpaque_I(0xffffff);
			renderer.renderStandardBlockWithAmbientOcclusion(block, x, y, z, 1, 1, 1);
		}
		if (renderPass == 1) {
			v5.setBrightness(240);

			World w = Minecraft.getMinecraft().theWorld;
			float l = Math.max(w.getSavedLightValue(EnumSkyBlock.Block, x, y+1, z), w.getSavedLightValue(EnumSkyBlock.Sky, x, y+1, z)*w.getSunBrightnessFactor(0));
			float a = 1-l/24F;
			if (a < 1) {
				c = ReikaColorAPI.mixColors(c, 0xffffff, a*0.5F+0.5F);
			}

			v5.setColorRGBA_I(c, (int)(a*255));
		}

		rand.setSeed(this.calcSeed(x, y, z));
		rand.nextBoolean();

		IIcon ico = renderPass == 1 ? Blocks.diamond_block.blockIcon : block.blockIcon;

		if (renderPass == 1 || StructureRenderer.isRenderingTiles()) {
			v5.setColorOpaque_I(c);
			ico = Blocks.brick_block.blockIcon;
		}

		return renderPass == 0;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return true;
	}

}
