package Reika.Satisforestry.Config;

import java.util.HashMap;

import net.minecraftforge.fluids.Fluid;

public class ResourceFluid extends NodeResource<Fluid> {

	public ResourceFluid(String s, String n, int w, int c, HashMap<String, Object> map) {
		super(s, n, w, c, map);
	}

	@Override
	public Fluid getItem(NodeItem obj) {
		return obj.item;
	}

	@Override
	public boolean matchItem(NodeItem obj, Fluid is) {
		return is == obj.item;
	}

}
