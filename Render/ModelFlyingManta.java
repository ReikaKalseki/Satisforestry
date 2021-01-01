// Date: 31/12/2020 7:30:06 PM
// Template version 1.1
// Java generated by Techne
// Keep in mind that you still need to fill in some blanks
// - ZeuX

package Reika.Satisforestry.Render;

import java.util.ArrayList;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

import Reika.Satisforestry.Entity.EntityFlyingManta;

public class ModelFlyingManta extends ModelBase
{
	//fields
	ModelPart Body2;
	ModelPart Tail;
	ModelPart BodyRidge;
	ModelPart Bulge2b;
	ModelPart Bulge3;
	ModelPart Bulge1;
	ModelPart Nose2;
	ModelPart Bulge2;
	ModelPart Nose;

	ModelPart LeftWing30;
	ModelPart LeftWing1;
	ModelPart LeftWing2;
	ModelPart LeftWing3;
	ModelPart LeftWing4;
	ModelPart LeftWing5;
	ModelPart LeftWing6;
	ModelPart LeftWing6b;
	ModelPart LeftWing7;
	ModelPart LeftWing8;
	ModelPart LeftWing9;
	ModelPart LeftWing10;
	ModelPart LeftWing11;
	ModelPart LeftWing12;
	ModelPart LeftWing13;
	ModelPart LeftWing14;
	ModelPart LeftWing15;
	ModelPart LeftWing16;
	ModelPart LeftWing17;
	ModelPart LeftWing18;
	ModelPart LeftWing19;
	ModelPart LeftWing20;
	ModelPart LeftWing21;
	ModelPart LeftWing22;
	ModelPart LeftWing23;
	ModelPart LeftWing24;
	ModelPart LeftWing25;
	ModelPart LeftWing26;
	ModelPart LeftWing27;
	ModelPart LeftWing28;
	ModelPart LeftWing29;

	private final ArrayList<ModelPart> bodyParts = new ArrayList();
	private final ArrayList<ModelPart> wingParts = new ArrayList();

	private boolean isWings = false;

