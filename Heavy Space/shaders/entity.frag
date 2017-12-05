#version 330 core

in vec2 pass_uv;
in vec3 worldNormal;
in vec3 toLightVector[12];
in vec3 toCameraVector;

// Lighting
uniform float ambientLight;
// Lighting lights
uniform int numberOfLights;
uniform vec3 lightColor[12];
uniform vec3 attenuation[12];
// Lighting material
uniform vec3 materialSpecularColor;
uniform float materialShininess;
uniform float allowBackLighting;

uniform sampler2D textureSampler;

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
	vec3 unitToCameraVector = normalize(toCameraVector);
	
	// Lighting
	vec3 totalDiffuse = vec3(0.0);
	vec3 totalSpecular = vec3(0.0);
	for (int i = 0; i < numberOfLights; i++) {
		vec3 unitToLightVector = normalize(toLightVector[i]);
		vec3 unitLightDirection = -unitToLightVector;
		vec3 unitReflectedLightDirection = reflect(unitLightDirection, unitWorldNormal);
		float distanceToLight = length(toLightVector[i]);
		
		// Attenuation
		float attenuation = clamp(1.0 - ((distanceToLight*distanceToLight) / (attenuation[i].x*attenuation[i].x)), 0.0, 1.0);
		
		// Diffuse lighting
		float diffuseCoefficient = max(dot(unitWorldNormal, unitToLightVector), 0.0);
		totalDiffuse = totalDiffuse + (diffuseCoefficient * lightColor[i] * attenuation);
		
		// Specular lighting
		float specularCoefficient = 0.0;
		float cosAngle = 0.0;
		if(diffuseCoefficient > 0.0) {
			cosAngle = max(dot(unitToCameraVector, unitReflectedLightDirection), 0.0);
			specularCoefficient = pow(cosAngle, materialShininess);
		}
		totalSpecular = totalSpecular + (specularCoefficient * materialSpecularColor * lightColor[i] * attenuation);
	}
	totalDiffuse = max(totalDiffuse, ambientLight);
	
	fragment = vec4(totalDiffuse * textureColor.xyz + totalSpecular, textureColor.a);
//	fragment = vec4(totalSpecular, 1.0);
}