#version 400

in vec3 textureCoords;
out vec4 result;

// Sampled cube instead of sampler 2d
uniform samplerCube cubeMap;

void main(void){
    result = texture(cubeMap, textureCoords);
}