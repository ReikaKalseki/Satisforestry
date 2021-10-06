package Reika.Satisforestry.Render;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;


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

	}

	@Override
	protected ResourceLocation getEntityTexture(Entity e) {
		return null;
	}

	@Override
	protected void bindEntityTexture(Entity e) {

	}

	@Override
	protected boolean func_110813_b(EntityLivingBase e) {
		return false;
	}

}
