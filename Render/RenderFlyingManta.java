/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.Satisforestry.Render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import Reika.DragonAPI.Libraries.IO.ReikaTextureHelper;
import Reika.Satisforestry.Satisforestry;

public class RenderFlyingManta extends RendererLivingEntity {

	public RenderFlyingManta() {
		super(new ModelFlyingManta(), 2.5F);
	}

	@Override
	public void doRender(Entity e, double par2, double par4, double par6, float par8, float ptick) {
		GL11.glPushMatrix();
		double s = 2.5;
		//GL11.glTranslated(0, 0, 0);
		//GL11.glRotated(90, 0, 1, 0);
		//GL11.glScaled(s, s, s);
		super.doRender(e, par2, par4, par6, par8, ptick);
		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity e) {
		return null;
	}

	@Override
	protected void bindEntityTexture(Entity e) {
		ReikaTextureHelper.bindTexture(Satisforestry.class, "Textures/manta.png");
	}

	@Override
	protected boolean func_110813_b(EntityLivingBase e) {
		return false;
	}

}
