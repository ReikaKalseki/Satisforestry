// Date: 06/01/2021 7:59:23 PM
// Template version 1.1
// Java generated by Techne
// Keep in mind that you still need to fill in some blanks
// - ZeuX

package Reika.Satisforestry.Render;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.Satisforestry.Entity.EntityLizardDoggo;
import Reika.Satisforestry.Entity.EntityLizardDoggo.DoggoFlags;

public class ModelLizardDoggo extends ModelBase {


	//fields
	ModelRenderer EarAttachmentR;
	ModelRenderer Body;
	ModelRenderer Body2;
	ModelRenderer Leg1;
	ModelRenderer Leg2;
	ModelRenderer Leg3;
	ModelRenderer Leg4;
	ModelRenderer Tail3;
	ModelRenderer Nose;
	ModelRenderer TailFin;
	ModelRenderer Tail2;
	ModelRenderer Tail;
	ModelRenderer BackScale5;
	ModelRenderer Scale2;
	ModelRenderer Scale3;
	ModelRenderer Scale5;
	ModelRenderer Scale4;
	ModelRenderer Scale1;
	ModelRenderer BackScale1;
	ModelRenderer BackScale2;
	ModelRenderer BackScale3;
	ModelRenderer Tongue;
	ModelRenderer EarR;
	ModelRenderer EarAttachmentL;
	ModelRenderer EarL;
	ModelRenderer Jaw1;
	ModelRenderer Neck;
	ModelRenderer Jaw2;
	ModelRenderer Head2;
	ModelRenderer Head1;
	ModelRenderer BackScale4;

	private final Collection<ModelRenderer> headParts = new ArrayList();
	private final HashMap<ModelRenderer, Float> scaleParts = new HashMap();

	private double rightEarXTarget;
	private double rightEarX;
	private double leftEarXTarget;
	private double leftEarX;

