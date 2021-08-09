package Reika.Satisforestry.Render;

import net.minecraftforge.client.model.ModelFormatException;
import net.minecraftforge.client.model.obj.GroupObject;
import net.minecraftforge.client.model.obj.WavefrontObject;

import Reika.Satisforestry.Satisforestry;


public class ModelSFMiner extends WavefrontObject {

	private GroupObject chassis;
	private GroupObject drill;

	public ModelSFMiner(String path) throws ModelFormatException {
		super("SF Miner", Satisforestry.class.getResourceAsStream(path));

		chassis = this.findGroupObject("chassis");
		drill = this.findGroupObject("drill");
	}

	private GroupObject findGroupObject(String s) {
		for (GroupObject g : groupObjects) {
			if (g.name.equals(s))
				return g;
		}
		return null;
	}

	public void drawChassis() {
		chassis.render();
	}

	public void drawDrill() {
		drill.render();
	}

}
