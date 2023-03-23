package Reika.Satisforestry.Render;

import org.lwjgl.opengl.GL11;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.MinecraftForgeClient;

import Reika.DragonAPI.Base.DragonAPIMod;
import Reika.DragonAPI.Base.TileEntityBase;
import Reika.DragonAPI.Base.TileEntityRenderBase;
import Reika.DragonAPI.Libraries.IO.ReikaTextureHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Blocks.BlockFrackingPressurizer.TileFrackingExtractor;


public class SFFrackerAuxRenderer extends TileEntityRenderBase {

	//private final ModelSFFrackerAux model = new ModelSFMinerAux("Render/ModelFracker.obj");

	@Override
	public void renderTileEntityAt(TileEntity tile, double par2, double par4, double par6, float ptick) {
		TileFrackingExtractor te = (TileFrackingExtractor)tile;
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glPushMatrix();
		GL11.glTranslated(par2, par4, par6);
		//GL11.glTranslated(4, 0, 0);
		if (this.doRenderModel(te)) {
			ReikaTextureHelper.bindTexture(Satisforestry.class, "Textures/frackeraux.png");
			GL11.glPushMatrix();
			//model.drawChassis();
			GL11.glPopMatrix();
			GL11.glPopMatrix();
		}
		else {

			GL11.glPopMatrix();
		}
		GL11.glPopAttrib();
	}

	@Override
	protected boolean doRenderModel(TileEntityBase te) {
		return te.isInWorld() && MinecraftForgeClient.getRenderPass() == 0 && ((TileFrackingExtractor)te).hasStructure();
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
