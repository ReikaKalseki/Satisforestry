package Reika.Satisforestry.Render;

import java.util.ArrayList;

import Reika.DragonAPI.IO.Shaders.ShaderLibrary.ComputedLibrary;

public class BlurWithRedBleed extends ComputedLibrary {

	private BlurWithRedBleed(String[] args) {
		super("blurwithbleed", args);
	}

	@Override
	protected ArrayList<String> generate() {
		int radius = (int)params[0];
		int bleed = (int)params[1];
		ArrayList<String> li = new ArrayList();
		li.add("vec4 blurWithLeakage(vec2 uv, float scale) {");
		
		li.add("vec4 color = vec4(0.0);");
		li.add("color.a = 1.0;");
		li.add("float sum = 0.0;");

		li.add("float f = 0.0;");
		li.add("vec2 offset = vec2(0.0);");
		li.add("vec4 get = vec4(0.0);");
		
		li.add("vec2 res = vec2(1.0/float(screenWidth), 1.0/float(screenHeight));");
		int r = radius+1;
		for (int i = -r; i <= r; i++) {
			for (int k = -r; k <= r; k++) {
				float dd = i*i+k*k;
				float f = dd <= radius ? (float)Math.sqrt(1D/(dd+1)) : 0;
				if (f > 0) {
					li.add("f = float("+f+");");
					li.add("sum += f;");
					li.add("offset = vec2(float("+i+"), float("+k+"))*res;");
					li.add("get = getColorAround(uv, offset, res, "+bleed+", scale);");
					li.add("color += get*f;");
				}
			}
		}
		li.add("color /= sum;");
		li.add("color = min(vec4(1.0), color);");
		
		li.add("return color; ");
		li.add("}");
		return li;
	}

}
