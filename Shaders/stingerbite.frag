#import color
#import effects
#import noise

void main() {
    vec4 orig = texture2D(bgl_RenderedTexture, texcoord);
	
	/*
	float distX = min(texcoord.x, 1.0-texcoord.x)*2.0*float(screenWidth)/float(screenHeight);
	float distY = min(texcoord.y, 1.0-texcoord.y)*2.0;
	float dist = min(distX, distY);
	float br = min(1.0, dist*3.0);
	*/
	
	float dx = (texcoord.x-0.5)*2.0;
	float dy = (texcoord.y-0.5)*2.0;
	float d = 1.0-min(1.0, pow(dx*dx*dx*dx*dx*dx*dx*dx+dy*dy*dy*dy*dy*dy*dy*dy, 0.125));
	float df = 1+0.25*snoise(texcoord*50.0*vec2(float(screenWidth)/float(screenHeight), 1.0));
	float br = min(1.0, (d*0.018*1000.0*0.4+0.05)*df);
	
	br = mix(1.0, br, intensity);
	
	vec2 texUV = texcoord;
    
	vec3 net = texture2D(bgl_RenderedTexture, texUV).rgb;
	
	//net.rgb = net.rgb*br;
	
	//net.g *= br;
	//net.b *= br;
	
	vec3 gas = vec3(192.0/255.0, 1.0, 48.0/255.0);
	//net = mix(orig.rgb, gas, 1.0-br);
	net = orig.rgb+gas*(1.0-br);
    
    gl_FragColor = vec4(net.r, net.g, net.b, orig.a);
}