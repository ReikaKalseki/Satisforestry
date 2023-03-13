#import color
#import effects

void main() {
    vec4 orig = texture2D(bgl_RenderedTexture, texcoord);
	vec3 hsv = rgb2hsv(orig.rgb);
	float angle = radians(hsv.r*360.0);
	float displacement = hsv.b*6.0;
	vec2 newpos = vec2(texcoord)+displacement*vec2(cos(angle), sin(angle));
    vec4 newcolor = texture2D(bgl_RenderedTexture, newpos);
    
	float r = mix(orig.r, newcolor.r, intensity);
	float g = mix(orig.g, newcolor.g, intensity);
	float b = mix(orig.b, newcolor.b, intensity);
	
    gl_FragColor = vec4(r, g, b, orig.a);
}