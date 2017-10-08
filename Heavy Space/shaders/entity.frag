#version 330 core

in vec2 pass_uv;
in vec3 worldNormal;
in vec3 toLightVector;
in vec3 toCameraVector;

uniform sampler2D textureSampler;
uniform vec3 lightColor;
uniform vec3 reflectivity;
uniform vec3 shininess;
uniform float allowBackLighting;

out vec4 fragment;

void main(){
	vec4 textureColor = texture(textureSampler, pass_uv);
	// Discard transparent pixels.
	if (textureColor.a < 0.5) {
		discard;
	}
	
	vec3 newWorldNormal = worldNormal;
	if (allowBackLighting > 0.5 && dot(worldNormal, toCameraVector) < 0.0) {
		newWorldNormal = -worldNormal;
	}
	
	vec3 unitWorldNormal = normalize(newWorldNormal);
	vec3 unitToLightVector = normalize(toLightVector);
	vec3 unitToCameraVector = normalize(toCameraVector);
	vec3 unitLightDirection = -unitToLightVector;
	vec3 reflectedLightDirection = reflect(unitLightDirection, unitWorldNormal);
	
	float brightness = max(dot(unitWorldNormal, unitToLightVector), 0.0);
	vec3 diffuseColor = brightness * lightColor;
	
	float reflectionFactor = max(dot(reflectedLightDirection, unitToCameraVector), 0.0);
	float dampenFactor = pow(reflectionFactor, 10);
	vec3 specularColor = dampenFactor * 1 * lightColor;
		
	fragment = vec4(diffuseColor, 1.0) * textureColor + vec4(specularColor, 1.0);
//	fragment = vec4(diffuseColor, 1.0) * textureColor;
//	fragment = vec4(specularColor, 1.0);
}