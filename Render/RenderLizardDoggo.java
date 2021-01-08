package Reika.Satisforestry.Render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.ResourceLocation;

import Reika.DragonAPI.Libraries.IO.ReikaTextureHelper;
import Reika.DragonAPI.Libraries.Java.ReikaGLHelper.BlendMode;
import Reika.DragonAPI.Libraries.Rendering.ReikaRenderHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Entity.EntityLizardDoggo;


public class RenderLizardDoggo extends RenderLiving {

	public static final RenderLizardDoggo instance = new RenderLizardDoggo();

	private float bodyYaw;
	private float relativeHeadYaw;
	private float headPitch;
	private double headBob;

	private RenderLizardDoggo() {
		super(new ModelLizardDoggo(), 0.7F);
		shadowSize *= 0.8;
	}

	@Override
	public void doRender(Entity e, double par2, double par4, double par6, float par8, float ptick) {
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
		GL11.glRotated(172-bodyYaw, 0, 1, 0);
		GL11.glTranslated(0, te.getEyeHeight()+0.12-headPitch/128+headBob, -0.875);
		GL11.glRotated(relativeHeadYaw*1.1, 0, 1, 0);
		GL11.glRotated(90-headPitch, 1, 0, 0);
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

	public void setOffsetsAndAngles(float f, float f1, float f2, float f3, float f4, float body, double dy) {
		headPitch = f4;
		relativeHeadYaw = f3;
		bodyYaw = body;
		headBob = dy;
	}

}
