package Reika.Satisforestry.Render;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.Base.ISBRH;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.Rendering.ReikaColorAPI;
import Reika.Satisforestry.Blocks.BlockRedBamboo;

public class RedBambooRenderer extends ISBRH {

	private final Random itemRand = new Random();

	private final Random randY = new Random();

	public RedBambooRenderer(int id) {
		super(id);
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks rb) {
		itemRand.setSeed(0);
		itemRand.nextBoolean();
		Tessellator v5 = Tessellator.instance;
		int color = ReikaColorAPI.mixColors(0x964335, 0xCC705B, itemRand.nextFloat());
		IIcon[] icons = new IIcon[3];
		icons[0] = block.getIcon(2, 0);
		icons[1] = BlockRedBamboo.getRandomLeaf(itemRand);
		icons[2] = BlockRedBamboo.getRandomLeaf(itemRand);

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
		v5.setColorOpaque_I(color);
		v5.setBrightness(240);

		for (int i = 0; i < icons.length; i++) {
			IIcon ico = icons[i];
			float u = ico.getMinU();
			float v = ico.getMinV();
			float du = ico.getMaxU();
			float dv = ico.getMaxV();

			double x0 = 0;
			double x1 = 1;
			switch(i) {
				case 1:
					x0 = 0.5;
					break;
				case 2:
					x1 = 0.5;
					float dd = u;
					u = du;
					du = dd;
					break;
			}

			v5.addVertexWithUV(x0, 0, 0, u, dv);
			v5.addVertexWithUV(x1, 0, 0, du, dv);
			v5.addVertexWithUV(x1, 1, 0, du, v);
			v5.addVertexWithUV(x0, 1, 0, u, v);
		}

		v5.draw();
		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_LIGHTING);
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block b, int modelId, RenderBlocks rb) {
		//rb.setRenderBoundsFromBlock(b);
		//rb.renderStandardBlockWithAmbientOcclusion(b, x, y, z, 1, 1, 1);
		int meta = world.getBlockMetadata(x, y, z);
		Tessellator v5 = Tessellator.instance;
		//double s = b.getBlockBoundsMaxX()-b.getBlockBoundsMinX();
		rand.setSeed(this.calcSeed(x, 0, z));
		rand.nextBoolean();
		randY.setSeed(this.calcSeed(x, y, z));
		randY.nextBoolean();

		int n = 1;
		if (rand.nextInt(4) == 0)
			n++;
		if (rand.nextInt(4) == 0)
			n++;

		v5.setBrightness(b.getMixedBrightnessForBlock(world, x, y, z));
		v5.setColorOpaque_I(0xffffff);

		boolean above = world.getBlock(x, y+1, z) == b;
		boolean below = world.getBlock(x, y-1, z) == b;
		int colorTop = ReikaColorAPI.mixColors(0x964335, 0xCC705B, randY.nextFloat());
		int colorBottom = below ? ReikaColorAPI.mixColors(0xE99396, 0xB06A6A, randY.nextFloat()) : ReikaColorAPI.mixColors(0xD6C8C7, 0xAF9199, randY.nextFloat());
		int dr = ReikaRandomHelper.getRandomBetween(0, 4, randY);
		for (int i = 0; i < dr; i++) {
			colorTop = ReikaColorAPI.getColorWithBrightnessMultiplier(colorTop, 0.8F);
			colorTop = ReikaColorAPI.getModifiedSat(colorTop, 1.2F);

			colorBottom = ReikaColorAPI.getColorWithBrightnessMultiplier(colorBottom, 0.9F);
			colorBottom = ReikaColorAPI.getModifiedSat(colorBottom, 1.1F);
		}
		boolean flag = false;
		for (int i = 0; i < n; i++) {
			double dx = ReikaRandomHelper.getRandomPlusMinus(0, 0.375, rand);
			double dz = ReikaRandomHelper.getRandomPlusMinus(0, 0.375, rand);
			double maxLeaf = 0;

			int n2 = 1;
			if (randY.nextInt(3) == 0 && false)
				n2++;
			int nl = ReikaRandomHelper.getRandomBetween(0, 5, randY);
			for (int i0 = 0; i0 < nl; i0++) {
				double ang = ReikaRandomHelper.getRandomPlusMinus(i0*360D/nl, 90D/nl, randY);//Math.toRadians(randY.nextDouble()*360);
				double w = ReikaRandomHelper.getRandomBetween(0.675, 1.25, randY);
				double h = ReikaRandomHelper.getRandomPlusMinus(1, 0.25, randY);
				double ax = w*Math.cos(Math.toRadians(ang));
				double az = w*Math.sin(Math.toRadians(ang));
				double dy = ReikaRandomHelper.getRandomPlusMinus(0.5, 0.25, randY);
				maxLeaf = Math.max(maxLeaf, dy);
				for (int i2 = 0; i2 < n2; i2++) {
					int leafColor = ReikaColorAPI.mixColors(0xFF9D9B, 0xAB3B47, randY.nextFloat());
					dr = ReikaRandomHelper.getRandomBetween(0, 3, randY);
					for (int di = 0; di < dr; di++) {
						leafColor = ReikaColorAPI.getColorWithBrightnessMultiplier(leafColor, 0.8F);
						leafColor = ReikaColorAPI.getModifiedSat(leafColor, 1.2F);
					}
					v5.setColorOpaque_I(leafColor);
					IIcon ico = BlockRedBamboo.getRandomLeaf(randY);
					float u = ico.getMinU();
					float v = ico.getMinV();
					float du = ico.getMaxU();
					float dv = ico.getMaxV();
					for (double ds = 0; ds <= 0.5; ds += 0.5) {
						double right = Math.toRadians(ang+90);
						double ss = (randY.nextDouble()*randY.nextDouble()+ds)%1D;
						double oy = ss*h/2;
						double ox = ss*Math.cos(right)*h/2;
						double oz = ss*Math.sin(right)*h/2;
						if (renderPass == 1) {
							v5.addVertexWithUV(x+dx+0.5-ox, 		y+dy-h/2+oy, 	z+dz+0.5-oz, 		u, v);
							v5.addVertexWithUV(x+dx+0.5+ax-ox, 		y+dy-h/2+oy, 	z+dz+0.5+az-oz, 	du, v);
							v5.addVertexWithUV(x+dx+0.5+ax+ox, 		y+dy+h/2-oy, 	z+dz+0.5+az+oz, 	du, dv);
							v5.addVertexWithUV(x+dx+0.5+ox, 		y+dy+h/2-oy, 	z+dz+0.5+oz, 		u, dv);

							v5.addVertexWithUV(x+dx+0.5+ox, 		y+dy+h/2-oy, 	z+dz+0.5+oz, 		u, dv);
							v5.addVertexWithUV(x+dx+0.5+ax+ox, 		y+dy+h/2-oy, 	z+dz+0.5+az+oz, 	du, dv);
							v5.addVertexWithUV(x+dx+0.5+ax-ox, 		y+dy-h/2+oy, 	z+dz+0.5+az-oz, 	du, v);
							v5.addVertexWithUV(x+dx+0.5-ox, 		y+dy-h/2+oy, 	z+dz+0.5-oz, 		u, v);

							flag = true;
						}
					}
				}
			}
			if (renderPass == 0) {
				double hs = above ? 1 : ReikaRandomHelper.getRandomBetween(maxLeaf+0.125, 1, randY);

				double s = ReikaRandomHelper.getRandomBetween(0.0625, 0.125, rand);
				rb.setRenderBounds(0.5-s, 0, 0.5-s, 0.5+s, hs, 0.5+s);
				int br = ReikaRandomHelper.getRandomBetween(160, 255, rand);
				v5.setColorOpaque_I(ReikaColorAPI.GStoHex(br));
				if (!above) {
					v5.setColorOpaque_I(colorTop);
					rb.renderFaceYPos(b, x+dx, y, z+dz, rb.getBlockIcon(b, world, x, y, z, 1));
				}
				if (!below) {
					v5.setColorOpaque_I(colorBottom);
					rb.renderFaceYNeg(b, x+dx, y, z+dz, rb.getBlockIcon(b, world, x, y, z, 0));
				}

				IIcon bico = rb.getBlockIcon(b, world, x, y, z, ForgeDirection.WEST.ordinal());
				double d3 = bico.getInterpolatedU(rb.renderMinZ*16);
				double d4 = bico.getInterpolatedU(rb.renderMaxZ*16);
				double d5 = bico.getMinV();
				double d6 = bico.getMaxV();
				v5.setColorOpaque_I(colorTop);
				v5.addVertexWithUV(x+dx+0.5-s, y+hs, z+dz+0.5+s, d4, d5);
				v5.addVertexWithUV(x+dx+0.5-s, y+hs, z+dz+0.5-s, d3, d5);
				v5.setColorOpaque_I(colorBottom);
				v5.addVertexWithUV(x+dx+0.5-s, y, z+dz+0.5-s, d3, d6);
				v5.addVertexWithUV(x+dx+0.5-s, y, z+dz+0.5+s, d4, d6);
				//rb.renderFaceXNeg(b, x+dx, y, z+dz, rb.getBlockIcon(b, world, x, y, z, ForgeDirection.WEST.ordinal()));

				v5.setColorOpaque_I(colorBottom);
				v5.addVertexWithUV(x+dx+0.5+s, y, z+dz+0.5+s, d3, d6);
				v5.addVertexWithUV(x+dx+0.5+s, y, z+dz+0.5-s, d4, d6);
				v5.setColorOpaque_I(colorTop);
				v5.addVertexWithUV(x+dx+0.5+s, y+hs, z+dz+0.5-s, d4, d5);
				v5.addVertexWithUV(x+dx+0.5+s, y+hs, z+dz+0.5+s, d3, d5);

				d3 = bico.getInterpolatedU(rb.renderMinX*16);
				d4 = bico.getInterpolatedU(rb.renderMaxX*16);
				v5.setColorOpaque_I(colorTop);
				v5.addVertexWithUV(x+dx+0.5-s, y+hs, z+dz+0.5-s, d4, d5);
				v5.addVertexWithUV(x+dx+0.5+s, y+hs, z+dz+0.5-s, d3, d5);
				v5.setColorOpaque_I(colorBottom);
				v5.addVertexWithUV(x+dx+0.5+s, y, z+dz+0.5-s, d3, d6);
				v5.addVertexWithUV(x+dx+0.5-s, y, z+dz+0.5-s, d4, d6);

				v5.setColorOpaque_I(colorTop);
				v5.addVertexWithUV(x+dx+0.5+s, y+hs, z+dz+0.5+s, d4, d5);
				v5.addVertexWithUV(x+dx+0.5-s, y+hs, z+dz+0.5+s, d3, d5);
				v5.setColorOpaque_I(colorBottom);
				v5.addVertexWithUV(x+dx+0.5-s, y, z+dz+0.5+s, d3, d6);
				v5.addVertexWithUV(x+dx+0.5+s, y, z+dz+0.5+s, d4, d6);

				flag = true;
			}
		}
		return flag;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return true;
	}

}
