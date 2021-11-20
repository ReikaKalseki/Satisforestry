package Reika.Satisforestry.Render;

import org.lwjgl.opengl.GL11;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.util.ForgeDirection;

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
import Reika.Satisforestry.Miner.TileNodeHarvester;


public class SFMinerRenderer extends TileEntityRenderBase {

	//private final WavefrontObject model = new WavefrontObject(DirectResourceManager.getResource("Reika/Satisforestry/Render/miner.obj"));
	private final ModelSFMiner model = new ModelSFMiner("Render/ModelMiner.obj");

	@Override
	public void renderTileEntityAt(TileEntity tile, double par2, double par4, double par6, float ptick) {
		TileNodeHarvester te = (TileNodeHarvester)tile;
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glPushMatrix();
		GL11.glTranslated(par2, par4, par6);
		GL11.glTranslated(4, 0, 0);
		GL11.glRotated(180, 0, 1, 0);
		if (this.doRenderModel(te)) {
			if (MinecraftForgeClient.getRenderPass() == 0) {
				this.renderModel(te);
			}
			GL11.glPopMatrix();
			if (MinecraftForgeClient.getRenderPass() == 1) {
				GL11.glPushMatrix();
				ReikaRenderHelper.disableEntityLighting();
				ReikaRenderHelper.disableLighting();
				GL11.glEnable(GL11.GL_BLEND);
				BlendMode.DEFAULT.apply();
				AxisAlignedBB box = te.getRenderBoundingBox();
				ReikaAABBHelper.renderAABB(box, par2, par4, par6, te.xCoord, te.yCoord, te.zCoord, 255, 64, 64, 32, true);

				ForgeDirection dir = te.getDirection();

				GL11.glDepthMask(false);
				GL11.glTranslated(par2, par4, par6);
				double a = 0.5+dir.offsetX*1.5;
				double b = 6.5;
				double c = 0.5+dir.offsetZ*1.5;
				double s = 0.25;
				if (dir.offsetX == 0) {
					a += 2.5;
				}
				else {
					c += 2.5;
				}
				GL11.glTranslated(a, b, c);
				//GL11.glRotated(180-RenderManager.instance.playerViewY, 0, 1, 0);
				//GL11.glRotated(-RenderManager.instance.playerViewX/2D, 1, 0, 0);
				GL11.glRotated(30, dir.offsetX == 0 ? 1 : 0, 0, dir.offsetZ == 0 ? 1 : 0);
				if (dir.offsetX != 0)
					GL11.glScaled(s, -s, 1);
				else
					GL11.glScaled(1, -s, s);
				ReikaGuiAPI.instance.drawCenteredStringNoShadow(this.getFontRenderer(), "Incomplete", 0, 0, 0xffffff);
				GL11.glRotated(180, 0, 1, 0);
				GL11.glTranslated(dir.offsetZ*5-dir.offsetX*3, 0, dir.offsetX*5-dir.offsetZ*3);
				GL11.glRotated(-60, dir.offsetX == 0 ? 1 : 0, 0, dir.offsetZ == 0 ? 1 : 0);
				ReikaGuiAPI.instance.drawCenteredStringNoShadow(this.getFontRenderer(), "Incomplete", 0, 0, 0xffffff);

				GL11.glPopMatrix();
			}
		}
		else {

			GL11.glPopMatrix();
		}
		GL11.glPopAttrib();
	}

	private void renderModel(TileNodeHarvester te) {
		ReikaTextureHelper.bindTexture(Satisforestry.class, "Textures/miner.png");
		GL11.glPushMatrix();
		model.drawChassis();
		GL11.glPushMatrix();
		GL11.glTranslated(0, -te.getDrillVerticalOffsetScale(0.5, 1.5), 0);
		GL11.glRotated(te.drillSpinAngle, 0, 1, 0);
		model.drawDrill();
		GL11.glPopMatrix();
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		ReikaRenderHelper.disableLighting();
		ReikaRenderHelper.disableEntityLighting();
		int c = te.getState().color;
		if (te.getOverclockingStep(true) > 0) {
			float f = 0.5F+(float)(0.5*Math.sin(te.getTicksExisted()*0.004));
			c = ReikaColorAPI.mixColors(c, 0xffffff, f);
		}
		model.drawLightbar(c);
		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}

	@Override
	protected boolean doRenderModel(TileEntityBase te) {
		return te.isInWorld() && ((TileNodeHarvester)te).hasStructure();
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
