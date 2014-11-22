uniform float planet_radius;
uniform float noise_scale;
varying float color_scale;

void main(){
    gl_FragColor = vec4(1.0, 1.0, 1.0,1.0)*color_scale;
}