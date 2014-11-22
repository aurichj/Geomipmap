attribute vec3 inPosition;
uniform mat4 g_WorldViewProjectionMatrix;
uniform float planet_radius;
uniform float noise_scale;
varying float color_scale;

void main(){   
    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0);
    color_scale = ( ((length(inPosition)-planet_radius)/noise_scale)+1)/2;
}
