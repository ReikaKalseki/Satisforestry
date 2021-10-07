package Reika.Satisforestry.Render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import Reika.DragonAPI.Extras.IconPrefabs;
import Reika.DragonAPI.Instantiable.Effects.EntityBlurFX;
import Reika.DragonAPI.Libraries.IO.ReikaTextureHelper;
import Reika.DragonAPI.Libraries.Java.ReikaGLHelper.BlendMode;
import Reika.DragonAPI.Libraries.MathSci.ReikaPhysicsHelper;
import Reika.Satisforestry.Entity.EntitySpitter.SpitterType;
import Reika.Satisforestry.Entity.EntitySpitterFireball;


public class RenderSpitterFire extends Render {

	public static final RenderSpitterFire instance = new RenderSpitterFire();

	private RenderSpitterFire() {

	}

	@Override
	public void doRender(Entity e, double par2, double par4, double par6, float f, float ptick) {
		ReikaTextureHelper.bindTerrainTexture();
		EntitySpitterFireball eb = (EntitySpitterFireball)e;
		Tessellator v5 = Tessellator.instance;
		IIcon icon = IconPrefabs.FADE.getIcon();
		float u = icon.getMinU();
		float v = icon.getMinV();
		float du = icon.getMaxU();
		float dv = icon.getMaxV();
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glPushMatrix();
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDepthMask(false);
		BlendMode.ADDITIVEDARK.apply();
		GL11.glTranslated(par2, par4, par6);
		if (!e.isDead) {
			RenderManager rm = RenderManager.instance;
			double dx = e.posX-RenderManager.renderPosX;
			double dy = e.posY-RenderManager.renderPosY;
			double dz = e.posZ-RenderManager.renderPosZ;
			double[] angs = ReikaPhysicsHelper.cartesianToPolar(dx, dy, dz);
			GL11.glRotated(angs[2], 0, 1, 0);
			GL11.glRotated(90-angs[1], 1, 0, 0);
		}
		//GL11.glRotatef(rm.playerViewX, 1.0F, 0.0F, 0.0F);
		v5.startDrawingQuads();
		v5.setBrightness(240);
		SpitterType type = eb.getSpitterType();
		double s1 = type.isAlpha() ? 0.35 : 0.25;
		double d = 0.001;
		double s2 = s1*1.5;
		int c1 = eb.getRenderColor(1);
		int c2 = eb.getRenderColor(2);
		v5.setColorOpaque_I(c1);
		v5.addVertexWithUV(-s1, -s1, 0, u, v);
		v5.addVertexWithUV(s1, -s1, 0, du, v);
		v5.addVertexWithUV(s1, s1, 0, du, dv);
		v5.addVertexWithUV(-s1, s1, 0, u, dv);
		v5.setColorOpaque_I(c2);
		v5.addVertexWithUV(-s2, -s2, 0, u, v);
		v5.addVertexWithUV(s2, -s2, 0, du, v);
		v5.addVertexWithUV(s2, s2, 0, du, dv);
		v5.addVertexWithUV(-s2, s2, 0, u, dv);
		v5.draw();
		GL11.glPopMatrix();
		GL11.glPopAttrib();

		double px = eb.lastTickPosX+(eb.posX-eb.lastTickPosX)*ptick;
		double py = eb.lastTickPosY+(eb.posY-eb.lastTickPosY)*ptick;
		double pz = eb.lastTickPosZ+(eb.posZ-eb.lastTickPosZ)*ptick;
		EntityFX fx = eb.spawnLifeParticle(px, py, pz);
		if (fx != null) {
			if (fx instanceof EntityBlurFX) {
				((EntityBlurFX)fx).setRapidExpand();
			}
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);
		}
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity e) {
		return null;
	}

}
