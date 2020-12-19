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

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.IBlockAccess;

import Reika.DragonAPI.Interfaces.ISBRH;


public class ResourceNodeRenderer implements ISBRH {

	private static final Random rand = new Random();

	public int renderPass;

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {

	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		renderer.renderStandardBlockWithAmbientOcclusion(block, x, y, z, 1, 1, 1);
		Tessellator v5 = Tessellator.instance;
		v5.setBrightness(240);

		rand.setSeed(this.calcSeed(x, 0, z));
		rand.nextBoolean();

		int div = 8;//16;
		int half = div/2-1;
		double scale = 16D/div;
		double h = 0.375;//0.25;

		double[][] grid = new double[div-1][div-1];
		for (int i = 0; i < grid.length; i++) {
			for (int k = 0; k < grid[i].length; k++) {
				double d = Math.min(Math.abs(k-half), Math.abs(i-half))/(double)half;
				double dh = 1-d;
				grid[i][k] = rand.nextDouble()*h*dh;
			}
		}

		IIcon ico =
				v5.setColorOpaque_I(0x22aaff);

		for (int i = 0; i < div; i++) {
			for (int k = 0; k < div; k++) {
				double x1 = x+i/(double)div;
				double x2 = x1+1/(double)div;
				double z1 = z+k/(double)div;
				double z2 = z1+1/(double)div;
				double y11 = i == 0 || k == 0 ? 0 : grid[i-1][k-1];
				double y12 = i == 0 || k == div-1 ? 0 : grid[i-1][k];
				double y21 = i == div-1 || k == 0 ? 0 : grid[i][k-1];
				double y22 = i == div-1 || k == div-1 ? 0 : grid[i][k];
				v5.addVertexWithUV(x1, y+1.02+y12, z2, ico.getInterpolatedU(i*scale), ico.getInterpolatedV((k+1)*scale));
				v5.addVertexWithUV(x2, y+1.02+y22, z2, ico.getInterpolatedU((i+1)*scale), ico.getInterpolatedV((k+1)*scale));
				v5.addVertexWithUV(x2, y+1.02+y21, z1, ico.getInterpolatedU((i+1)*scale), ico.getInterpolatedV(k*scale));
				v5.addVertexWithUV(x1, y+1.02+y11, z1, ico.getInterpolatedU(i*scale), ico.getInterpolatedV(k*scale));
			}
		}

		return true;
	}

	private long calcSeed(int x, int y, int z) {
		return ChunkCoordIntPair.chunkXZ2Int(x, z) ^ y;
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
