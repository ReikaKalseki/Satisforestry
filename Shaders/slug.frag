#import math
#import geometry

uniform float distance;
uniform float scale;
uniform float factor;
uniform float speed;

void main() {
	vec2 focusXY = getScreenPos(0.0, 1.5, 0.0);
	
	float distv = distsq(focusXY, texcoord);
	float distfac_vertex = max(0.0, min(1.0, 2.25-65.0/scale*distv*distance));
	float vf = intensity*distfac_vertex*0.05;
	
	float ds = pow(distance, 0.125)/1.5;
	float dv = 0.5*factor;
	
	vec2 texUV = texcoord.xy;
	texUV.x += dv*0.47*vf*sin(23.3+texUV.y*51.8*ds+float(time)*speed/4.1);
	texUV.y += dv*0.62*vf*cos(34.5+texUV.x*45.7*ds+float(time)*speed/3.8);
	texUV.x += dv*0.167*vf*sin(23.3+texUV.y*171.8*ds+float(time)*speed/6.1);
	texUV.y += dv*0.145*vf*cos(34.5+texUV.x*185.7*ds+float(time)*speed/5.8);
	
    vec4 color = texture2D(bgl_RenderedTexture, texUV);    
    gl_FragColor = color;
}