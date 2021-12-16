package Reika.Satisforestry.Render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Effects.EntityBlurFX;
import Reika.DragonAPI.Libraries.Rendering.ReikaColorAPI;


public class EntitySlugStreak extends EntityBlurFX {

	private final Entity followTarget;

	private final double velX;
	private final double velY;
	private final double velZ;

	public EntitySlugStreak(World world, double x, double y, double z, double vx, double vy, double vz, IIcon ico, Entity follow) {
		super(world, x, y, z, vx, vy, vz, ico);
		followTarget = follow;
		velX = vx;
		velY = vy;
		velZ = vz;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		EntityBlurFX fx = new EntityBlurFX(worldObj, posX, posY, posZ, particleIcon);
		fx.setColor(ReikaColorAPI.RGBFtoHex(particleRed, particleGreen, particleBlue));
		fx.setScale(particleScale);
		fx.setLife(this.getMaxAge());
		if (this.isAlphaFade())
			fx.setAlphaFading();
		if (followTarget != null) {
			motionX = velX+followTarget.motionX;
			motionY = velY+followTarget.motionY+0.06;
			motionZ = velZ+followTarget.motionZ;
		}
		Minecraft.getMinecraft().effectRenderer.addEffect(fx);
	}

	@Override
	public void renderParticle(Tessellator v5, float par2, float par3, float par4, float par5, float par6, float par7) {

	}

}
