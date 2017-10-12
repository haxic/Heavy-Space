#version 330 core

in vec3 position;
in vec2 uv;
in vec3 normal;

uniform mat4 mvp;
uniform mat4 modelView;
uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform vec3 cameraPosition;
// Lighting lights
uniform vec3 lightPosition[12];
uniform int numberOfLights;
// Texture
uniform int atlasSize;
uniform vec2 textureOffset;

out vec2 pass_uv;
out vec3 worldNormal;
out vec3 toLightVector[12];
out vec3 toCameraVector;

void main(){
	pass_uv = (uv/atlasSize) + textureOffset;
	
	
	gl_Position = mvp * vec4(position, 1.0);
	vec3 worldPosition = (model * vec4(position, 1.0)).xyz;
	worldNormal = (model * vec4(normal, 0.0)).xyz;
	for (int i = 0; i < numberOfLights; i++) {
		toLightVector[i] = lightPosition[i] - worldPosition;
	}
	toCameraVector = cameraPosition - worldPosition;
}

	//pass_uv = (uv/atlasSize) + uvOffset;	

	//vec3 vertexPosition_cameraspace = (modelView * vec4(position, 1.0)).xyz;
	//eyeDirection_cameraspace = vec3(0) - vertexPosition_cameraspace;
	
	//vec3 lightPosition_cameraspace = (view * vec4(lightPosition, 1.0)).xyz;
	//lightDirection_cameraspace = lightPosition + eyeDirection_cameraspace;
	
	//normal_cameraspace = (modelView * vec4(normal, 0.0)).xyz;