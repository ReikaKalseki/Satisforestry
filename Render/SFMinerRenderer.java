package Reika.Satisforestry.Render;

import org.lwjgl.opengl.GL11;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.MinecraftForgeClient;

import Reika.DragonAPI.Base.DragonAPIMod;
import Reika.DragonAPI.Base.TileEntityBase;
import Reika.DragonAPI.Base.TileEntityRenderBase;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Blocks.TileNodeHarvester;


public class SFMinerRenderer extends TileEntityRenderBase {

	//private final WavefrontObject model = new WavefrontObject(DirectResourceManager.getResource("Reika/Satisforestry/Render/miner.obj"));
	private final ModelSFMiner model = new ModelSFMiner("/Render/miner.obj");

	@Override
	public void renderTileEntityAt(TileEntity tile, double par2, double par4, double par6, float ptick) {
		TileNodeHarvester te = (TileNodeHarvester)tile;
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glPushMatrix();
		GL11.glTranslated(par2, par4, par6);
		if (this.doRenderModel(te)) {
			this.renderModel(te);
			this.renderLightbar(te);
		}
		else {

		}
		GL11.glPopMatrix();
		GL11.glPopAttrib();
	}

	private void renderModel(TileNodeHarvester te) {
		GL11.glPushMatrix();
		model.drawChassis();
		GL11.glPushMatrix();
		double f = te.getActivityRamp();
		double drillVertical = -Math.min(3*f, 1.5);
		double drillSpin = 0;
		GL11.glTranslated(0, drillVertical, 0);
		GL11.glRotated(drillSpin, 0, 1, 0);
		GL11.glPopMatrix();
		GL11.glPopMatrix();
	}

	private void renderLightbar(TileNodeHarvester te) {

	}

	@Override
	protected boolean doRenderModel(TileEntityBase te) {
		return MinecraftForgeClient.getRenderPass() == 0 && !(te.isInWorld() && !((TileNodeHarvester)te).hasStructure());
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
