package Reika.Satisforestry.Render;

import java.util.Locale;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import Reika.DragonAPI.Libraries.IO.ReikaTextureHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Entity.EntitySpitter;
import Reika.Satisforestry.Entity.EntitySpitter.SpitterType;


public class RenderSpitter extends RenderLiving {

	public RenderSpitter() {
		super(new ModelSpitter(), 1);
	}

	@Override
	public void doRender(Entity e, double par2, double par4, double par6, float par8, float ptick) {
		super.doRender(e, par2, par4, par6, par8, ptick);
	}

	@Override
	protected void preRenderCallback(EntityLivingBase e, float ptick) {
		GL11.glScalef(1.5F, 1.5F, 1.5F);
		SpitterType s = ((EntitySpitter)e).getSpitterType();
		if (s.isAlpha()) {
			GL11.glScalef(1.4F, 1.4F, 1.4F);
		}
		shadowSize = s.isAlpha() ? 1.1F : 0.8F;
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity e) {
		return null;
	}

	@Override
	protected void bindEntityTexture(Entity e) {
		SpitterType s = ((EntitySpitter)e).getSpitterType();
		ReikaTextureHelper.bindTexture(Satisforestry.class, "Textures/spitter_"+s.name().toLowerCase(Locale.ENGLISH)+".png");
	}

	@Override
	protected boolean func_110813_b(EntityLivingBase e) {
		return false;
	}

}
