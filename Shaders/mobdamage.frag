#import color
#import effects
#import noise

uniform int red1;
uniform int green1;
uniform int blue1;
uniform int alpha1;
uniform int red2;
uniform int green2;
uniform int blue2;
uniform int alpha2;

uniform float fadeFactor;
uniform float noiseScaleX;
uniform float noiseScaleY;

uniform float additiveScale;
uniform float multiplyScale;

uniform float minimumEffect;

void main() {
    vec4 orig = texture2D(bgl_RenderedTexture, texcoord);
	
	vec4 clr1 = vec4(red1, green1, blue1, alpha1)/255.0;
	vec4 clr2 = vec4(red2, green2, blue2, alpha2)/255.0;
	
	/*
	float distX = min(texcoord.x, 1.0-texcoord.x)*2.0*float(screenWidth)/float(screenHeight);
	float distY = min(texcoord.y, 1.0-texcoord.y)*2.0;
	float dist = min(distX, distY);
	float br = min(1.0, dist*3.0);
	*/
	
	float dx = (texcoord.x-0.5)*2.0;
	float dy = (texcoord.y-0.5)*2.0;
	float d = 1.0-min(1.0, pow(dx*dx*dx*dx*dx*dx*dx*dx+dy*dy*dy*dy*dy*dy*dy*dy, 0.125));
	float sn = snoise(texcoord*50.0*vec2(float(screenWidth)/float(screenHeight)*noiseScaleX, 1.0*noiseScaleY));
	vec4 clr = mix(clr1, clr2, 0.5+0.5*sn);
	float df = 1+0.25*sn;
	float br = min(1.0, (d*0.018*1000.0*0.4+0.05)*df*fadeFactor);
	
	br = mix(1.0, br, intensity*clr.a);
	
	vec2 texUV = texcoord;
    
	vec3 net = texture2D(bgl_RenderedTexture, texUV).rgb;
	
	//net.rgb = net.rgb*br;
	
	//net.g *= br;
	//net.b *= br;
	
	//net = mix(orig.rgb, clr.rgb, 1.0-br);
	net = orig.rgb+clr.rgb*min(1.0, minimumEffect+(1.0-br)*additiveScale);
	net = mix(net.rgb, clr.rgb, min(1.0, minimumEffect+(1.0-br)*multiplyScale));
    
    gl_FragColor = vec4(net.r, net.g, net.b, orig.a);
}