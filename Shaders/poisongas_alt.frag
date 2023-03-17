#import color
#import effects

void main() {
    vec4 orig = texture2D(bgl_RenderedTexture, texcoord);
	vec3 add = vec3(0.0, 0.0, 0.0);
	for(int i=0;i<18;i++)
	{
		float dispFac = 1+0.5*sin(float(time)*0.1);
		float ang = float(i)*20.0;
		float angle = radians(ang);
		vec2 dL = 7.5*dispFac*vec2(1.0/screenWidth, 1.0/screenHeight);
		vec2 disp = dL*vec2(cos(angle), sin(angle));
		vec3 rgbMix = hsb2rgb(vec3(mod(ang+180.0, 360.0)/360.0, 1.0, 1.0));
		add += rgbMix*texture2D(bgl_RenderedTexture, texcoord+disp).rgb*0.05;
		add += rgbMix*texture2D(bgl_RenderedTexture, texcoord+disp*0.5).rgb*0.1;
	}
	//float dX = 3.0/screenWidth;
    //vec4 right = texture2D(bgl_RenderedTexture, texcoord+vec2(dL*intensity, 0.0));
    //vec4 left = texture2D(bgl_RenderedTexture, texcoord-vec2(dL*intensity, 0.0));
	float f = max(0.0, 1.0-intensity);
	float r = min(1.0, orig.r*f+add.r*intensity);
	float g = min(1.0, orig.g*f+add.g*intensity);
	float b = min(1.0, orig.b*f+add.b*intensity);
	
    gl_FragColor = vec4(r, g, b, orig.a);
}