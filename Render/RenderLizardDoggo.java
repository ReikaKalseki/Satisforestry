package Reika.Satisforestry.Render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

import Reika.DragonAPI.Libraries.IO.ReikaTextureHelper;
import Reika.DragonAPI.Libraries.Java.ReikaGLHelper.BlendMode;
import Reika.DragonAPI.Libraries.Rendering.ReikaRenderHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Entity.EntityLizardDoggo;


public class RenderLizardDoggo extends RenderLiving {

	public RenderLizardDoggo() {
		super(new ModelLizardDoggo(), 0.7F);
		shadowSize *= 0.8;
	}

	@Override
	public void doRender(Entity e, double par2, double par4, double par6, float par8, float ptick) {
		e.rotationPitch = 5;
		super.doRender(e, par2, par4, par6, par8, ptick);

		EntityLizardDoggo el = (EntityLizardDoggo)e;
		EntityItem is = el.getHeldItemForRender();
		if (is != null) {
			GL11.glPushMatrix();
			GL11.glTranslatef((float)par2, (float)par4, (float)par6);
			this.renderHeldItem(el, is, ptick);
			GL11.glPopMatrix();
		}
	}

	private void renderHeldItem(EntityLizardDoggo te, EntityItem ei, float par8) {
		float tick = par8+te.ticksExisted;
		ei.age = 0;
		ei.hoverStart = 0;
		ei.rotationYaw = 0;
		//for (double dt = -0.0625; dt <= 0.0625; dt += 0.0625) {
		//double s = 2;
		//GL11.glTranslated(0.0625, 0.75, 0.875);
		//GL11.glRotated(te.rotationPitch, 1, 0, 0);
		//GL11.glRotated(-22.5, 1, 0, 0);
		Vec3 vec = te.getLookVec();
		GL11.glRotated(180-te.rotationYawHead, 0, 1, 0);
		GL11.glTranslated(vec.xCoord, te.getEyeHeight()+vec.yCoord, vec.zCoord);
		BlendMode.DEFAULT.apply();
		GL11.glEnable(GL11.GL_BLEND);
		double c = 0.2;
		double a = 1;
		GL11.glColor4d(c, c, c, a);
		ReikaRenderHelper.disableEntityLighting();
		RenderItem.renderInFrame = true;
		RenderManager.instance.renderEntityWithPosYaw(ei, 0, 0, 0, 0, 0/*tick*/);
		RenderItem.renderInFrame = false;
		ReikaRenderHelper.enableEntityLighting();
		GL11.glDisable(GL11.GL_BLEND);
		//}
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity e) {
		return null;
	}

	@Override
	protected void bindEntityTexture(Entity e) {
		ReikaTextureHelper.bindTexture(Satisforestry.class, "Textures/doggo.png");
	}

	@Override
	protected boolean func_110813_b(EntityLivingBase e) {
		return false;
	}

}
