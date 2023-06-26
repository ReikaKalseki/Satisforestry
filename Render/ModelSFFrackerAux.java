package Reika.Satisforestry.Render;

import net.minecraftforge.client.model.ModelFormatException;
import net.minecraftforge.client.model.obj.GroupObject;
import net.minecraftforge.client.model.obj.WavefrontObject;

import Reika.Satisforestry.Satisforestry;


public class ModelSFFrackerAux extends WavefrontObject {

	private GroupObject object;

	public ModelSFFrackerAux(String path) throws ModelFormatException {
		super("SF Fracker Aux", Satisforestry.class.getResourceAsStream(path));

		object = this.findGroupObject("ThumperNode");
	}

	private GroupObject findGroupObject(String s) {
		for (GroupObject g : groupObjects) {
			if (g.name.equals(s))
				return g;
		}
		return null;
	}

	public void render() {
		object.render();
	}

}
