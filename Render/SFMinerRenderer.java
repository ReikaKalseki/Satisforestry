package Reika.Satisforestry.Render;

import org.lwjgl.opengl.GL11;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.MinecraftForgeClient;

import Reika.DragonAPI.Base.DragonAPIMod;
import Reika.DragonAPI.Base.TileEntityBase;
import Reika.DragonAPI.Base.TileEntityRenderBase;
import Reika.DragonAPI.Libraries.IO.ReikaTextureHelper;
import Reika.DragonAPI.Libraries.Rendering.ReikaColorAPI;
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
			this.renderModel(te);
		}
		else {

		}
		GL11.glPopMatrix();
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
		if (te.getOverclockingStep() > 0) {
			float f = 0.5F+(float)(0.5*Math.sin(te.getTicksExisted()*0.004));
			c = ReikaColorAPI.mixColors(c, 0xffffff, f);
		}
		model.drawLightbar(c);
		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}

	@Override
	protected boolean doRenderModel(TileEntityBase te) {
		return MinecraftForgeClient.getRenderPass() == 0 && true;//!(te.isInWorld() && !((TileNodeHarvester)te).hasStructure());
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