	public ModelFlyingManta() {
		textureWidth = 512;
		textureHeight = 32;

		Body2 = new ModelPart(this, 0, 0);
		Body2.addBox(-2F, -2F, 42F, 4, 4, 16);
		Body2.setRotationPoint(0F, 8F, 0F);
		Body2.setTextureSize(512, 32);
		Body2.mirror = true;
		this.initializePart(Body2, 0F, 0F, 0F);
		Tail = new ModelPart(this, 0, 0);
		Tail.addBox(-1F, -1.5F, 58F, 2, 3, 16);
		Tail.setRotationPoint(0F, 8F, 0F);
		Tail.setTextureSize(512, 32);
		Tail.mirror = true;
		this.initializePart(Tail, 0F, 0F, 0F);
		BodyRidge = new ModelPart(this, 0, 0);
		BodyRidge.addBox(-2.5F, -2.5F, -6F, 5, 5, 48);
		BodyRidge.setRotationPoint(0F, 8F, 0F);
		BodyRidge.setTextureSize(512, 32);
		BodyRidge.mirror = true;
		this.initializePart(BodyRidge, 0F, 0F, 0F);
		Bulge2b = new ModelPart(this, 0, 0);
		Bulge2b.addBox(-4F, -3F, -1.5F, 8, 3, 14);
		Bulge2b.setRotationPoint(0F, 8F, 0F);
		Bulge2b.setTextureSize(512, 32);
		Bulge2b.mirror = true;
		this.initializePart(Bulge2b, 0F, 0F, 0F);
		Bulge3 = new ModelPart(this, 0, 0);
		Bulge3.addBox(-1.5F, -4.5F, -1.5F, 3, 1, 14);
		Bulge3.setRotationPoint(0F, 8F, 0F);
		Bulge3.setTextureSize(512, 32);
		Bulge3.mirror = true;
		this.initializePart(Bulge3, 0F, 0F, 0F);
		Bulge1 = new ModelPart(this, 0, 0);
		Bulge1.addBox(-2F, -3.5F, -4F, 4, 3, 19);
		Bulge1.setRotationPoint(0F, 8F, 0F);
		Bulge1.setTextureSize(512, 32);
		Bulge1.mirror = true;
		this.initializePart(Bulge1, 0F, 0F, 0F);
		Nose2 = new ModelPart(this, 0, 0);
		Nose2.addBox(-2F, -1.5F, -7F, 4, 4, 2);
		Nose2.setRotationPoint(0F, 8F, 0F);
		Nose2.setTextureSize(512, 32);
		Nose2.mirror = true;
		this.initializePart(Nose2, 0F, 0F, 0F);
		Bulge2 = new ModelPart(this, 0, 0);
		Bulge2.addBox(-3F, -3.4F, -3F, 6, 3, 17);
		Bulge2.setRotationPoint(0F, 8F, 0F);
		Bulge2.setTextureSize(512, 32);
		Bulge2.mirror = true;
		this.initializePart(Bulge2, 0F, 0F, 0F);
		Nose = new ModelPart(this, 0, 0);
		Nose.addBox(-1F, 0F, -8F, 2, 2, 1);
		Nose.setRotationPoint(0F, 8F, 0F);
		Nose.setTextureSize(512, 32);
		Nose.mirror = true;
		this.initializePart(Nose, 0F, 0F, 0F);

		isWings = true;
		LeftWing30 = new ModelPart(this, 0, 0);
		LeftWing30.addBox(61F, -1F, 25F, 2, 2, 10);
		LeftWing30.setRotationPoint(0F, 8F, 0F);
		LeftWing30.setTextureSize(512, 32);
		LeftWing30.mirror = true;
		this.initializePart(LeftWing30, 0F, 0F, 0F);
		LeftWing1 = new ModelPart(this, 0, 0);
		LeftWing1.addBox(1F, -1F, -5F, 2, 2, 78);
		LeftWing1.setRotationPoint(0F, 8F, 0F);
		LeftWing1.setTextureSize(512, 32);
		LeftWing1.mirror = true;
		this.initializePart(LeftWing1, 0F, 0F, 0F);
		LeftWing2 = new ModelPart(this, 0, 0);
		LeftWing2.addBox(3F, -1F, -5F, 2, 2, 76);
		LeftWing2.setRotationPoint(0F, 8F, 0F);
		LeftWing2.setTextureSize(512, 32);
		LeftWing2.mirror = true;
		this.initializePart(LeftWing2, 0F, 0F, 0F);
		LeftWing3 = new ModelPart(this, 0, 0);
		LeftWing3.addBox(5F, -1F, -5F, 2, 2, 74);
		LeftWing3.setRotationPoint(0F, 8F, 0F);
		LeftWing3.setTextureSize(512, 32);
		LeftWing3.mirror = true;
		this.initializePart(LeftWing3, 0F, 0F, 0F);
		LeftWing4 = new ModelPart(this, 0, 0);
		LeftWing4.addBox(7F, -1F, -5F, 2, 2, 72);
		LeftWing4.setRotationPoint(0F, 8F, 0F);
		LeftWing4.setTextureSize(512, 32);
		LeftWing4.mirror = true;
		this.initializePart(LeftWing4, 0F, 0F, 0F);
		LeftWing5 = new ModelPart(this, 0, 0);
		LeftWing5.addBox(9F, -1F, -4F, 2, 2, 69);
		LeftWing5.setRotationPoint(0F, 8F, 0F);
		LeftWing5.setTextureSize(512, 32);
		LeftWing5.mirror = true;
		this.initializePart(LeftWing5, 0F, 0F, 0F);
		LeftWing6 = new ModelPart(this, 0, 0);
		LeftWing6.addBox(11F, -1F, -4F, 2, 2, 67);
		LeftWing6.setRotationPoint(0F, 8F, 0F);
		LeftWing6.setTextureSize(512, 32);
		LeftWing6.mirror = true;
		this.initializePart(LeftWing6, 0F, 0F, 0F);
		LeftWing6b = new ModelPart(this, 0, 0);
		LeftWing6b.addBox(13F, -1F, -4F, 2, 2, 65);
		LeftWing6b.setRotationPoint(0F, 8F, 0F);
		LeftWing6b.setTextureSize(512, 32);
		LeftWing6b.mirror = true;
		this.initializePart(LeftWing6b, 0F, 0F, 0F);
		LeftWing7 = new ModelPart(this, 0, 0);
		LeftWing7.addBox(15F, -1F, -4F, 2, 2, 63);
		LeftWing7.setRotationPoint(0F, 8F, 0F);
		LeftWing7.setTextureSize(512, 32);
		LeftWing7.mirror = true;
		this.initializePart(LeftWing7, 0F, 0F, 0F);
		LeftWing8 = new ModelPart(this, 0, 0);
		LeftWing8.addBox(17F, -1F, -3F, 2, 2, 60);
		LeftWing8.setRotationPoint(0F, 8F, 0F);
		LeftWing8.setTextureSize(512, 32);
		LeftWing8.mirror = true;
		this.initializePart(LeftWing8, 0F, 0F, 0F);
		LeftWing9 = new ModelPart(this, 0, 0);
		LeftWing9.addBox(19F, -1F, -3F, 2, 2, 59);
		LeftWing9.setRotationPoint(0F, 8F, 0F);
		LeftWing9.setTextureSize(512, 32);
		LeftWing9.mirror = true;
		this.initializePart(LeftWing9, 0F, 0F, 0F);
		LeftWing10 = new ModelPart(this, 0, 0);
		LeftWing10.addBox(21F, -1F, -3F, 2, 2, 57);
		LeftWing10.setRotationPoint(0F, 8F, 0F);
		LeftWing10.setTextureSize(512, 32);
		LeftWing10.mirror = true;
		this.initializePart(LeftWing10, 0F, 0F, 0F);
		LeftWing11 = new ModelPart(this, 0, 0);
		LeftWing11.addBox(23F, -1F, -2F, 2, 2, 55);
		LeftWing11.setRotationPoint(0F, 8F, 0F);
		LeftWing11.setTextureSize(512, 32);
		LeftWing11.mirror = true;
		this.initializePart(LeftWing11, 0F, 0F, 0F);
		LeftWing12 = new ModelPart(this, 0, 0);
		LeftWing12.addBox(25F, -1F, -2F, 2, 2, 54);
		LeftWing12.setRotationPoint(0F, 8F, 0F);
		LeftWing12.setTextureSize(512, 32);
		LeftWing12.mirror = true;
		this.initializePart(LeftWing12, 0F, 0F, 0F);
		LeftWing13 = new ModelPart(this, 0, 0);
		LeftWing13.addBox(27F, -1F, -1F, 2, 2, 52);
		LeftWing13.setRotationPoint(0F, 8F, 0F);
		LeftWing13.setTextureSize(512, 32);
		LeftWing13.mirror = true;
		this.initializePart(LeftWing13, 0F, 0F, 0F);
		LeftWing14 = new ModelPart(this, 0, 0);
		LeftWing14.addBox(29F, -1F, -1F, 2, 2, 51);
		LeftWing14.setRotationPoint(0F, 8F, 0F);
		LeftWing14.setTextureSize(512, 32);
		LeftWing14.mirror = true;
		this.initializePart(LeftWing14, 0F, 0F, 0F);
		LeftWing15 = new ModelPart(this, 0, 0);
		LeftWing15.addBox(31F, -1F, 0F, 2, 2, 49);
		LeftWing15.setRotationPoint(0F, 8F, 0F);
		LeftWing15.setTextureSize(512, 32);
		LeftWing15.mirror = true;
		this.initializePart(LeftWing15, 0F, 0F, 0F);
		LeftWing16 = new ModelPart(this, 0, 0);
		LeftWing16.addBox(33F, -1F, 1F, 2, 2, 47);
		LeftWing16.setRotationPoint(0F, 8F, 0F);
		LeftWing16.setTextureSize(512, 32);
		LeftWing16.mirror = true;
		this.initializePart(LeftWing16, 0F, 0F, 0F);
		LeftWing17 = new ModelPart(this, 0, 0);
		LeftWing17.addBox(35F, -1F, 2F, 2, 2, 45);
		LeftWing17.setRotationPoint(0F, 8F, 0F);
		LeftWing17.setTextureSize(512, 32);
		LeftWing17.mirror = true;
		this.initializePart(LeftWing17, 0F, 0F, 0F);
		LeftWing18 = new ModelPart(this, 0, 0);
		LeftWing18.addBox(37F, -1F, 3F, 2, 2, 43);
		LeftWing18.setRotationPoint(0F, 8F, 0F);
		LeftWing18.setTextureSize(512, 32);
		LeftWing18.mirror = true;
		this.initializePart(LeftWing18, 0F, 0F, 0F);
		LeftWing19 = new ModelPart(this, 0, 0);
		LeftWing19.addBox(39F, -1F, 4F, 2, 2, 41);
		LeftWing19.setRotationPoint(0F, 8F, 0F);
		LeftWing19.setTextureSize(512, 32);
		LeftWing19.mirror = true;
		this.initializePart(LeftWing19, 0F, 0F, 0F);
		LeftWing20 = new ModelPart(this, 0, 0);
		LeftWing20.addBox(41F, -1F, 5F, 2, 2, 39);
		LeftWing20.setRotationPoint(0F, 8F, 0F);
		LeftWing20.setTextureSize(512, 32);
		LeftWing20.mirror = true;
		this.initializePart(LeftWing20, 0F, 0F, 0F);
		LeftWing21 = new ModelPart(this, 0, 0);
		LeftWing21.addBox(43F, -1F, 7F, 2, 2, 36);
		LeftWing21.setRotationPoint(0F, 8F, 0F);
		LeftWing21.setTextureSize(512, 32);
		LeftWing21.mirror = true;
		this.initializePart(LeftWing21, 0F, 0F, 0F);
		LeftWing22 = new ModelPart(this, 0, 0);
		LeftWing22.addBox(45F, -1F, 9F, 2, 2, 33);
		LeftWing22.setRotationPoint(0F, 8F, 0F);
		LeftWing22.setTextureSize(512, 32);
		LeftWing22.mirror = true;
		this.initializePart(LeftWing22, 0F, 0F, 0F);
		LeftWing23 = new ModelPart(this, 0, 0);
		LeftWing23.addBox(47F, -1F, 11F, 2, 2, 30);
		LeftWing23.setRotationPoint(0F, 8F, 0F);
		LeftWing23.setTextureSize(512, 32);
		LeftWing23.mirror = true;
		this.initializePart(LeftWing23, 0F, 0F, 0F);
		LeftWing24 = new ModelPart(this, 0, 0);
		LeftWing24.addBox(49F, -1F, 13F, 2, 2, 27);
		LeftWing24.setRotationPoint(0F, 8F, 0F);
		LeftWing24.setTextureSize(512, 32);
		LeftWing24.mirror = true;
		this.initializePart(LeftWing24, 0F, 0F, 0F);
		LeftWing25 = new ModelPart(this, 0, 0);
		LeftWing25.addBox(51F, -1F, 15F, 2, 2, 25);
		LeftWing25.setRotationPoint(0F, 8F, 0F);
		LeftWing25.setTextureSize(512, 32);
		LeftWing25.mirror = true;
		this.initializePart(LeftWing25, 0F, 0F, 0F);
		LeftWing26 = new ModelPart(this, 0, 0);
		LeftWing26.addBox(53F, -1F, 17F, 2, 2, 22);
		LeftWing26.setRotationPoint(0F, 8F, 0F);
		LeftWing26.setTextureSize(512, 32);
		LeftWing26.mirror = true;
		this.initializePart(LeftWing26, 0F, 0F, 0F);
		LeftWing27 = new ModelPart(this, 0, 0);
		LeftWing27.addBox(55F, -1F, 19F, 2, 2, 20);
		LeftWing27.setRotationPoint(0F, 8F, 0F);
		LeftWing27.setTextureSize(512, 32);
		LeftWing27.mirror = true;
		this.initializePart(LeftWing27, 0F, 0F, 0F);
		LeftWing28 = new ModelPart(this, 0, 0);
		LeftWing28.addBox(57F, -1F, 21F, 2, 2, 17);
		LeftWing28.setRotationPoint(0F, 8F, 0F);
		LeftWing28.setTextureSize(512, 32);
		LeftWing28.mirror = true;
		this.initializePart(LeftWing28, 0F, 0F, 0F);
		LeftWing29 = new ModelPart(this, 0, 0);
		LeftWing29.addBox(59F, -1F, 23F, 2, 2, 14);
		LeftWing29.setRotationPoint(0F, 8F, 0F);
		LeftWing29.setTextureSize(512, 32);
		LeftWing29.mirror = true;
		this.initializePart(LeftWing29, 0F, 0F, 0F);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		Body2.render(f5);
		Tail.render(f5);
		BodyRidge.render(f5);
		Bulge2b.render(f5);
		Bulge3.render(f5);
		Bulge1.render(f5);
		Nose2.render(f5);
		Bulge2.render(f5);
		Nose.render(f5);

		double t0 = System.currentTimeMillis()/750D;
		for (ModelPart p : wingParts) {
			double o = p.rootX-1;
			double t = t0+o*0.015;
			double ft = o/70D;
			p.offsetY = (float)(((EntityFlyingManta)entity).getWingDeflection()*ft*Math.sin(t));
			p.render(f5);

			p.offsetX = -p.rootX/8;
			p.render(f5);
			p.offsetX = 0;
		}

		/*
		LeftWing30.render(f5);
		LeftWing1.render(f5);
		LeftWing2.render(f5);
		LeftWing3.render(f5);
		LeftWing4.render(f5);
		LeftWing5.render(f5);
		LeftWing6.render(f5);
		LeftWing6b.render(f5);
		LeftWing7.render(f5);
		LeftWing8.render(f5);
		LeftWing9.render(f5);
		LeftWing10.render(f5);
		LeftWing11.render(f5);
		LeftWing12.render(f5);
		LeftWing13.render(f5);
		LeftWing14.render(f5);
		LeftWing15.render(f5);
		LeftWing16.render(f5);
		LeftWing17.render(f5);
		LeftWing18.render(f5);
		LeftWing19.render(f5);
		LeftWing20.render(f5);
		LeftWing21.render(f5);
		LeftWing22.render(f5);
		LeftWing23.render(f5);
		LeftWing24.render(f5);
		LeftWing25.render(f5);
		LeftWing26.render(f5);
		LeftWing27.render(f5);
		LeftWing28.render(f5);
		LeftWing29.render(f5);
		 */
	}

	private void initializePart(ModelPart model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
		bodyParts.add(model);
		if (isWings) {
			wingParts.add(model);
		}
	}

	private static class ModelPart extends ModelRenderer {

		private float rootX;
		private float rootY;
		private float rootZ;

		public ModelPart(ModelBase model, int tx, int ty) {
			super(model, tx, ty);
		}

		@Override
		public ModelRenderer addBox(float x, float y, float z, int sx, int sy, int sz) {
			rootX = x;
			rootY = y;
			rootZ = z;
			return super.addBox(x, y, z, sx, sy, sz);
		}

	}

}