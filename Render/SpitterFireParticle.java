package Reika.Satisforestry.Render;

import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Effects.EntityBlurFX;
import Reika.DragonAPI.Libraries.IO.ReikaTextureHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.Satisforestry.Entity.EntitySpitter.SpitterType;


public class SpitterFireParticle extends EntityBlurFX {

	private static IIcon icon = ReikaTextureHelper.getMissingIcon();

	private double rotationSpeed;
	private SpitterType type;

	private boolean canFade = true;

	public SpitterFireParticle(World world, double x, double y, double z, SpitterType s) {
		this(world, x, y, z, 0, 0, 0, s);
	}

	public SpitterFireParticle(World world, double x, double y, double z, double vx, double vy, double vz, SpitterType s) {
		super(world, x, y, z, vx, vy, vz, icon);
		rotationSpeed = ReikaRandomHelper.getRandomPlusMinus(0, 1.5);
		type = s;
		this.setParticleIcon(icon);
	}

	@Override
	protected void onSetColliding() {
		canFade = false;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (!canFade) {
			particleAge = Math.min(particleAge, this.getMaximumSizeAge());
		}
	}

	@Override
	protected void onCollision() {
		//particleGravity *= 0.5;
		//motionY = ReikaRandomHelper.getRandomBetween(0.33, 0.75);
		canFade = true;
	}

	public static void setFireIcon(IIcon ico) {
		icon = ico;
	}
	/*
	@Override
	public void renderParticle(Tessellator v5, float ptick, float yaw, float xz, float pitch, float yz, float xy) {
		//if (true) {
		//	particleAlpha = particleBlue = particleGreen = particleRed = 1;
		//	super.renderParticle(v5, ptick, yaw, xz, pitch, yz, xy);
		//	return;
		//}
		TessellatorVertexList tv5 = new TessellatorVertexList();
		float f6 = particleTextureIndexX / 16.0F;
		float f7 = f6 + 0.0624375F;
		float f8 = particleTextureIndexY / 16.0F;
		float f9 = f8 + 0.0624375F;
		float f10 = 0.1F * particleScale;

		if (particleIcon != null) {
			f6 = particleIcon.getMinU();
			f7 = particleIcon.getMaxU();
			f8 = particleIcon.getMinV();
			f9 = particleIcon.getMaxV();
		}

		float f11 = (float)(prevPosX + (posX - prevPosX) * ptick - interpPosX);
		float f12 = (float)(prevPosY + (posY - prevPosY) * ptick - interpPosY);
		float f13 = (float)(prevPosZ + (posZ - prevPosZ) * ptick - interpPosZ);
		tv5.addVertexWithUV(f11 - yaw * f10 - yz * f10, f12 - xz * f10, f13 - pitch * f10 - xy * f10, f7, f9);
		tv5.addVertexWithUV(f11 - yaw * f10 + yz * f10, f12 + xz * f10, f13 - pitch * f10 + xy * f10, f7, f8);
		tv5.addVertexWithUV(f11 + yaw * f10 + yz * f10, f12 + xz * f10, f13 + pitch * f10 + xy * f10, f6, f8);
		tv5.addVertexWithUV(f11 + yaw * f10 - yz * f10, f12 - xz * f10, f13 + pitch * f10 - xy * f10, f6, f9);

		double f = particleAge*rotationSpeed;
		tv5.rotateNonOrthogonal(0, f, 0, posX-RenderManager.renderPosX, posY-RenderManager.renderPosY, posZ-RenderManager.renderPosZ);
		v5.setColorRGBA_I(type.coreColor | 0xffffff, (int)(particleAlpha*255) | 255);
		tv5.render();
	}*/

}
