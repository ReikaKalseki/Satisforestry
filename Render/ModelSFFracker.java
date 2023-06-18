package Reika.Satisforestry.Render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.client.model.ModelFormatException;
import net.minecraftforge.client.model.obj.GroupObject;
import net.minecraftforge.client.model.obj.WavefrontObject;

import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Miner.TileFrackingPressurizer.Thumper;


public class ModelSFFracker extends WavefrontObject {

	private GroupObject chassis;
	private GroupObject lightbar;

	private final GroupObject[] thumpers = new GroupObject[4];
	private final GroupObject[] vents = new GroupObject[4];

	public ModelSFFracker(String path) throws ModelFormatException {
		super("SF Fracker", Satisforestry.class.getResourceAsStream(path));

		chassis = this.findGroupObject("ThumperBody");
		lightbar = this.findGroupObject("ThumperLightbar");
		thumpers[0] = this.findGroupObject("ThumperRod");
		thumpers[1] = this.findGroupObject("ThumperRod2");
		thumpers[2] = this.findGroupObject("ThumperRod3");
		thumpers[3] = this.findGroupObject("ThumperRod4");
		vents[0] = this.findGroupObject("ThumperVent");
		vents[1] = this.findGroupObject("ThumperVent2");
		vents[2] = this.findGroupObject("ThumperVent3");
		vents[3] = this.findGroupObject("ThumperVent4");
	}

	private GroupObject findGroupObject(String s) {
		for (GroupObject g : groupObjects) {
			if (g.name.equals(s))
				return g;
		}
		return null;
	}

	public void drawChassis(double ventExt) {
		if (chassis == null)
			return;
		chassis.render();
		int[][] ventOffsets = new int[4][2];
		ventOffsets[2][0] = 1;
		ventOffsets[2][1] = 0;
		ventOffsets[0][0] = -1;
		ventOffsets[0][1] = 0;
		ventOffsets[1][0] = 0;
		ventOffsets[1][1] = 1;
		ventOffsets[3][0] = 0;
		ventOffsets[3][1] = -1;
		for (int i = 0; i < vents.length; i++) {
			GL11.glPushMatrix();
			GL11.glTranslated(ventExt*0.125*ventOffsets[i][0], ventExt*0.125, ventExt*0.125*ventOffsets[i][1]);
			vents[i].render();
			GL11.glPopMatrix();
		}
	}

	public void drawThumper(Thumper t) {
		if (thumpers[t.index] == null)
			return;
		GL11.glPushMatrix();
		GL11.glTranslated(0, t.getRenderPosition()*1.5, 0);
		thumpers[t.index].render();
		GL11.glPopMatrix();
	}

	public void renderThumpers() {
		for (GroupObject go : thumpers)
			go.render();
	}

	public void drawLightbar(int c) {
		if (lightbar == null)
			return;
		Tessellator v5 = Tessellator.instance;
		v5.startDrawing(lightbar.glDrawingMode);
		v5.setBrightness(240);
		v5.setColorOpaque_I(c);
		lightbar.render(v5);
		v5.draw();
	}

}
