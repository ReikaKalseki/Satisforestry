#import color

void main() {
    vec4 orig = texture2D(bgl_RenderedTexture, texcoord);
    
    float gs = getVisualBrightness(orig.rgb);
	
	float power = max(0.0, (0.5-gs*2.0)*2.0);
	
	vec3 hsl = rgb2hsb(orig.rgb);
	vec3 gas = rgb2hsb(vec3(186.0/255.0, 1.0, 33.0/255.0));
	
	hsl.r = gas.r;
	hsl.g = mix(hsl.g, gas.g, power*0.5);
	hsl.b = max(hsl.b, gs);
	
	vec3 back = hsb2rgb(hsl);
	
	vec3 shifted = min(vec3(1.0), back);
	
	gas = hsb2rgb(gas);
	
	vec3 space = vec3(1.0);//gas/(vec3(1.0)-shifted);
	
	float mf = min(power*intensity, min(space.r, min(space.g, space.b)));
	
	vec3 net = mix(orig.rgb, shifted, intensity*power)+gas*mf;
	
	//net = mix(orig.rgb, gas, intensity);
    
    gl_FragColor = vec4(net.r, net.g, net.b, orig.a);
}