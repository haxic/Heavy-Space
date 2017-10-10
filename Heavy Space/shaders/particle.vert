#version 140

in vec2 position;
in mat4 modelViewMatrix;
in vec4 textureOffsets;
in vec2 blendFactor;

out vec2 textureCoordinate1;
out vec2 textureCoordinate2;
out vec2 pass_blendFactor;

uniform mat4 projectionMatrix;
uniform float atlasSize;

void main(void){
	vec2 textureCoordinate = position + vec2(0.5, 0.5);
	textureCoordinate.y = 1.0 - textureCoordinate.y;
	textureCoordinate /= atlasSize;
	textureCoordinate1 = textureCoordinate + textureOffsets.xy;
	textureCoordinate2 = textureCoordinate + textureOffsets.zw;
	pass_blendFactor = blendFactor;
	
	gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 0.0, 1.0);

}