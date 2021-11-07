package Reika.Satisforestry.Render;

import java.util.HashMap;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.MinecraftForgeClient;

import Reika.DragonAPI.Instantiable.RayTracer;
import Reika.DragonAPI.Instantiable.RayTracer.RayTracerWithCache;
import Reika.DragonAPI.Libraries.IO.ReikaTextureHelper;
import Reika.DragonAPI.Libraries.Java.ReikaGLHelper.BlendMode;
import Reika.DragonAPI.Libraries.Rendering.ReikaColorAPI;
import Reika.DragonAPI.Libraries.Rendering.ReikaRenderHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Blocks.BlockPowerSlug;
import Reika.Satisforestry.Blocks.BlockPowerSlug.TilePowerSlug;
import Reika.Satisforestry.Blocks.BlockPowerSlug.TilePowerSlugInert;
import Reika.Satisforestry.Registry.SFShaders;

public class PowerSlugRenderer extends TileEntitySpecialRenderer {

	protected static final RayTracerWithCache LOS = RayTracer.getVisualLOSForRenderCulling();

	private final ModelPowerSlug model = new ModelPowerSlug();

	@Override
	public void renderTileEntityAt(TileEntity tile, double par2, double par4, double par6, float ptick) {
		TilePowerSlug te = (TilePowerSlug)tile;
		if (te.hasWorldObj() && MinecraftForgeClient.getRenderPass() != 1)
			return;
		GL11.glPushMatrix();
		GL11.glEnable(GL11.GL_BLEND);
		BlendMode.DEFAULT.apply();
		GL11.glTranslated(par2, par4, par6);
		GL11.glScalef(1.0F, -1.0F, -1.0F);
		//GL11.glDepthMask(false);
		GL11.glTranslatef(0.5F, -1.5F, -0.5F);
		if (te.hasWorldObj()) {/*
			if (ReikaWorldHelper.checkForAdjBlock(te.worldObj, te.xCoord, te.yCoord, te.zCoord, Blocks.water) != null) {
				int l = Blocks.water.colorMultiplier(te.worldObj, te.xCoord, te.yCoord, te.zCoord);
				float f = (l >> 16 & 255) / 255.0F;
				float f1 = (l >> 8 & 255) / 255.0F;
				float f2 = (l & 255) / 255.0F;
				double d2 = RenderBlocks.getInstance().getLiquidHeight(te.xCoord, te.yCoord, te.zCoord, Material.water);
				double d3 = RenderBlocks.getInstance().getLiquidHeight(te.xCoord, te.yCoord, te.zCoord + 1, Material.water);
				double d4 = RenderBlocks.getInstance().getLiquidHeight(te.xCoord + 1, te.yCoord, te.zCoord + 1, Material.water);
				double d5 = RenderBlocks.getInstance().getLiquidHeight(te.xCoord + 1, te.yCoord, te.zCoord, Material.water);
				Tessellator v5 = Tessellator.instance;
				v5.startDrawingQuads();
				v5.setBrightness(Blocks.water.getMixedBrightnessForBlock(te.worldObj, te.xCoord, te.yCoord, te.zCoord));
				v5.setColorOpaque_F(f, f1, f2);
				IIcon ico = Blocks.water.getIcon(te.worldObj, te.xCoord, te.yCoord, te.zCoord, 1);
				float u = ico.getMinU();
				float du = ico.getMaxU();
				float v = ico.getMinV();
				float dv = ico.getMaxV();
				v5.addVertexWithUV(0-0.5, d4, 1-0.5, du, v);
				v5.addVertexWithUV(0-0.5, d5, 0-0.5, du, dv);
				v5.addVertexWithUV(1-0.5, d2, 0-0.5, u, dv);
				v5.addVertexWithUV(1-0.5, d3, 1-0.5, u, v);
				v5.draw();
			}*/
		}
		else {
			float s = 1.5F;
			GL11.glScalef(s, s, s);
			GL11.glTranslatef(0.35F, -s/2.5F, 0);
			GL11.glRotated(15, 0, 0, 1);
			GL11.glRotated(-90, 0, 1, 0);
		}
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		ReikaRenderHelper.disableEntityLighting();
		//GL11.glShadeModel(GL11.GL_SMOOTH);
		//if (te.hasWorldObj())
		ReikaRenderHelper.disableLighting();
		//GL11.glDisable(GL11.GL_CULL_FACE);
		int tier = te.getTier();
		if (te instanceof TilePowerSlugInert)
			tier += 3;
		int c = BlockPowerSlug.getColor(tier);
		GL11.glColor4f(ReikaColorAPI.getRed(c)/255F, ReikaColorAPI.getGreen(c)/255F, ReikaColorAPI.getBlue(c)/255F, 1F);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		if (te.hasWorldObj()) {
			switch(te.getDirection()) {
				case SOUTH:
					GL11.glRotatef(-90, 1, 0, 0);
					GL11.glTranslatef(0, -1, 1);
					break;
				case NORTH:
					GL11.glRotatef(90, 1, 0, 0);
					GL11.glRotatef(180, 0, 1, 0);
					GL11.glTranslatef(0, -1, 1);
					break;
				case EAST:
					GL11.glRotatef(-90, 0, 0, 1);
					GL11.glTranslatef(-1, -1, 0);
					break;
				case WEST:
					GL11.glRotatef(90, 0, 0, 1);
					GL11.glRotatef(180, 0, 1, 0);
					GL11.glTranslatef(-1, -1, 0);
					break;
				case UP:
					GL11.glRotatef(180, 1, 0, 0);
					GL11.glTranslatef(0, -2, 0);
					break;
				case DOWN:
				default:
					break;
			}
		}
		GL11.glRotatef(te.angle, 0, 1, 0);
		ReikaTextureHelper.bindTexture(Satisforestry.class, "Textures/powerslug.png");
		model.renderAll(te);
		if (tile.hasWorldObj())
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glPopAttrib();
		if (te.hasWorldObj()) {
			if (!(te instanceof TilePowerSlugInert)) {
				EntityPlayer ep = Minecraft.getMinecraft().thePlayer;
				LOS.setOrigins(te.xCoord+0.5, te.yCoord+0.25, te.zCoord+0.5, ep.posX, ep.posY, ep.posZ);
				if (LOS.isClearLineOfSight(te)) {
					double dist = ep.getDistance(te.xCoord+0.5, te.yCoord+0.25, te.zCoord+0.5);
					float f = 0;
					if (dist <= 4) {
						f = 1;
					}
					else if (dist <= 8) {
						f = 1-(float)((dist-4D)/4D);
					}
					if (f > 0) {
						HashMap<String, Object> map = new HashMap();
						map.put("distance", dist*dist);
						map.put("scale", 1.5F);
						map.put("factor", 0.1F);
						map.put("speed", 1.5F);
						SFShaders.SLUG.getShader().addFocus(te.xCoord, te.yCoord, te.zCoord);
						SFShaders.SLUG.getShader().modifyLastCompoundFocus(f, map);
						SFShaders.SLUG.getShader().setEnabled(true);
						SFShaders.SLUG.setIntensity(Math.max(SFShaders.SLUG.getIntensity(), f));
						SFShaders.SLUG.clearOnRender = true;
					}
				}
			}
		}
		else {
			GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glColor4f(ReikaColorAPI.getRed(c)/255F, ReikaColorAPI.getGreen(c)/255F, ReikaColorAPI.getBlue(c)/255F, 0.2F);
			ReikaTextureHelper.bindTexture(Satisforestry.class, "Textures/powerslug.png");
			model.renderAll(te);
			GL11.glPopAttrib();
		}
		GL11.glPopMatrix();
	}

}
