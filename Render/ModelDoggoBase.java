package Reika.Satisforestry.Render;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

import Reika.Satisforestry.Entity.EntityLizardDoggo;

public class ModelDoggoBase extends ModelBase {

	//fields
	protected ModelRenderer EarAttachmentR;
	protected ModelRenderer Body;
	protected ModelRenderer Body2;
	protected ModelRenderer Leg1;
	protected ModelRenderer Leg2;
	protected ModelRenderer Leg3;
	protected ModelRenderer Leg4;
	protected ModelRenderer Tail3;
	protected ModelRenderer Nose;
	protected ModelRenderer TailFin;
	protected ModelRenderer Tail2;
	protected ModelRenderer Tail;
	protected ModelRenderer BackScale5;
	protected ModelRenderer Scale2;
	protected ModelRenderer Scale3;
	protected ModelRenderer Scale5;
	protected ModelRenderer Scale4;
	protected ModelRenderer Scale1;
	protected ModelRenderer BackScale1;
	protected ModelRenderer BackScale2;
	protected ModelRenderer BackScale3;
	protected ModelRenderer Tongue;
	protected ModelRenderer EarR;
	protected ModelRenderer EarAttachmentL;
	protected ModelRenderer EarL;
	protected ModelRenderer Jaw1;
	protected ModelRenderer Neck;
	protected ModelRenderer Jaw2;
	protected ModelRenderer Head2;
	protected ModelRenderer Head1;
	protected ModelRenderer BackScale4;

	protected final Collection<ModelRenderer> headParts = new ArrayList();
	protected final HashMap<ModelRenderer, Float> scaleParts = new HashMap();

	protected double rightEarX;
	protected double leftEarX;

	protected void init() {
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

	protected final void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	@Override
	public final void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity e) {
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

		Neck.rotateAngleX += Math.toRadians(10);
		Tongue.rotateAngleY += Math.toRadians(30);

		Body.rotateAngleX = ((float)Math.PI / 2F);
		Body2.rotateAngleX = ((float)Math.PI / 2F);

		Leg1.rotateAngleX = MathHelper.cos(f * 0.6662F) * 1.4F * f1;
		Leg2.rotateAngleX = MathHelper.cos(f * 0.6662F + (float)Math.PI) * 1.4F * f1;
		Leg3.rotateAngleX = MathHelper.cos(f * 0.6662F + (float)Math.PI) * 1.4F * f1;
		Leg4.rotateAngleX = MathHelper.cos(f * 0.6662F) * 1.4F * f1;
	}

	@Override
	public final void setLivingAnimations(EntityLivingBase e, float a, float b, float c) {

	}
}