	public ModelLizardDoggo() {
		textureWidth = 128;
		textureHeight = 64;

		EarAttachmentR = new ModelRenderer(this, 122, 14);
		EarAttachmentR.addBox(-5.2F, -2.8F, 1.8F, 1, 3, 1);
		EarAttachmentR.setRotationPoint(-1F, 13.5F, -7F);
		EarAttachmentR.setTextureSize(128, 64);
		EarAttachmentR.mirror = true;
		this.setRotation(EarAttachmentR, 0F, 0F, 0.5235988F);
		Body = new ModelRenderer(this, 51, 16);
		Body.addBox(-4.5F, 0F, -5F, 7, 7, 6);
		Body.setRotationPoint(0F, 14F, 2F);
		Body.setTextureSize(128, 64);
		Body.mirror = true;
		this.setRotation(Body, 1.570796F, 0F, 0F);
		Body2 = new ModelRenderer(this, 52, 0);
		Body2.addBox(-4F, -3F, -5F, 8, 8, 7);
		Body2.setRotationPoint(-1F, 14F, -3F);
		Body2.setTextureSize(128, 64);
		Body2.mirror = true;
		this.setRotation(Body2, 1.570796F, 0F, 0F);
		Leg1 = new ModelRenderer(this, 100, 27);
		Leg1.addBox(-3.5F, 0F, -2F, 3, 8, 3);
		Leg1.setRotationPoint(-2.5F, 16F, 7F);
		Leg1.setTextureSize(128, 64);
		Leg1.mirror = true;
		this.setRotation(Leg1, 0F, 0F, 0F);
		Leg2 = new ModelRenderer(this, 115, 27);
		Leg2.addBox(0.5F, 0F, -2F, 3, 8, 3);
		Leg2.setRotationPoint(0.5F, 16F, 7F);
		Leg2.setTextureSize(128, 64);
		Leg2.mirror = true;
		this.setRotation(Leg2, 0F, 0F, 0F);
		Leg3 = new ModelRenderer(this, 100, 40);
		Leg3.addBox(-3.2F, 0F, -1F, 3, 8, 3);
		Leg3.setRotationPoint(-2.5F, 16F, -4F);
		Leg3.setTextureSize(128, 64);
		Leg3.mirror = true;
		this.setRotation(Leg3, 0F, 0F, 0F);
		Leg4 = new ModelRenderer(this, 115, 40);
		Leg4.addBox(0.2F, 0F, -1F, 3, 8, 3);
		Leg4.setRotationPoint(0.5F, 16F, -4F);
		Leg4.setTextureSize(128, 64);
		Leg4.mirror = true;
		this.setRotation(Leg4, 0F, 0F, 0F);
		Tail3 = new ModelRenderer(this, 116, 0);
		Tail3.addBox(-1F, 13F, -1F, 2, 4, 2);
		Tail3.setRotationPoint(-1F, 12F, 8F);
		Tail3.setTextureSize(128, 64);
		Tail3.mirror = true;
		this.setRotation(Tail3, 1.308997F, 0F, 0F);
		Nose = new ModelRenderer(this, 102, 19);
		Nose.addBox(-2F, -4.2F, -6F, 4, 3, 3);
		Nose.setRotationPoint(-1F, 13.5F, -7F);
		Nose.setTextureSize(128, 64);
		Nose.mirror = true;
		this.setRotation(Nose, 0F, 0F, 0F);
		TailFin = new ModelRenderer(this, 0, 56);
		TailFin.addBox(-2F, 16.8F, -0.1F, 4, 3, 1);
		TailFin.setRotationPoint(-1F, 12F, 8F);
		TailFin.setTextureSize(128, 64);
		TailFin.mirror = true;
		this.setRotation(TailFin, 1.308997F, 0F, 0F);
		Tail2 = new ModelRenderer(this, 102, 0);
		Tail2.addBox(-1.5F, 7F, -3.2F, 3, 6, 3);
		Tail2.setRotationPoint(-1F, 12F, 8F);
		Tail2.setTextureSize(128, 64);
		Tail2.mirror = true;
		this.setRotation(Tail2, 1.43117F, 0F, 0F);
		Tail = new ModelRenderer(this, 84, 0);
		Tail.addBox(-2F, 1F, -5F, 4, 7, 4);
		Tail.setRotationPoint(-1F, 12F, 8F);
		Tail.setTextureSize(128, 64);
		Tail.mirror = true;
		this.setRotation(Tail, 1.570796F, 0F, 0F);
		BackScale5 = new ModelRenderer(this, 0, 0);
		BackScale5.addBox(-2F, 1F, -0.5F, 4, 3, 1);
		BackScale5.setRotationPoint(-1F, 12F, -7F);
		BackScale5.setTextureSize(128, 64);
		BackScale5.mirror = true;
		this.setRotation(BackScale5, 1.710423F, 0F, 0F);
		Scale2 = new ModelRenderer(this, 0, 34);
		Scale2.addBox(-2.5F, 4F, -1.3F, 5, 3, 1);
		Scale2.setRotationPoint(-1F, 12F, 8F);
		Scale2.setTextureSize(128, 64);
		Scale2.mirror = true;
		this.setRotation(Scale2, 1.623156F, 0F, 0F);
		Scale3 = new ModelRenderer(this, 0, 40);
		Scale3.addBox(-2F, 7F, -1F, 4, 3, 1);
		Scale3.setRotationPoint(-1F, 12F, 8F);
		Scale3.setTextureSize(128, 64);
		Scale3.mirror = true;
		this.setRotation(Scale3, 1.53589F, 0F, 0F);
		Scale5 = new ModelRenderer(this, 0, 50);
		Scale5.addBox(-1.5F, 13F, 0F, 3, 3, 1);
		Scale5.setRotationPoint(-1F, 12F, 8F);
		Scale5.setTextureSize(128, 64);
		Scale5.mirror = true;
		this.setRotation(Scale5, 1.361357F, 0F, 0F);
		Scale4 = new ModelRenderer(this, 0, 45);
		Scale4.addBox(-1.5F, 10F, -0.6F, 3, 3, 1);
		Scale4.setRotationPoint(-1F, 12F, 8F);
		Scale4.setTextureSize(128, 64);
		Scale4.mirror = true;
		this.setRotation(Scale4, 1.448623F, 0F, 0F);
		Scale1 = new ModelRenderer(this, 0, 28);
		Scale1.addBox(-2.5F, 1F, -1.3F, 5, 3, 1);
		Scale1.setRotationPoint(-1F, 12F, 8F);
		Scale1.setTextureSize(128, 64);
		Scale1.mirror = true;
		this.setRotation(Scale1, 1.710423F, 0F, 0F);
		BackScale1 = new ModelRenderer(this, 0, 22);
		BackScale1.addBox(-2.5F, 1F, -1.3F, 5, 3, 1);
		BackScale1.setRotationPoint(-1F, 12F, 5F);
		BackScale1.setTextureSize(128, 64);
		BackScale1.mirror = true;
		this.setRotation(BackScale1, 1.710423F, 0F, 0F);
		BackScale2 = new ModelRenderer(this, 0, 15);
		BackScale2.addBox(-2F, 0F, -1.3F, 4, 4, 1);
		BackScale2.setRotationPoint(-1F, 12F, 2F);
		BackScale2.setTextureSize(128, 64);
		BackScale2.mirror = true;
		this.setRotation(BackScale2, 1.710423F, 0F, 0F);
		BackScale3 = new ModelRenderer(this, 0, 10);
		BackScale3.addBox(-2F, 1F, -0.8F, 4, 3, 1);
		BackScale3.setRotationPoint(-1F, 12F, -1F);
		BackScale3.setTextureSize(128, 64);
		BackScale3.mirror = true;
		this.setRotation(BackScale3, 1.710423F, 0F, 0F);
		Tongue = new ModelRenderer(this, 80, 40);
		Tongue.addBox(1F, -1F, -3F, 5, 1, 2);
		Tongue.setRotationPoint(-1F, 13.5F, -7F);
		Tongue.setTextureSize(128, 64);
		Tongue.mirror = true;
		this.setRotation(Tongue, 0F, 0.5235988F, 0F);
		EarR = new ModelRenderer(this, 88, 44);
		EarR.addBox(-4.8F, -2F, 1.8F, 1, 12, 1);
		EarR.setRotationPoint(-1F, 13.5F, -7F);
		EarR.setTextureSize(128, 64);
		EarR.mirror = true;
		this.setRotation(EarR, 0F, 0F, 0.0872665F);
		EarAttachmentL = new ModelRenderer(this, 122, 9);
		EarAttachmentL.addBox(4.2F, -2.8F, 1.8F, 1, 3, 1);
		EarAttachmentL.setRotationPoint(-1F, 13.5F, -7F);
		EarAttachmentL.setTextureSize(128, 64);
		EarAttachmentL.mirror = true;
		this.setRotation(EarAttachmentL, 0F, 0F, -0.5235988F);
		EarL = new ModelRenderer(this, 94, 44);
		EarL.addBox(3.8F, -2F, 1.8F, 1, 12, 1);
		EarL.setRotationPoint(-1F, 13.5F, -7F);
		EarL.setTextureSize(128, 64);
		EarL.mirror = true;
		this.setRotation(EarL, 0F, 0F, -0.0872665F);
		Jaw1 = new ModelRenderer(this, 80, 29);
		Jaw1.addBox(-3F, -0.5F, -3F, 6, 1, 3);
		Jaw1.setRotationPoint(-1F, 13.5F, -7F);
		Jaw1.setTextureSize(128, 64);
		Jaw1.mirror = true;
		this.setRotation(Jaw1, 0.0872665F, 0F, 0F);
		Neck = new ModelRenderer(this, 84, 12);
		Neck.addBox(-2.5F, 0F, 0.2F, 5, 4, 1);
		Neck.setRotationPoint(-1F, 13.5F, -7F);
		Neck.setTextureSize(128, 64);
		Neck.mirror = true;
		this.setRotation(Neck, 0.1745329F, 0F, 0F);
		Jaw2 = new ModelRenderer(this, 80, 35);
		Jaw2.addBox(-2F, -0.3F, -6F, 4, 1, 3);
		Jaw2.setRotationPoint(-1F, 13.5F, -7F);
		Jaw2.setTextureSize(128, 64);
		Jaw2.mirror = true;
		this.setRotation(Jaw2, 0.0349066F, 0F, 0F);
		Head2 = new ModelRenderer(this, 102, 10);
		Head2.addBox(-3F, -5F, -3F, 6, 4, 3);
		Head2.setRotationPoint(-1F, 13.5F, -7F);
		Head2.setTextureSize(128, 64);
		Head2.mirror = true;
		this.setRotation(Head2, 0F, 0F, 0F);
		Head1 = new ModelRenderer(this, 79, 19);
		Head1.addBox(-3F, -5F, 0F, 6, 6, 3);
		Head1.setRotationPoint(-1F, 13.5F, -7F);
		Head1.setTextureSize(128, 64);
		Head1.mirror = true;
		this.setRotation(Head1, 0F, 0F, 0F);
		BackScale4 = new ModelRenderer(this, 0, 5);
		BackScale4.addBox(-2F, 1F, -0.5F, 4, 3, 1);
		BackScale4.setRotationPoint(-1F, 12F, -4F);
		BackScale4.setTextureSize(128, 64);
		BackScale4.mirror = true;
		this.setRotation(BackScale4, 1.710423F, 0F, 0F);

		headParts.add(EarAttachmentL);
		headParts.add(EarAttachmentR);
		headParts.add(EarL);
		headParts.add(EarR);
		headParts.add(Head1);
		headParts.add(Head2);
		headParts.add(Nose);
		headParts.add(Jaw1);
		headParts.add(Jaw2);
		headParts.add(Tongue);
		headParts.add(Neck);

		scaleParts.put(BackScale1, BackScale1.rotateAngleX);
		scaleParts.put(BackScale2, BackScale2.rotateAngleX);
		scaleParts.put(BackScale3, BackScale3.rotateAngleX);
		scaleParts.put(BackScale4, BackScale4.rotateAngleX);
		scaleParts.put(BackScale5, BackScale5.rotateAngleX);

		scaleParts.put(Scale1, Scale1.rotateAngleX);
		scaleParts.put(Scale2, Scale2.rotateAngleX);
		scaleParts.put(Scale3, Scale3.rotateAngleX);
		scaleParts.put(Scale4, Scale4.rotateAngleX);
		scaleParts.put(Scale5, Scale5.rotateAngleX);
	}

