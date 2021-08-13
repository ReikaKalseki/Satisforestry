package Reika.Satisforestry.Render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import Reika.DragonAPI.Libraries.IO.ReikaTextureHelper;
import Reika.DragonAPI.Libraries.Rendering.ReikaColorAPI;
import Reika.DragonAPI.Libraries.Rendering.ReikaRenderHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Blocks.BlockPowerSlug;
import Reika.Satisforestry.Blocks.BlockPowerSlug.TilePowerSlug;

public class PowerSlugRenderer extends TileEntitySpecialRenderer {

	private final ModelPowerSlug model = new ModelPowerSlug();

	@Override
	public void renderTileEntityAt(TileEntity tile, double par2, double par4, double par6, float ptick) {
		TilePowerSlug te = (TilePowerSlug)tile;
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glPushMatrix();
		GL11.glTranslated(par2, par4, par6);
		GL11.glScalef(1.0F, -1.0F, -1.0F);
		GL11.glTranslatef(0.5F, -1.5F, -0.5F);
		ReikaRenderHelper.disableEntityLighting();
		ReikaRenderHelper.disableLighting();
		int c = BlockPowerSlug.getColor(te.getTier());
		GL11.glColor4f(ReikaColorAPI.getRed(c)/255F, ReikaColorAPI.getGreen(c)/255F, ReikaColorAPI.getBlue(c)/255F, 0.9F);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glRotatef(te.angle, 0, 1, 0);
		ReikaTextureHelper.bindTexture(Satisforestry.class, "Textures/powerslug.png");
		model.renderAll(te);
		if (tile.hasWorldObj())
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glPopMatrix();
		GL11.glPopAttrib();
	}

}
