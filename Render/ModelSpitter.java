package Reika.Satisforestry.Render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelSpitter extends ModelBase
{
	//fields
	protected ModelRenderer Body;
	protected ModelRenderer Leg1U;
	protected ModelRenderer Leg2U;
	protected ModelRenderer Leg1L;
	protected ModelRenderer Forehead;
	protected ModelRenderer HeadR2;
	protected ModelRenderer HeadL2;
	protected ModelRenderer FaceL1;
	protected ModelRenderer FaceR1;
	protected ModelRenderer FaceR2;
	protected ModelRenderer FaceL2;
	protected ModelRenderer HeadL;
	protected ModelRenderer HeadR;
	protected ModelRenderer Leg3;
	protected ModelRenderer BodyBack;
	protected ModelRenderer Leg4;
	protected ModelRenderer Leg2L;
	protected ModelRenderer Neck;
	protected ModelRenderer Nose;

	public ModelSpitter() {
		textureWidth = 64;
		textureHeight = 64;

		Body = new ModelRenderer(this, 29, 0);
		Body.addBox(-4.5F, -7F, -3F, 9, 5, 7);
		Body.setRotationPoint(0F, 14F, 2F);
		Body.setTextureSize(64, 64);
		Body.mirror = true;
		this.setRotation(Body, 1.570796F, 0F, 0F);
		Leg1U = new ModelRenderer(this, 0, 13);
		Leg1U.addBox(-1.5F, 0F, -1.5F, 3, 6, 3);
		Leg1U.setRotationPoint(-3F, 13F, 4F);
		Leg1U.setTextureSize(64, 64);
		Leg1U.mirror = true;
		this.setRotation(Leg1U, 0.5235988F, 0F, 0F);
		Leg2U = new ModelRenderer(this, 0, 13);
		Leg2U.addBox(-1.5F, 0F, -1.5F, 3, 6, 3);
		Leg2U.setRotationPoint(3F, 13F, 4F);
		Leg2U.setTextureSize(64, 64);
		Leg2U.mirror = true;
		this.setRotation(Leg2U, 0.5235988F, 0F, 0F);
		Leg1L = new ModelRenderer(this, 13, 12);
		Leg1L.addBox(-1F, 0F, -1F, 2, 7, 2);
		Leg1L.setRotationPoint(-3F, 17F, 7F);
		Leg1L.setTextureSize(64, 64);
		Leg1L.mirror = true;
		this.setRotation(Leg1L, 0F, 0F, 0F);
		Forehead = new ModelRenderer(this, 50, 24);
		Forehead.addBox(-1F, -2.3F, -4.9F, 2, 2, 4);
		Forehead.setRotationPoint(0F, 11.5F, -7F);
		Forehead.setTextureSize(64, 64);
		Forehead.mirror = true;
		this.setRotation(Forehead, 0.1745329F, 0F, 0F);
		HeadR2 = new ModelRenderer(this, 30, 34);
		HeadR2.addBox(0.5F, -3F, 3F, 1, 1, 4);
		HeadR2.setRotationPoint(0F, 12.4F, -8.5F);
		HeadR2.setTextureSize(64, 64);
		HeadR2.mirror = true;
		this.setRotation(HeadR2, 0F, -1.047198F, 0F);
		HeadL2 = new ModelRenderer(this, 30, 40);
		HeadL2.addBox(-1.5F, -3F, 3F, 1, 1, 4);
		HeadL2.setRotationPoint(0F, 12.4F, -8.5F);
		HeadL2.setTextureSize(64, 64);
		HeadL2.mirror = true;
		this.setRotation(HeadL2, 0F, 1.047198F, 0F);
		FaceL1 = new ModelRenderer(this, 0, 34);
		FaceL1.addBox(0F, -3F, -2F, 2, 6, 5);
		FaceL1.setRotationPoint(0F, 13.5F, -9F);
		FaceL1.setTextureSize(64, 64);
		FaceL1.mirror = true;
		this.setRotation(FaceL1, 0F, 0.5235988F, 0F);
		FaceR1 = new ModelRenderer(this, 15, 34);
		FaceR1.addBox(-2F, -3F, -2F, 2, 6, 5);
		FaceR1.setRotationPoint(0F, 13.5F, -9F);
		FaceR1.setTextureSize(64, 64);
		FaceR1.mirror = true;
		this.setRotation(FaceR1, 0F, -0.5235988F, 0F);
		FaceR2 = new ModelRenderer(this, 15, 46);
		FaceR2.addBox(-1F, -3F, 2F, 2, 6, 5);
		FaceR2.setRotationPoint(0F, 13.5F, -8.5F);
		FaceR2.setTextureSize(64, 64);
		FaceR2.mirror = true;
		this.setRotation(FaceR2, 0F, -1.047198F, 0F);
		FaceL2 = new ModelRenderer(this, 0, 46);
		FaceL2.addBox(-1F, -3F, 2F, 2, 6, 5);
		FaceL2.setRotationPoint(0F, 13.5F, -8.5F);
		FaceL2.setTextureSize(64, 64);
		FaceL2.mirror = true;
		this.setRotation(FaceL2, 0F, 1.047198F, 0F);
		HeadL = new ModelRenderer(this, 25, 24);
		HeadL.addBox(-1F, -3F, -1F, 4, 1, 8);
		HeadL.setRotationPoint(0F, 12.5F, -8.5F);
		HeadL.setTextureSize(64, 64);
		HeadL.mirror = true;
		this.setRotation(HeadL, 0F, 1.047198F, 0.1745329F);
		HeadR = new ModelRenderer(this, 0, 24);
		HeadR.addBox(-3F, -3F, -1F, 4, 1, 8);
		HeadR.setRotationPoint(0F, 12.5F, -8.5F);
		HeadR.setTextureSize(64, 64);
		HeadR.mirror = true;
		this.setRotation(HeadR, 0F, -1.047198F, -0.1745329F);
		Leg3 = new ModelRenderer(this, 22, 13);
		Leg3.addBox(-1F, 0F, -1F, 2, 8, 2);
		Leg3.setRotationPoint(-3.5F, 16F, -4F);
		Leg3.setTextureSize(64, 64);
		Leg3.mirror = true;
		this.setRotation(Leg3, 0F, 0F, 0F);
		BodyBack = new ModelRenderer(this, 0, 0);
		BodyBack.addBox(-4F, -2F, -3F, 8, 5, 6);
		BodyBack.setRotationPoint(0F, 14F, 2F);
		BodyBack.setTextureSize(64, 64);
		BodyBack.mirror = true;
		this.setRotation(BodyBack, 1.570796F, 0F, 0F);
		Leg4 = new ModelRenderer(this, 22, 13);
		Leg4.addBox(-1F, 0F, -1F, 2, 8, 2);
		Leg4.setRotationPoint(3.5F, 16F, -4F);
		Leg4.setTextureSize(64, 64);
		Leg4.mirror = true;
		this.setRotation(Leg4, 0F, 0F, 0F);
		Leg2L = new ModelRenderer(this, 13, 12);
		Leg2L.addBox(-1F, 0F, -1F, 2, 7, 2);
		Leg2L.setRotationPoint(3F, 17F, 7F);
		Leg2L.setTextureSize(64, 64);
		Leg2L.mirror = true;
		this.setRotation(Leg2L, 0F, 0F, 0F);
		Neck = new ModelRenderer(this, 38, 13);
		Neck.addBox(-3F, -2F, -2F, 6, 4, 6);
		Neck.setRotationPoint(0F, 14F, -6F);
		Neck.setTextureSize(64, 64);
		Neck.mirror = true;
		this.setRotation(Neck, 1.570796F, 0F, 0F);
		Nose = new ModelRenderer(this, 50, 31);
		Nose.addBox(-1F, -2F, -5F, 2, 5, 4);
		Nose.setRotationPoint(0F, 13.5F, -7F);
		Nose.setTextureSize(64, 64);
		Nose.mirror = true;
		this.setRotation(Nose, 0F, 0F, 0F);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		this.setRotationAngles(f, f1, f2, f3, f4, f5, entity);

		Body.render(f5);
		BodyBack.render(f5);

		Leg1U.render(f5);
		Leg2L.render(f5);
		Leg2U.render(f5);
		Leg1L.render(f5);
		Leg3.render(f5);
		Leg4.render(f5);

		Neck.render(f5);

		Nose.render(f5);
		HeadL.render(f5);
		HeadR.render(f5);
		Forehead.render(f5);
		HeadR2.render(f5);
		HeadL2.render(f5);
		FaceL1.render(f5);
		FaceR1.render(f5);
		FaceR2.render(f5);
		FaceL2.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	@Override
	public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity e) {
		super.setRotationAngles(f, f1, f2, f3, f4, f5, e);

		Leg1L.rotateAngleX = MathHelper.cos(f * 0.6662F) * 1.4F * f1;
		Leg2L.rotateAngleX = MathHelper.cos(f * 0.6662F + (float)Math.PI) * 1.4F * f1;
		Leg3.rotateAngleX = MathHelper.cos(f * 0.6662F + (float)Math.PI) * 1.4F * f1;
		Leg4.rotateAngleX = MathHelper.cos(f * 0.6662F) * 1.4F * f1;
	}

}
