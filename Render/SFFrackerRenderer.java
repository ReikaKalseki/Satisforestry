package Reika.Satisforestry.Render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.MinecraftForgeClient;

import Reika.DragonAPI.Base.DragonAPIMod;
import Reika.DragonAPI.Base.TileEntityBase;
import Reika.DragonAPI.Base.TileEntityRenderBase;
import Reika.DragonAPI.Libraries.ReikaAABBHelper;
import Reika.DragonAPI.Libraries.IO.ReikaTextureHelper;
import Reika.DragonAPI.Libraries.Java.ReikaGLHelper.BlendMode;
import Reika.DragonAPI.Libraries.Rendering.ReikaColorAPI;
import Reika.DragonAPI.Libraries.Rendering.ReikaGuiAPI;
import Reika.DragonAPI.Libraries.Rendering.ReikaRenderHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Miner.TileFrackingPressurizer;


public class SFFrackerRenderer extends TileEntityRenderBase {

	//private final ModelSFFracker model = new ModelSFMiner("Render/ModelFracker.obj");

	@Override
	public void renderTileEntityAt(TileEntity tile, double par2, double par4, double par6, float ptick) {
		TileFrackingPressurizer te = (TileFrackingPressurizer)tile;
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glPushMatrix();
		GL11.glTranslated(par2, par4, par6);
		//GL11.glTranslated(4, 0, 0);
		if (this.doRenderModel(te)) {
			if (MinecraftForgeClient.getRenderPass() == 0) {
				this.renderModel(te);
			}
			GL11.glPopMatrix();
			if (MinecraftForgeClient.getRenderPass() == 1) {
				GL11.glPushMatrix();
				Tessellator.instance.setBrightness(240);
				Tessellator.instance.setColorOpaque_I(0xffffff);
				ReikaRenderHelper.disableEntityLighting();
				ReikaRenderHelper.disableLighting();
				GL11.glColor4f(1, 1, 1, 1);
				GL11.glEnable(GL11.GL_BLEND);
				BlendMode.DEFAULT.apply();
				AxisAlignedBB box = te.getRenderBoundingBox();
				ReikaAABBHelper.renderAABB(box, par2, par4, par6, te.xCoord, te.yCoord, te.zCoord, 255, 64, 64, 32, true);
				GL11.glDepthMask(false);
				GL11.glTranslated(par2, par4, par6);
				double s = 0.2;
				GL11.glTranslated(0.5, 7.5, 0.5);
				//GL11.glRotated(180-RenderManager.instance.playerViewY, 0, 1, 0);
				//GL11.glRotated(-RenderManager.instance.playerViewX/2D, 1, 0, 0);
				GL11.glScaled(-s, -s, s);
				for (int i = 0; i < 4; i++) {
					GL11.glPushMatrix();
					GL11.glRotated(i*90, 0, 1, 0);
					GL11.glTranslated(0, 0, -5.5/s);
					GL11.glRotated(-30, 0, 0, 1);
					ReikaGuiAPI.instance.drawCenteredStringNoShadow(this.getFontRenderer(), "Incomplete", 0, 0, 0xffffff);
					GL11.glPopMatrix();
				}

				GL11.glPopMatrix();
			}
		}
		else {

			GL11.glPopMatrix();
		}
		GL11.glPopAttrib();
	}

	@SuppressWarnings("incomplete-switch")
	private void renderModel(TileFrackingPressurizer te) {
		ReikaTextureHelper.bindTexture(Satisforestry.class, "Textures/fracker.png");
		GL11.glPushMatrix();
		//model.drawChassis();
		GL11.glPushMatrix();
		GL11.glTranslated(0, -te.thumper1.getPosition(), 0);
		//model.drawThumper1();
		GL11.glTranslated(0, te.thumper1.getPosition()-te.thumper2.getPosition(), 0);
		//model.drawThumper2();
		GL11.glTranslated(0, te.thumper2.getPosition()-te.thumper3.getPosition(), 0);
		//model.drawThumper3();
		GL11.glTranslated(0, te.thumper3.getPosition()-te.thumper4.getPosition(), 0);
		//model.drawThumper4();
		GL11.glTranslated(0, te.thumper4.getPosition(), 0);
		GL11.glPopMatrix();
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		ReikaRenderHelper.disableLighting();
		ReikaRenderHelper.disableEntityLighting();
		int c = te.getState().color;
		if (te.getOverclockingStep(true) > 0) {
			float f = 0.5F+(float)(0.5*Math.sin(te.getTicksExisted()*0.004));
			c = ReikaColorAPI.mixColors(c, 0xffffff, f);
		}
		//model.drawLightbar(c);
		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}

	@Override
	protected boolean doRenderModel(TileEntityBase te) {
		return te.isInWorld() && ((TileFrackingPressurizer)te).hasStructure();
	}

	@Override
	public String getTextureFolder() {
		return "Textures/";
	}

	@Override
	protected DragonAPIMod getOwnerMod() {
		return Satisforestry.instance;
	}

	@Override
	protected Class getModClass() {
		return Satisforestry.class;
	}

}
