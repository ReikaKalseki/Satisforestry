package Reika.Satisforestry.Render;

import org.lwjgl.opengl.GL11;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.MinecraftForgeClient;

import Reika.DragonAPI.Base.DragonAPIMod;
import Reika.DragonAPI.Base.TileEntityBase;
import Reika.DragonAPI.Base.TileEntityRenderBase;
import Reika.DragonAPI.Instantiable.Rendering.StructureRenderer;
import Reika.DragonAPI.Libraries.IO.ReikaTextureHelper;
import Reika.DragonAPI.Libraries.Rendering.ReikaRenderHelper;
import Reika.Satisforestry.GuiSFBlueprint;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Blocks.BlockFrackerMulti.FrackerBlocks;
import Reika.Satisforestry.Miner.TileFrackingPressurizer;
import Reika.Satisforestry.Miner.TileResourceHarvesterBase;
import Reika.Satisforestry.Registry.SFBlocks;


public class SFFrackerRenderer extends TileEntityRenderBase {

	private final ModelSFFracker model = new ModelSFFracker("Render/ModelFracker.obj");

	@Override
	public void renderTileEntityAt(TileEntity tile, double par2, double par4, double par6, float ptick) {
		TileFrackingPressurizer te = (TileFrackingPressurizer)tile;
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glPushMatrix();
		GL11.glTranslated(par2, par4, par6);
		if (te.isInWorld() || te.forceRenderer) {
			//GL11.glTranslated(4, 0, 0);
			if (this.doRenderModel(te)) {
				if (MinecraftForgeClient.getRenderPass() == 0 || te.forceRenderer) {
					this.renderModel(te);
				}
				GL11.glPopMatrix();/*
				if (MinecraftForgeClient.getRenderPass() == 1 || te.forceRenderer) {
					GL11.glPushMatrix();
					Tessellator.instance.setBrightness(240);
					Tessellator.instance.setColorOpaque_I(0xffffff);
					ReikaRenderHelper.disableEntityLighting();
					ReikaRenderHelper.disableLighting();
					GL11.glColor4f(1, 1, 1, 1);
					GL11.glEnable(GL11.GL_BLEND);
					BlendMode.DEFAULT.apply();
					AxisAlignedBB box = te.getRenderBoundingBox();
					GL11.glDepthMask(false);
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
				}*/
			}
			else {
				SFMinerRenderer.renderOverlaidCube(te, SFBlocks.FRACKERMULTI.getBlockInstance().getIcon(1, FrackerBlocks.TUBE.ordinal()));
				GL11.glPopMatrix();
			}
		}
		else {
			GL11.glTranslated(0, -0.5, 0);
			GL11.glScaled(0.1, 0.1, 0.1);
			this.renderModel(te);
			GL11.glPopMatrix();
		}
		GL11.glPopAttrib();
	}

	@SuppressWarnings("incomplete-switch")
	private void renderModel(TileFrackingPressurizer te) {
		ReikaTextureHelper.bindTexture(Satisforestry.class, "Textures/FrackerTex.png");
		GL11.glPushMatrix();
		GL11.glTranslated(0.5, 0, 0.5);
		model.drawChassis(te.ventExtension);
		if (te.isInWorld() || StructureRenderer.isRenderingTiles()) {
			model.drawThumper(te.thumper1);
			model.drawThumper(te.thumper2);
			model.drawThumper(te.thumper3);
			model.drawThumper(te.thumper4);
			GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
			ReikaRenderHelper.disableLighting();
			ReikaRenderHelper.disableEntityLighting();
			model.drawLightbar(te.getLightbarColorForRender());
			GL11.glPopAttrib();
		}
		else {
			GL11.glTranslated(0, -2, 0);
			model.renderThumpers();
		}
		GL11.glPopMatrix();
	}

	@Override
	protected boolean doRenderModel(TileEntityBase te) {
		((TileResourceHarvesterBase)te).forceRenderer = StructureRenderer.isRenderingTiles() && GuiSFBlueprint.renderTESR;
		if (((TileResourceHarvesterBase)te).forceRenderer)
			return true;
		return (te.isInWorld() && ((TileFrackingPressurizer)te).hasStructure());
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
