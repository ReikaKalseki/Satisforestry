#import color
#import math
#import effects
#generate blur 2

vec4 getColorAround(vec2 uv, vec2 offset, vec2 res, int r, float scale) {
	vec4 ret = texture2D(bgl_RenderedTexture, uv+offset);
	float fac = 1.0;
	for(int idx=0;idx<36;idx++)
	{
		float i = float(idx);
		float dx = (mod(i, 6.0)-3.0)*r*0.5;
		float dy = (floor(i/6.0)-3.0)*r*0.5;
		vec2 offset2 = vec2(dx, dy)*res;
		vec2 duv = uv+offset+offset2;
		float dd = max(0.0, 1.0-(dx*dx+dy*dy)/25.0);
		vec4 at = texture2D(bgl_RenderedTexture, duv)*scale*min(1.0, dd*2.5);
		//fac += dd;
		//ret += at*dd;
		ret = max(ret, at);
	}
	ret /= fac;
	return ret;
}

#generate blurwithbleed 4 6

void main() {
    vec4 orig = texture2D(bgl_RenderedTexture, texcoord);
    
	vec4 blurred4 = blur2(texcoord);
	vec4 blurred20 = min(vec4(1.0), blurWithLeakage(texcoord, 1.0)*1.4);
	/*
	vec2 res = vec2(1.0/float(screenWidth), 1.0/float(screenHeight));
	vec4 blurred20 = 0.25*getColorAround(texcoord, vec2(0.0), res, 4, 1.0);
	for(int i=0;i<18;i++)
	{
		float ang = float(i)*20.0;
		float angle = radians(ang);
		vec2 dL = 4.0*res;
		vec2 disp = dL*vec2(cos(angle), sin(angle));
		blurred20 += 0.05*getColorAround(texcoord, dL, res, 4, 1.0);
	}
	blurred20 = min(vec4(1.0), blurred20);
    */
	float r = mix(orig.r, blurred20.r, intensity);
	float g = mix(orig.g, blurred4.g, intensity);
	float b = mix(orig.b, blurred4.b, intensity);
	
    gl_FragColor = vec4(r, g, b, orig.a);
}