package Reika.Satisforestry.Render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.Base.BaseBlockRenderer;
import Reika.DragonAPI.Base.DragonAPIMod;
import Reika.DragonAPI.Base.TileEntityBase;
import Reika.DragonAPI.Base.TileEntityRenderBase;
import Reika.DragonAPI.Instantiable.Rendering.StructureRenderer;
import Reika.DragonAPI.Libraries.IO.ReikaTextureHelper;
import Reika.DragonAPI.Libraries.Rendering.ReikaRenderHelper;
import Reika.Satisforestry.GuiSFBlueprint;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Blocks.BlockMinerMulti.MinerBlocks;
import Reika.Satisforestry.Miner.TileNodeHarvester;
import Reika.Satisforestry.Miner.TileResourceHarvesterBase;
import Reika.Satisforestry.Registry.SFBlocks;


public class SFMinerRenderer extends TileEntityRenderBase {

	//private final WavefrontObject model = new WavefrontObject(DirectResourceManager.getResource("Reika/Satisforestry/Render/miner.obj"));
	private final ModelSFMiner model = new ModelSFMiner("Render/ModelMiner.obj");

	@Override
	public void renderTileEntityAt(TileEntity tile, double par2, double par4, double par6, float ptick) {
		TileNodeHarvester te = (TileNodeHarvester)tile;
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glPushMatrix();
		GL11.glTranslated(par2, par4, par6);
		if (te.isInWorld() || te.forceRenderer) {
			GL11.glTranslated(4, 0, 0);
			GL11.glRotated(180, 0, 1, 0);
			if (this.doRenderModel(te)) {
				if (MinecraftForgeClient.getRenderPass() == 0 || te.forceRenderer) {
					this.renderModel(te, ptick);
				}
				GL11.glPopMatrix();
			}
			else {
				Tessellator.instance.addTranslation(3, 0, -1);
				renderOverlaidCube(te, SFBlocks.MINERMULTI.getBlockInstance().getIcon(1, MinerBlocks.DRILL.ordinal()));
				Tessellator.instance.addTranslation(-3, 0, 1);
				GL11.glPopMatrix();
			}
		}
		else {
			GL11.glTranslated(-0.6, -0.6, 0);
			GL11.glScaled(0.1, 0.1, 0.1);
			this.renderModel(te, ptick);
			GL11.glPopMatrix();
		}
		GL11.glPopAttrib();
	}

