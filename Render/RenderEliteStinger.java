package Reika.Satisforestry.Render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.entity.RenderSpider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.util.ResourceLocation;

import Reika.DragonAPI.Libraries.IO.ReikaRenderHelper;
import Reika.DragonAPI.Libraries.IO.ReikaTextureHelper;
import Reika.DragonAPI.Libraries.Java.ReikaGLHelper.BlendMode;
import Reika.Satisforestry.Satisforestry;


public class RenderEliteStinger extends RenderSpider {

	public RenderEliteStinger() {
		shadowSize *= 1.15F;
	}

	@Override
	public void doRender(Entity e, double par2, double par4, double par6, float par8, float ptick) {
		super.doRender(e, par2, par4, par6, par8, ptick);
	}

	@Override
	protected void preRenderCallback(EntityLivingBase e, float ptick) {
		GL11.glScalef(1.15F, 1.15F, 1.15F);
	}

	@Override
	protected int shouldRenderPass(EntitySpider e, int pass, float ptick)  {
		GL11.glEnable(GL11.GL_LIGHTING);
		ReikaRenderHelper.enableEntityLighting();
		if (pass != 0) {
			return -1;
		}
		else {
			ReikaTextureHelper.bindTexture(Satisforestry.class, "Textures/elite_stinger_eyes.png");
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glDisable(GL11.GL_LIGHTING);
			BlendMode.DEFAULT.apply();
			ReikaRenderHelper.disableEntityLighting();
			if (e.isInvisible()) {
				GL11.glDepthMask(false);
			}
			else {
				GL11.glDepthMask(true);
			}
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			return 1;
		}
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity e) {
		return null;
	}

	@Override
	protected void bindEntityTexture(Entity e) {
		ReikaTextureHelper.bindTexture(Satisforestry.class, "Textures/elite_stinger.png");
	}

	@Override
	protected boolean func_110813_b(EntityLivingBase e) {
		return false;
	}

}
