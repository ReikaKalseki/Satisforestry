#import color
#import effects
#generate blur 2

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

#generate blurwithbleed 5 3

void main() {
    vec4 orig = texture2D(bgl_RenderedTexture, texcoord);
    
	vec4 blurred4 = blur2(texcoord);
	vec4 blurred20 = min(vec4(1.0), blurWithLeakage(texcoord, 0.75)*1.1);
    
	float r = mix(orig.r, blurred20.r, intensity);
	float g = mix(orig.g, blurred4.g, intensity);
	float b = mix(orig.b, blurred4.b, intensity);
	
    gl_FragColor = vec4(r, g, b, orig.a);
}