	public static void renderOverlaidCube(TileResourceHarvesterBase te, IIcon base) {
		IIcon ico = null;
		double o = 0;
		switch(MinecraftForgeClient.getRenderPass()) {
			case 0:
				ico = base;
				break;
			case 1:
				GL11.glEnable(GL11.GL_BLEND);
				o = 0.002;
				ico = te.getBlockType().getIcon(1, te.worldObj.getBlockMetadata(te.xCoord, te.yCoord, te.zCoord));
				break;
		}
		if (ico != null) {
			Tessellator v5 = Tessellator.instance;
			ReikaTextureHelper.bindTerrainTexture();
			float u = ico.getMinU();
			float v = ico.getMinV();
			float du = ico.getMaxU();
			float dv = ico.getMaxV();
			v5.startDrawingQuads();
			v5.setNormal(1F, 1F, 1F);
			BaseBlockRenderer.faceBrightnessColor(ForgeDirection.UP, v5, 1, 1, 1, te.worldObj, te.xCoord, te.yCoord, te.zCoord, te.getBlockType(), null);
			v5.addVertexWithUV(0-o, 0-o, 0-o, u, v);
			v5.addVertexWithUV(1+o, 0-o, 0-o, du, v);
			v5.addVertexWithUV(1+o, 0-o, 1+o, du, dv);
			v5.addVertexWithUV(0-o, 0-o, 1+o, u, dv);

			BaseBlockRenderer.faceBrightnessColor(ForgeDirection.DOWN, v5, 1, 1, 1, te.worldObj, te.xCoord, te.yCoord, te.zCoord, te.getBlockType(), null);
			v5.addVertexWithUV(0-o, 1+o, 1+o, u, dv);
			v5.addVertexWithUV(1+o, 1+o, 1+o, du, dv);
			v5.addVertexWithUV(1+o, 1+o, 0-o, du, v);
			v5.addVertexWithUV(0-o, 1+o, 0-o, u, v);

			BaseBlockRenderer.faceBrightnessColor(ForgeDirection.EAST, v5, 1, 1, 1, te.worldObj, te.xCoord, te.yCoord, te.zCoord, te.getBlockType(), null);
			v5.addVertexWithUV(1+o, 1+o, 1+o, du, dv);
			v5.addVertexWithUV(1+o, 0-o, 1+o, du, v);
			v5.addVertexWithUV(1+o, 0-o, 0-o, u, v);
			v5.addVertexWithUV(1+o, 1+o, 0-o, u, dv);

			BaseBlockRenderer.faceBrightnessColor(ForgeDirection.SOUTH, v5, 1, 1, 1, te.worldObj, te.xCoord, te.yCoord, te.zCoord, te.getBlockType(), null);
			v5.addVertexWithUV(0-o, 1+o, 1+o, u, dv);
			v5.addVertexWithUV(0-o, 0-o, 1+o, u, v);
			v5.addVertexWithUV(1+o, 0-o, 1+o, du, v);
			v5.addVertexWithUV(1+o, 1+o, 1+o, du, dv);

			BaseBlockRenderer.faceBrightnessColor(ForgeDirection.NORTH, v5, 1, 1, 1, te.worldObj, te.xCoord, te.yCoord, te.zCoord, te.getBlockType(), null);
			v5.addVertexWithUV(1+o, 1+o, 0-o, du, dv);
			v5.addVertexWithUV(1+o, 0-o, 0-o, du, v);
			v5.addVertexWithUV(0-o, 0-o, 0-o, u, v);
			v5.addVertexWithUV(0-o, 1+o, 0-o, u, dv);

			BaseBlockRenderer.faceBrightnessColor(ForgeDirection.WEST, v5, 1, 1, 1, te.worldObj, te.xCoord, te.yCoord, te.zCoord, te.getBlockType(), null);
			v5.addVertexWithUV(0-o, 1+o, 0-o, u, dv);
			v5.addVertexWithUV(0-o, 0-o, 0-o, u, v);
			v5.addVertexWithUV(0-o, 0-o, 1+o, du, v);
			v5.addVertexWithUV(0-o, 1+o, 1+o, du, dv);
			v5.draw();
		}
	}

	@SuppressWarnings("incomplete-switch")
	private void renderModel(TileNodeHarvester te, float ptick) {
		ReikaTextureHelper.bindTexture(Satisforestry.class, "Textures/MinerTex.png");
		GL11.glPushMatrix();
		ForgeDirection dir = te.getDirection();
		if (dir == null)
			dir = ForgeDirection.EAST;
		switch(dir) {
			case EAST:
				break;
			case NORTH:
				GL11.glRotated(90, 0, 1, 0);
				GL11.glTranslated(-3, 0, 4);
				break;
			case SOUTH:
				GL11.glRotated(-90, 0, 1, 0);
				GL11.glTranslated(-4, 0, -3);
				break;
			case WEST:
				GL11.glRotated(180, 0, 1, 0);
				GL11.glTranslated(-7, 0, 1);
				break;
		}
		model.drawChassis();
		GL11.glPushMatrix();
		double d = 3.5;
		GL11.glTranslated(d, 0, -0.5);
		GL11.glTranslated(0, 1-te.getDrillHeight(ptick), 0);
		GL11.glRotated(te.getDrillAngle(ptick), 0, 1, 0);
		GL11.glTranslated(-d, 0, 0.5);
		model.drawDrill();
		GL11.glPopMatrix();
		if (te.isInWorld()) {
			GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
			ReikaRenderHelper.disableLighting();
			ReikaRenderHelper.disableEntityLighting();
			model.drawLightbar(te.getLightbarColorForRender());
			GL11.glPopAttrib();
		}
		GL11.glPopMatrix();
	}

	@Override
	protected boolean doRenderModel(TileEntityBase te) {
		((TileResourceHarvesterBase)te).forceRenderer = StructureRenderer.isRenderingTiles() && GuiSFBlueprint.renderTESR;
		if (((TileResourceHarvesterBase)te).forceRenderer)
			return true;
		return (te.isInWorld() && ((TileNodeHarvester)te).hasStructure());
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
