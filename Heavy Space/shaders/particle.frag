#version 140

in vec2 textureCoordinate1;
in vec2 textureCoordinate2;
in float pass_blendFactor;

out vec4 out_colour;

uniform sampler2D particleTexture;

void main(void){
	
	vec4 color1 = texture(particleTexture, textureCoordinate1);
	vec4 color2 = texture(particleTexture, textureCoordinate2);
	
	out_colour = mix(color1, color2, pass_blendFactor);

}