	@Override
	public void render(Entity e, float f, float f1, float f2, float f3, float f4, float f5) {
		EntityLizardDoggo el = (EntityLizardDoggo)e;
		GL11.glPushMatrix();
		if (DoggoFlags.BACKWARDS.get(el))
			GL11.glRotated(180, 0, 1, 0);
		super.render(e, f, f1, f2, f3, f4, f5);

		this.setRotationAngles(f, f1, f2, f3, f4, f5, e);

		boolean jump = DoggoFlags.JUMP.get(el);
		boolean lure = DoggoFlags.LURED.get(el);
		int tick = el.getSprintJumpTick();

		double dt = tick >= 4 ? (8-(tick-4))*0.018 : tick*0.036;
		if (jump)
			dt *= 1.5;
		else
			dt *= 2.5;

		double t = System.currentTimeMillis()/72D;
		double dy = 0;
		double ang = 0;
		boolean tame = el.isTamed();

		float sneeze1 = el.getSneezeTick1();
		float sneeze2 = el.getSneezeTick2();
		float sneeze = Math.max(Math.min(1, sneeze1*3), sneeze2);
		for (Entry<ModelRenderer, Float> en : scaleParts.entrySet()) {
			ModelRenderer mr = en.getKey();
			float base = en.getValue();
			mr.rotateAngleX = base+(float)Math.toRadians(30)*sneeze;
			float df = Math.abs(base-scaleParts.get(BackScale1))*1.25F;
			mr.offsetY = sneeze*0.03125F+df*sneeze;
		}

		if (tame) {
			dy = 0.008*Math.sin(t);
			ang = 1.8*Math.sin(t/2+238);
		}
		else if (lure) {
			dy = MathHelper.clamp_double(0.06*Math.sin(t*1.2), -0.04, 0.04);
			ang = 2.5*Math.sin(t/1.5+238);
		}
		if (sneeze2 >= 0.9) {
			ang += 350*Math.sin(11*t*(sneeze2-0.9)-349)*(sneeze2-0.9)*(sneeze2-0.9);
		}

		double stretch = 1;
		if (sneeze1 > 0.75) {
			stretch += sneeze1*0.375*(1-sneeze1);
		}
		else {
			stretch += sneeze1*0.125;
		}

		RenderLizardDoggo.instance.setOffsetsAndAngles(f, f1, f2, f3, f4, el.renderYawOffset, -dy);

		Tongue.rotateAngleY -= Math.toRadians(ang*2);
		Tongue.rotateAngleX -= dy*1.7;

		if (DoggoFlags.SPRINTING.get(el) || jump) {
			this.updateEars(el, jump);
		}
		else {
			leftEarX = 0;
			rightEarX = 0;
		}

		GL11.glPushMatrix();
		GL11.glScaled(1, 1, stretch);
		GL11.glTranslated(0, dy+dt, -(stretch-1));
		GL11.glRotated(ang, 0, 0, 1);
		EarAttachmentR.render(f5);
		Nose.render(f5);
		if (el.isTamed())
			Tongue.render(f5);
		EarAttachmentL.render(f5);

		if (!tame) {
			Jaw1.offsetY = -0.0625F;
			Jaw2.offsetY = -0.0625F;
		}
		Jaw1.render(f5);
		Jaw2.render(f5);
		if (!tame) {
			Jaw1.offsetY = 0;
			Jaw2.offsetY = 0;
		}

		Head2.render(f5);
		Head1.render(f5);

		EarL.render(f5);
		EarR.render(f5);
		GL11.glTranslated(0, -dy/2, 0);
		GL11.glRotated(-ang*0.67, 0, 0, 1);

		Neck.render(f5);
		Body.render(f5);
		Body2.render(f5);

		BackScale1.render(f5);
		BackScale2.render(f5);
		BackScale3.render(f5);
		BackScale4.render(f5);
		BackScale5.render(f5);

		GL11.glRotated(-ang*0.33, 0, 0, 1);
		GL11.glTranslated(0, -dy*0.75-dt/3, 0);

		TailFin.render(f5);
		Tail3.render(f5);
		Tail2.render(f5);
		Tail.render(f5);

		Scale2.render(f5);
		Scale3.render(f5);
		Scale5.render(f5);
		Scale4.render(f5);
		Scale1.render(f5);
		GL11.glPopMatrix();

		Leg1.render(f5);
		Leg2.render(f5);
		Leg3.render(f5);
		Leg4.render(f5);

		GL11.glPopMatrix();
	}

