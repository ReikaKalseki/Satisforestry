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
import Reika.Satisforestry.GuiSFBlueprint;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Blocks.BlockFrackerMulti.FrackerBlocks;
import Reika.Satisforestry.Blocks.BlockFrackingPressurizer.TileFrackingExtractor;
import Reika.Satisforestry.Registry.SFBlocks;


public class SFFrackerAuxRenderer extends TileEntityRenderBase {

	private final ModelSFFrackerAux model = new ModelSFFrackerAux("Render/ModelFrackerAux.obj");

	@Override
	public void renderTileEntityAt(TileEntity tile, double par2, double par4, double par6, float ptick) {
		TileFrackingExtractor te = (TileFrackingExtractor)tile;
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glPushMatrix();
		GL11.glTranslated(par2, par4, par6);
		//GL11.glTranslated(4, 0, 0);
		if (this.doRenderModel(te)) {
			ReikaTextureHelper.bindTexture(Satisforestry.class, "Textures/FrackerAuxTex.png");
			GL11.glPushMatrix();
			if (te.isInWorld()) {
				GL11.glTranslated(0.5, -2, 0.5);
				double ang = 0;
				switch (te.getFacing()) {
					case EAST:
						break;
					case NORTH:
						ang = 90;
						break;
					case SOUTH:
						ang = -90;
						break;
					case WEST:
						ang = 180;
						break;
				}
				GL11.glRotated(ang, 0, 1, 0);
			}
			else {
				double s = 0.4;
				GL11.glScaled(s, s, s);
				GL11.glTranslated(0, -1.5, 0);
			}
			model.render();
			GL11.glPopMatrix();
			GL11.glPopMatrix();
		}
		else {
			if (MinecraftForgeClient.getRenderPass() == 0 || StructureRenderer.isRenderingTiles()) {
				IIcon ico = SFBlocks.FRACKERMULTI.getBlockInstance().getIcon(1, FrackerBlocks.TUBE.ordinal());
				Tessellator v5 = Tessellator.instance;
				ReikaTextureHelper.bindTerrainTexture();
				float u = ico.getMinU();
				float v = ico.getMinV();
				float du = ico.getMaxU();
				float dv = ico.getMaxV();
				v5.startDrawingQuads();
				v5.setNormal(1F, 1F, 1F);
				BaseBlockRenderer.faceBrightnessColor(ForgeDirection.UP, v5, 1, 1, 1, te.worldObj, te.xCoord, te.yCoord, te.zCoord, te.getBlockType(), null);
				v5.addVertexWithUV(0, 0, 0, u, v);
				v5.addVertexWithUV(1, 0, 0, du, v);
				v5.addVertexWithUV(1, 0, 1, du, dv);
				v5.addVertexWithUV(0, 0, 1, u, dv);

				BaseBlockRenderer.faceBrightnessColor(ForgeDirection.DOWN, v5, 1, 1, 1, te.worldObj, te.xCoord, te.yCoord, te.zCoord, te.getBlockType(), null);
				v5.addVertexWithUV(0, 1, 1, u, dv);
				v5.addVertexWithUV(1, 1, 1, du, dv);
				v5.addVertexWithUV(1, 1, 0, du, v);
				v5.addVertexWithUV(0, 1, 0, u, v);

				BaseBlockRenderer.faceBrightnessColor(ForgeDirection.EAST, v5, 1, 1, 1, te.worldObj, te.xCoord, te.yCoord, te.zCoord, te.getBlockType(), null);
				v5.addVertexWithUV(1, 1, 1, du, dv);
				v5.addVertexWithUV(1, 0, 1, du, v);
				v5.addVertexWithUV(1, 0, 0, u, v);
				v5.addVertexWithUV(1, 1, 0, u, dv);

				BaseBlockRenderer.faceBrightnessColor(ForgeDirection.SOUTH, v5, 1, 1, 1, te.worldObj, te.xCoord, te.yCoord, te.zCoord, te.getBlockType(), null);
				v5.addVertexWithUV(0, 1, 1, u, dv);
				v5.addVertexWithUV(0, 0, 1, u, v);
				v5.addVertexWithUV(1, 0, 1, du, v);
				v5.addVertexWithUV(1, 1, 1, du, dv);

				BaseBlockRenderer.faceBrightnessColor(ForgeDirection.NORTH, v5, 1, 1, 1, te.worldObj, te.xCoord, te.yCoord, te.zCoord, te.getBlockType(), null);
				v5.addVertexWithUV(1, 1, 0, du, dv);
				v5.addVertexWithUV(1, 0, 0, du, v);
				v5.addVertexWithUV(0, 0, 0, u, v);
				v5.addVertexWithUV(0, 1, 0, u, dv);

				BaseBlockRenderer.faceBrightnessColor(ForgeDirection.WEST, v5, 1, 1, 1, te.worldObj, te.xCoord, te.yCoord, te.zCoord, te.getBlockType(), null);
				v5.addVertexWithUV(0, 1, 0, u, dv);
				v5.addVertexWithUV(0, 0, 0, u, v);
				v5.addVertexWithUV(0, 0, 1, du, v);
				v5.addVertexWithUV(0, 1, 1, du, dv);
				v5.draw();
			}
			GL11.glPopMatrix();
		}
		GL11.glPopAttrib();
	}

	@Override
	protected boolean doRenderModel(TileEntityBase te) {
		if (StructureRenderer.isRenderingTiles() && GuiSFBlueprint.renderTESR)
			return true;
		return !te.isInWorld() || (MinecraftForgeClient.getRenderPass() == 0 && ((TileFrackingExtractor)te).hasStructure());
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
