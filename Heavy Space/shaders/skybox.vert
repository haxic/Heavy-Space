#version 400

in vec3 position;
// 3d vector instead of 2d vector, since the cube map is sampled using a direction vector, not a 2d texture coordinate
out vec3 textureCoords;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

void main(void){
	// No model matrix, since there will be no translation, rotation or scaling for the cube map 
	gl_Position = projectionMatrix * viewMatrix * vec4(position, 1.0); 
	textureCoords = position;
}