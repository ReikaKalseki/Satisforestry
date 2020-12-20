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
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.IBlockAccess;

import Reika.DragonAPI.Interfaces.ISBRH;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;


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
		v5.setColorOpaque_I(0xffffff);

		rand.setSeed(this.calcSeed(x, y, z));
		rand.nextBoolean();

		IIcon ico = Blocks.bedrock.blockIcon;

		int n = ReikaRandomHelper.getRandomBetween(5, 9, rand);
		double maxr = 2.25;

		double da = 360D/n;
		for (int i = 0; i < n; i++) {
			double h = ReikaRandomHelper.getRandomBetween(0.0625, 0.125, rand);
			double dy = y+1+h;
			boolean split = rand.nextInt(3) > 0;
			double r1 = ReikaRandomHelper.getRandomBetween(0.25, 0.625, rand);
			double r2 = Math.max(r1+0.5, ReikaRandomHelper.getRandomBetween(1.75, maxr, rand));
			double f = ReikaRandomHelper.getRandomBetween(0.625, 0.875, rand);
			double aw = da*f/2D;
			double oa = (1-f)*da/2D;
			double a0 = da*i+oa;
			double a1 = Math.toRadians(a0-aw);
			double a2 = Math.toRadians(a0+aw);
			double x1 = r1*Math.cos(a1);
			double x2 = r1*Math.cos(a2);
			double x3 = r2*Math.cos(a2);
			double x4 = r2*Math.cos(a1);
			double z1 = r1*Math.sin(a1);
			double z2 = r1*Math.sin(a2);
			double z3 = r2*Math.sin(a2);
			double z4 = r2*Math.sin(a1);
			if (split) {
				double xa = Math.cos(Math.toRadians(a0));
				double za = Math.sin(Math.toRadians(a0));

				this.addVertexAt(v5, x, dy, z, maxr, ico, x1, z1);
				this.addVertexAt(v5, x, dy, z, maxr, ico, xa*r1, za*r1);
				this.addVertexAt(v5, x, dy, z, maxr, ico, xa*r2, za*r2);
				this.addVertexAt(v5, x, dy, z, maxr, ico, x4, z4);

				this.addVertexAt(v5, x, dy, z, maxr, ico, xa*r1, za*r1);
				this.addVertexAt(v5, x, dy, z, maxr, ico, x2, z2);
				this.addVertexAt(v5, x, dy, z, maxr, ico, x3, z3);
				this.addVertexAt(v5, x, dy, z, maxr, ico, xa*r2, za*r2);


				this.addVertexAt(v5, x, y+1, z, maxr, ico, x1, z1);
				this.addVertexAt(v5, x, y+1, z, maxr, ico, xa*r1, za*r1);
				this.addVertexAt(v5, x, dy, z, maxr, ico, xa*r1, za*r1);
				this.addVertexAt(v5, x, dy, z, maxr, ico, x1, z1);

				this.addVertexAt(v5, x, y+1, z, maxr, ico, xa*r1, za*r1);
				this.addVertexAt(v5, x, y+1, z, maxr, ico, x2, z2);
				this.addVertexAt(v5, x, dy, z, maxr, ico, x2, z2);
				this.addVertexAt(v5, x, dy, z, maxr, ico, xa*r1, za*r1);

				this.addVertexAt(v5, x, dy, z, maxr, ico, x4, z4);
				this.addVertexAt(v5, x, dy, z, maxr, ico, xa*r2, za*r2);
				this.addVertexAt(v5, x, y+1, z, maxr, ico, xa*r2, za*r2);
				this.addVertexAt(v5, x, y+1, z, maxr, ico, x4, z4);

				this.addVertexAt(v5, x, dy, z, maxr, ico, xa*r2, za*r2);
				this.addVertexAt(v5, x, dy, z, maxr, ico, x3, z3);
				this.addVertexAt(v5, x, y+1, z, maxr, ico, x3, z3);
				this.addVertexAt(v5, x, y+1, z, maxr, ico, xa*r2, za*r2);

				this.addVertexAt(v5, x, y+1, z, maxr, ico, x1, z1);
				this.addVertexAt(v5, x, dy, z, maxr, ico, x1, z1);
				this.addVertexAt(v5, x, dy, z, maxr, ico, x4, z4);
				this.addVertexAt(v5, x, y+1, z, maxr, ico, x4, z4);

				this.addVertexAt(v5, x, y+1, z, maxr, ico, x3, z3);
				this.addVertexAt(v5, x, dy, z, maxr, ico, x3, z3);
				this.addVertexAt(v5, x, dy, z, maxr, ico, x2, z2);
				this.addVertexAt(v5, x, y+1, z, maxr, ico, x2, z2);
			}
			else {
				this.addVertexAt(v5, x, dy, z, maxr, ico, x1, z1);
				this.addVertexAt(v5, x, dy, z, maxr, ico, x2, z2);
				this.addVertexAt(v5, x, dy, z, maxr, ico, x3, z3);
				this.addVertexAt(v5, x, dy, z, maxr, ico, x4, z4);

				this.addVertexAt(v5, x, y+1, z, maxr, ico, x1, z1);
				this.addVertexAt(v5, x, y+1, z, maxr, ico, x2, z2);
				this.addVertexAt(v5, x, dy, z, maxr, ico, x2, z2);
				this.addVertexAt(v5, x, dy, z, maxr, ico, x1, z1);

				this.addVertexAt(v5, x, dy, z, maxr, ico, x4, z4);
				this.addVertexAt(v5, x, dy, z, maxr, ico, x3, z3);
				this.addVertexAt(v5, x, y+1, z, maxr, ico, x3, z3);
				this.addVertexAt(v5, x, y+1, z, maxr, ico, x4, z4);

				this.addVertexAt(v5, x, y+1, z, maxr, ico, x1, z1);
				this.addVertexAt(v5, x, dy, z, maxr, ico, x1, z1);
				this.addVertexAt(v5, x, dy, z, maxr, ico, x4, z4);
				this.addVertexAt(v5, x, y+1, z, maxr, ico, x4, z4);

				this.addVertexAt(v5, x, y+1, z, maxr, ico, x3, z3);
				this.addVertexAt(v5, x, dy, z, maxr, ico, x3, z3);
				this.addVertexAt(v5, x, dy, z, maxr, ico, x2, z2);
				this.addVertexAt(v5, x, y+1, z, maxr, ico, x2, z2);
			}
		}

		v5.setBrightness(240);
		int div = 8;//16;
		int half = div/2-1;
		double scale = 16D/div;
		double h = 0.25;

		double size = 1;
		double minX = x+0.5-size/2;
		double maxX = x+0.5+size/2;
		double minZ = z+0.5-size/2;
		double maxZ = z+0.5+size/2;

		double[][] grid = new double[div-1][div-1];
		for (int i = 0; i < grid.length; i++) {
			for (int k = 0; k < grid[i].length; k++) {
				double d = Math.min(Math.abs(k-half), Math.abs(i-half))/(double)half;
				double dh = 1-d;
				grid[i][k] = rand.nextDouble()*h*dh;
			}
		}

		ico = Blocks.snow.blockIcon;
		v5.setColorOpaque_I(0x22aaff);

		for (int i = 0; i < div; i++) {
			for (int k = 0; k < div; k++) {
				double x1 = minX+i/(double)div;
				double x2 = x1+size/div;
				double z1 = minZ+k/(double)div;
				double z2 = z1+size/div;
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

	private void addVertexAt(Tessellator v5, int x, double y, int z, double maxr, IIcon ico, double dx, double dz) {
		double x2 = x+0.5+dx;
		double z2 = z+0.5+dz;
		double x0 = x+0.5-maxr;
		double z0 = z+0.5-maxr;
		double fx = (x2-x0)/(maxr*2);//ReikaMathLibrary.getDecimalPart(x2);
		double fz = (z2-z0)/(maxr*2);//ReikaMathLibrary.getDecimalPart(z2);
		double u = ico.getInterpolatedU(fx*16+0.01);
		double v = ico.getInterpolatedV(fz*16+0.01); //tiny offset is to avoid the selection lying right at the boundary between two px and giving flicker
		//v5.setColorOpaque_F((float)fx, 0, (float)fz);
		v5.addVertexWithUV(x2, y, z2, u, v);
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
