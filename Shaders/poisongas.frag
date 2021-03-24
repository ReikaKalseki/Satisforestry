#import color
#import effects

vec4 getColorAround(vec2 uv, vec2 offset, vec2 res, int r, float scale) {
	vec4 ret = vec4(0.0);
	for (int i = -r; i <= r; i++) {
		for (int k = -r; k <= r; k++) {
			vec2 offset2 = vec2(float(i), float(k))*res;
			vec2 duv = uv+offset+offset2;
			vec4 at = texture2D(bgl_RenderedTexture, duv);
			if (i != 0 && k != 0) {
				at *= scale;
			}
			ret = max(ret, at);
		}
	}
	return ret;
}

vec4 blurWithLeakage(vec2 uv, float radius, int r2, float scale) {
	vec4 color = vec4(0.0);
	color.a = 1.0;
	int r = int(radius)+1;
	
	float sum = 0.0;
  
	for (int i = -r; i <= r; i++) {
		for (int k = -r; k <= r; k++) {
			float dd = float(i*i+k*k);
			if (dd <= radius) {
				float f = sqrt(1.0/(dd+1.0));
				sum += f;
				vec2 res = vec2(1.0/float(screenWidth), 1.0/float(screenHeight));
				vec2 offset = vec2(float(i), float(k))*res;
				vec4 get = getColorAround(uv, offset, res, r2, scale);
				color += get*f;
			}
		}
	}
	
	color /= sum;
	
	color = min(vec4(1.0), color);
	
	return color; 
}

void main() {
    vec4 orig = texture2D(bgl_RenderedTexture, texcoord);
    
	vec4 blurred4 = blur(texcoord, 3.0);
	vec4 blurred20 = min(vec4(1.0), blurWithLeakage(texcoord, 20.0, 2, 0.75)*1.1);
    
	float r = mix(orig.r, blurred20.r, intensity);
	float g = mix(orig.g, blurred4.g, intensity);
	float b = mix(orig.b, blurred4.b, intensity);
	
    gl_FragColor = vec4(r, g, b, orig.a);
}