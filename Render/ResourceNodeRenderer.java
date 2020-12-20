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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Math.Noise.SimplexNoiseGenerator;
import Reika.DragonAPI.Interfaces.ISBRH;
import Reika.DragonAPI.Libraries.IO.ReikaColorAPI;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.Satisforestry.SFOptions;
import Reika.Satisforestry.Blocks.BlockResourceNode;


public class ResourceNodeRenderer implements ISBRH {

	private static final Random rand = new Random();

	public static int renderPass;

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {

	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		Tessellator v5 = Tessellator.instance;
		int c = SFOptions.RESOURCECOLOR.getValue();
		if (renderPass == 0) {
			v5.setColorOpaque_I(0xffffff);
			renderer.renderStandardBlockWithAmbientOcclusion(block, x, y, z, 1, 1, 1);
		}
		else {
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

		IIcon ico = renderPass == 1 ? BlockResourceNode.getOverlay() : block.blockIcon;

		int n = ReikaRandomHelper.getRandomBetween(5, 9, rand);
		double minr = 1.75;
		double maxr = 2.25;

		double da = 360D/n;
		for (int i = 0; i < n; i++) {
			double ox = ReikaRandomHelper.getRandomPlusMinus(0, 0.09375, rand);
			double oz = ReikaRandomHelper.getRandomPlusMinus(0, 0.09375, rand);
			double h = ReikaRandomHelper.getRandomBetween(0.0625, 0.1875, rand);
			double dy = y+1+h;
			boolean split = rand.nextInt(3) > 0;
			double r1 = ReikaRandomHelper.getRandomBetween(0.25, 0.625, rand);
			double r2 = Math.max(r1+0.5, ReikaRandomHelper.getRandomBetween(minr, maxr, rand));
			double f = ReikaRandomHelper.getRandomBetween(0.625, 0.875, rand);
			double aw = da*f/2D;
			double oa = (1-f)*da/2D;
			double a0 = da*i+oa;
			double a1 = Math.toRadians(a0-aw);
			double a2 = Math.toRadians(a0+aw);

			double oo = 0.09375;
			double x1 = r1*Math.cos(a1)+ox+ReikaRandomHelper.getRandomPlusMinus(0, oo, rand);
			double x2 = r1*Math.cos(a2)+ox+ReikaRandomHelper.getRandomPlusMinus(0, oo, rand);
			double x3 = r2*Math.cos(a2)+ox+ReikaRandomHelper.getRandomPlusMinus(0, oo, rand);
			double x4 = r2*Math.cos(a1)+ox+ReikaRandomHelper.getRandomPlusMinus(0, oo, rand);
			double z1 = r1*Math.sin(a1)+oz+ReikaRandomHelper.getRandomPlusMinus(0, oo, rand);
			double z2 = r1*Math.sin(a2)+oz+ReikaRandomHelper.getRandomPlusMinus(0, oo, rand);
			double z3 = r2*Math.sin(a2)+oz+ReikaRandomHelper.getRandomPlusMinus(0, oo, rand);
			double z4 = r2*Math.sin(a1)+oz+ReikaRandomHelper.getRandomPlusMinus(0, oo, rand);

			if (split) {
				double xm = Math.cos(Math.toRadians(a0));
				double zm = Math.sin(Math.toRadians(a0));

				double xa = xm*r1+ox+ReikaRandomHelper.getRandomPlusMinus(0, oo, rand);
				double xb = xm*r2+ox+ReikaRandomHelper.getRandomPlusMinus(0, oo, rand);
				double za = zm*r1+oz+ReikaRandomHelper.getRandomPlusMinus(0, oo, rand);
				double zb = zm*r2+oz+ReikaRandomHelper.getRandomPlusMinus(0, oo, rand);

				this.addVertexAt(v5, x, dy, z, maxr, ico, x1, z1);
				this.addVertexAt(v5, x, dy, z, maxr, ico, xa, za);
				this.addVertexAt(v5, x, dy, z, maxr, ico, xb, zb);
				this.addVertexAt(v5, x, dy, z, maxr, ico, x4, z4);

				this.addVertexAt(v5, x, dy, z, maxr, ico, xa, za);
				this.addVertexAt(v5, x, dy, z, maxr, ico, x2, z2);
				this.addVertexAt(v5, x, dy, z, maxr, ico, x3, z3);
				this.addVertexAt(v5, x, dy, z, maxr, ico, xb, zb);

				this.addVertexAt(v5, x, y+1, z, maxr, ico, x1, z1);
				this.addVertexAt(v5, x, y+1, z, maxr, ico, xa, za);
				this.addVertexAt(v5, x, dy, z, maxr, ico, xa, za);
				this.addVertexAt(v5, x, dy, z, maxr, ico, x1, z1);

				this.addVertexAt(v5, x, y+1, z, maxr, ico, xa, za);
				this.addVertexAt(v5, x, y+1, z, maxr, ico, x2, z2);
				this.addVertexAt(v5, x, dy, z, maxr, ico, x2, z2);
				this.addVertexAt(v5, x, dy, z, maxr, ico, xa, za);

				this.addVertexAt(v5, x, dy, z, maxr, ico, x4, z4);
				this.addVertexAt(v5, x, dy, z, maxr, ico, xb, zb);
				this.addVertexAt(v5, x, y+1, z, maxr, ico, xb, zb);
				this.addVertexAt(v5, x, y+1, z, maxr, ico, x4, z4);

				this.addVertexAt(v5, x, dy, z, maxr, ico, xb, zb);
				this.addVertexAt(v5, x, dy, z, maxr, ico, x3, z3);
				this.addVertexAt(v5, x, y+1, z, maxr, ico, x3, z3);
				this.addVertexAt(v5, x, y+1, z, maxr, ico, xb, zb);

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

		if (renderPass == 1) {
			v5.setColorOpaque_I(c);
			ico = BlockResourceNode.getCrystal();

			SimplexNoiseGenerator gen = (SimplexNoiseGenerator)new SimplexNoiseGenerator(rand.nextLong()).setFrequency(12);

			n = 8;
			double dd = 1D/n;
			double r = ReikaMathLibrary.roundToNearestFraction(ReikaRandomHelper.getRandomBetween(minr-0.5, minr-0.25, rand), dd);
			double h = 0.4;
			double dhl = 0.15;
			double oy = 0.995;

			for (double i = -r; i <= r; i += dd) {
				for (double k = -r; k <= r; k += dd) {
					double dh11 = Math.max(-0.5, -dhl*ReikaMathLibrary.py3d(i, 0, k));
					double dh12 = Math.max(-0.5, -dhl*ReikaMathLibrary.py3d(i, 0, k+dd));
					double dh21 = Math.max(-0.5, -dhl*ReikaMathLibrary.py3d(i+dd, 0, k));
					double dh22 = Math.max(-0.5, -dhl*ReikaMathLibrary.py3d(i+dd, 0, k+dd));
					/*
				double y11 = rand.nextDouble()*h*dh11;
				double y12 = rand.nextDouble()*h*dh12;
				double y21 = rand.nextDouble()*h*dh21;
				double y22 = rand.nextDouble()*h*dh22;
					 */
					double x1 = x+0.5+i;
					double x2 = x+0.5+i+dd;
					double z1 = z+0.5+k;
					double z2 = z+0.5+k+dd;

					double y11 = ReikaMathLibrary.normalizeToBounds(gen.getValue(x1, z1), 0, 1)*h+dh11;
					double y12 = ReikaMathLibrary.normalizeToBounds(gen.getValue(x1, z2), 0, 1)*h+dh12;
					double y21 = ReikaMathLibrary.normalizeToBounds(gen.getValue(x2, z1), 0, 1)*h+dh21;
					double y22 = ReikaMathLibrary.normalizeToBounds(gen.getValue(x2, z2), 0, 1)*h+dh22;

					double u1 = ico.getInterpolatedU((i+r)*16D/(r*2));
					double u2 = Math.min(ico.getMaxU(), ico.getInterpolatedU((i+dd+r)*16D/(r*2)));
					double v1 = ico.getInterpolatedV((k+r)*16D/(r*2));
					double v2 = Math.min(ico.getMaxV(), ico.getInterpolatedV((k+dd+r)*16D/(r*2)));

					if (rand.nextBoolean()) {
						double s = u2;
						u2 = u1;
						u1 = s;
					}
					if (rand.nextBoolean()) {
						double s = v2;
						v2 = v1;
						v1 = s;
					}

					v5.addVertexWithUV(x1, y+oy+y12, z2, u1, v2);
					v5.addVertexWithUV(x2, y+oy+y22, z2, u2, v2);
					v5.addVertexWithUV(x2, y+oy+y21, z1, u2, v1);
					v5.addVertexWithUV(x1, y+oy+y11, z1, u1, v1);
				}
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