	private void updateEars(EntityLizardDoggo e, boolean jump) {
		//EarL.rotateAngleX = (float)(dt*7.5*Math.sin(t-3478));
		//EarL.rotateAngleZ = EarL.rotateAngleX;

		double v = 0.12*2;
		if (jump)
			v *= 1.25;

		if (ReikaMathLibrary.approxrAbs(rightEarXTarget, rightEarX, 0.3)) {
			rightEarXTarget = Math.toRadians(ReikaRandomHelper.getRandomBetween(0, 360));
		}
		if (rightEarXTarget > rightEarX) {
			rightEarX += v;
		}
		else {
			rightEarX -= v;
		}

		if (ReikaMathLibrary.approxrAbs(leftEarXTarget, leftEarX, 0.3)) {
			leftEarXTarget = Math.toRadians(ReikaRandomHelper.getRandomBetween(0, 360));
		}
		if (leftEarXTarget > leftEarX) {
			leftEarX += v;
		}
		else {
			leftEarX -= v;
		}
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	@Override
	public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity e) {
		super.setRotationAngles(f, f1, f2, f3, f4, f5, e);

		float sneeze = ((EntityLizardDoggo)e).getSneezeTick2();
		if (sneeze >= 0.8) {
			f3 += 250*Math.sin(12*e.ticksExisted*(sneeze-0.8)-349)*(sneeze-0.8)*(sneeze-0.8);
		}

		for (ModelRenderer head : headParts) {
			head.rotateAngleX = f4 / (180F / (float)Math.PI);
			head.rotateAngleY = f3 / (180F / (float)Math.PI);
			if (head == EarL) {
				head.rotateAngleX += leftEarX;
			}
			else if (head == EarR) {
				head.rotateAngleX += rightEarX;
			}
		}

		Tongue.rotateAngleY += Math.toRadians(25);

		Body.rotateAngleX = ((float)Math.PI / 2F);
		Body2.rotateAngleX = ((float)Math.PI / 2F);

		Leg1.rotateAngleX = MathHelper.cos(f * 0.6662F) * 1.4F * f1;
		Leg2.rotateAngleX = MathHelper.cos(f * 0.6662F + (float)Math.PI) * 1.4F * f1;
		Leg3.rotateAngleX = MathHelper.cos(f * 0.6662F + (float)Math.PI) * 1.4F * f1;
		Leg4.rotateAngleX = MathHelper.cos(f * 0.6662F) * 1.4F * f1;
	}

	@Override
	public void setLivingAnimations(EntityLivingBase e, float a, float b, float c) {

	}

}
