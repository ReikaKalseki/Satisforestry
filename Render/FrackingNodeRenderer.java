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
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import Reika.DragonAPI.Base.ISBRH;
import Reika.DragonAPI.Instantiable.Rendering.StructureRenderer;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.Satisforestry.Blocks.BlockFrackingAux.TileFrackingAux;
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
		TileEntity te = world.getTileEntity(x, y, z);
		ResourceFluid ri = null;
		if (te instanceof TileFrackingNode) {
			ri = ((TileFrackingNode)te).getResource();
		}
		else if (te instanceof TileFrackingAux) {
			TileFrackingNode te2 = ((TileFrackingAux)te).getMaster();
			if (te2 != null)
				ri = te2.getResource();
		}
		int c = ri == null ? 0xffffff : ri.color;
		if (renderPass == 0 || StructureRenderer.isRenderingTiles()) {
			v5.setColorOpaque_I(0xffffff);
			renderer.renderStandardBlockWithAmbientOcclusion(block, x, y, z, 1, 1, 1);
		}
		else {
			v5.setColorOpaque_I(c);
		}
		v5.setBrightness(block.getMixedBrightnessForBlock(world, x, y+1, z));

		rand.setSeed(this.calcSeed(x, y, z));
		rand.nextBoolean();

		IIcon ico = renderPass == 1 ? BlockFrackingNode.getOverlay() : block.blockIcon;

		int n = ReikaRandomHelper.getRandomBetween(5, 9, rand);
		double minr = 2.25;
		double maxr = 3;
		double r1 = ReikaRandomHelper.getRandomBetween(0.125, 0.25, rand);
		double r2 = ReikaRandomHelper.getRandomBetween(minr, maxr, rand);
		double dr = this.getRadiusScale();
		r1 *= dr;
		r2 *= dr;
		double maxh = ReikaRandomHelper.getRandomBetween(0.375, 0.5, rand);
		double minh = 0.0625;//ReikaRandomHelper.getRandomBetween(0.0625, 0.125, rand);
		maxh *= dr;
		minh *= dr*dr;
		double oo = 0.09375;
		double f = ReikaRandomHelper.getRandomBetween(0.875, 0.9375, rand);
		this.renderWedgePie(x, y, z, n, r1, r2, minh, maxh, oo, f, v5, ico);

		return true;
	}

	protected void renderWedgePie(double x, double y, double z, int n, double r1, double r2, double minh, double maxh, double oo, double f, Tessellator v5, IIcon ico) {
		double da = 360D/n;
		for (int i = 0; i < n; i++) {
			double aw = da*f/2D;
			double oa = (1-f)*da/2D;
			double a0 = da*i+oa;
			double a1 = Math.toRadians(a0-aw);
			double a2 = Math.toRadians(a0+aw);

			double x1 = r1*Math.cos(a1)+ReikaRandomHelper.getRandomPlusMinus(0, oo, rand);
			double x2 = r1*Math.cos(a2)+ReikaRandomHelper.getRandomPlusMinus(0, oo, rand);
			double x3 = r2*Math.cos(a2)+ReikaRandomHelper.getRandomPlusMinus(0, oo, rand);
			double x4 = r2*Math.cos(a1)+ReikaRandomHelper.getRandomPlusMinus(0, oo, rand);
			double z1 = r1*Math.sin(a1)+ReikaRandomHelper.getRandomPlusMinus(0, oo, rand);
			double z2 = r1*Math.sin(a2)+ReikaRandomHelper.getRandomPlusMinus(0, oo, rand);
			double z3 = r2*Math.sin(a2)+ReikaRandomHelper.getRandomPlusMinus(0, oo, rand);
			double z4 = r2*Math.sin(a1)+ReikaRandomHelper.getRandomPlusMinus(0, oo, rand);

			double xm = Math.cos(Math.toRadians(a0));
			double zm = Math.sin(Math.toRadians(a0));

			double xa = xm*r1+ReikaRandomHelper.getRandomPlusMinus(0, oo, rand);
			double xb = xm*r2+ReikaRandomHelper.getRandomPlusMinus(0, oo, rand);
			double za = zm*r1+ReikaRandomHelper.getRandomPlusMinus(0, oo, rand);
			double zb = zm*r2+ReikaRandomHelper.getRandomPlusMinus(0, oo, rand);

			this.addVertexAt(v5, x, y, z, minh, maxh, r2, ico, x1, z1);
			this.addVertexAt(v5, x, y, z, minh, maxh, r2, ico, xa, za);
			this.addVertexAt(v5, x, y, z, minh, maxh, r2, ico, xb, zb);
			this.addVertexAt(v5, x, y, z, minh, maxh, r2, ico, x4, z4);

			this.addVertexAt(v5, x, y, z, minh, maxh, r2, ico, xa, za);
			this.addVertexAt(v5, x, y, z, minh, maxh, r2, ico, x2, z2);
			this.addVertexAt(v5, x, y, z, minh, maxh, r2, ico, x3, z3);
			this.addVertexAt(v5, x, y, z, minh, maxh, r2, ico, xb, zb);

			this.addVertexAt(v5, x, y, z, 0, 0, r2, ico, x1, z1);
			this.addVertexAt(v5, x, y, z, 0, 0, r2, ico, xa, za);
			this.addVertexAt(v5, x, y, z, minh, maxh, r2, ico, xa, za);
			this.addVertexAt(v5, x, y, z, minh, maxh, r2, ico, x1, z1);

			this.addVertexAt(v5, x, y, z, 0, 0, r2, ico, xa, za);
			this.addVertexAt(v5, x, y, z, 0, 0, r2, ico, x2, z2);
			this.addVertexAt(v5, x, y, z, minh, maxh, r2, ico, x2, z2);
			this.addVertexAt(v5, x, y, z, minh, maxh, r2, ico, xa, za);

			this.addVertexAt(v5, x, y, z, minh, maxh, r2, ico, x4, z4);
			this.addVertexAt(v5, x, y, z, minh, maxh, r2, ico, xb, zb);
			this.addVertexAt(v5, x, y, z, 0, 0, r2, ico, xb, zb);
			this.addVertexAt(v5, x, y, z, 0, 0, r2, ico, x4, z4);

			this.addVertexAt(v5, x, y, z, minh, maxh, r2, ico, xb, zb);
			this.addVertexAt(v5, x, y, z, minh, maxh, r2, ico, x3, z3);
			this.addVertexAt(v5, x, y, z, 0, 0, r2, ico, x3, z3);
			this.addVertexAt(v5, x, y, z, 0, 0, r2, ico, xb, zb);

			this.addVertexAt(v5, x, y, z, 0, 0, r2, ico, x1, z1);
			this.addVertexAt(v5, x, y, z, minh, maxh, r2, ico, x1, z1);
			this.addVertexAt(v5, x, y, z, minh, maxh, r2, ico, x4, z4);
			this.addVertexAt(v5, x, y, z, 0, 0, r2, ico, x4, z4);

			this.addVertexAt(v5, x, y, z, 0, 0, r2, ico, x3, z3);
			this.addVertexAt(v5, x, y, z, minh, maxh, r2, ico, x3, z3);
			this.addVertexAt(v5, x, y, z, minh, maxh, r2, ico, x2, z2);
			this.addVertexAt(v5, x, y, z, 0, 0, r2, ico, x2, z2);
		}
	}

	protected double getRadiusScale() {
		return 1;
	}

	private void addVertexAt(Tessellator v5, double x, double y, double z, double minh, double maxh, double maxr, IIcon ico, double dx, double dz) {
		double x2 = x+0.5+dx;
		double z2 = z+0.5+dz;
		double x0 = x+0.5-maxr;
		double z0 = z+0.5-maxr;
		double fx = (x2-x0)/(maxr*2);//ReikaMathLibrary.getDecimalPart(x2);
		double fz = (z2-z0)/(maxr*2);//ReikaMathLibrary.getDecimalPart(z2);
		double dr = ReikaMathLibrary.py3d(dx, 0, dz);
		double u = ico.getInterpolatedU(fx*16+0.01);
		double v = ico.getInterpolatedV(fz*16+0.01); //tiny offset is to avoid the selection lying right at the boundary between two px and giving flicker
		double dy = maxh <= 0.03 ? y+1 : y+1+ReikaMathLibrary.linterpolate(dr, 0, maxr, maxh, minh);
		v5.addVertexWithUV(x2, dy, z2, u, v);
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return true;
	}

}
