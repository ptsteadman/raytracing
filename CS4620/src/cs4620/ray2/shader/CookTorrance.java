package cs4620.ray2.shader;

import cs4620.ray2.IntersectionRecord;
import cs4620.ray2.Light;
import cs4620.ray2.Ray;
import cs4620.ray2.Scene;
import egl.math.Color;
import egl.math.Colord;
import egl.math.Vector3d;

public class CookTorrance extends Shader {

	/** The color of the diffuse reflection. */
	protected final Colord diffuseColor = new Colord(Color.White);
	public void setDiffuseColor(Colord diffuseColor) { this.diffuseColor.set(diffuseColor); }

	/** The color of the specular reflection. */
	protected final Colord specularColor = new Colord(Color.White);
	public void setSpecularColor(Colord specularColor) { this.specularColor.set(specularColor); }

	/** The roughness controlling the roughness of the surface. */
	protected double roughness = 1.0;
	public void setRoughness(double roughness) { this.roughness = roughness; }

	/**
	 * The index of refraction of this material. Used when calculating Snell's Law.
	 */
	protected double refractiveIndex;
	public void setRefractiveIndex(double refractiveIndex) { this.refractiveIndex = refractiveIndex; }
	
	public CookTorrance() { }

	/**
	 * @see Object#toString()
	 */
	public String toString() {    
		return "CookTorrance " + diffuseColor + " " + specularColor + " " + roughness + " end";
	}

	/**
	 * Evaluate the intensity for a given intersection using the CookTorrance shading model.
	 *
	 * @param outIntensity The color returned towards the source of the incoming ray.
	 * @param scene The scene in which the surface exists.
	 * @param ray The ray which intersected the surface.
	 * @param record The intersection record of where the ray intersected the surface.
	 * @param depth The recursion depth.
	 */
	@Override
	public void shade(Colord outIntensity, Scene scene, Ray ray, IntersectionRecord record, int depth) {
		// TODO#A7 Fill in this function.
		// 1) Loop through each light in the scene.
		// 2) If the intersection point is shadowed, skip the calculation for the light.
		//	  See Shader.java for a useful shadowing function.
		// 3) Compute the incoming direction by subtracting
		//    the intersection point from the light's position.
		// 4) Compute the color of the point using the CookTorrance shading model. Add this value
		//    to the output.
		for (Light curLight : scene.getLights()){
			
			//From intersectino point to light
			Ray placeholder = new Ray();
			if (!this.isShadowed(scene, curLight, record, placeholder)){
				Vector3d l = new Vector3d(curLight.getDirection(record.location));  // don't need to negate this 
				l.normalize();
				Vector3d v = new Vector3d(ray.direction.clone().negate().normalize());
				Vector3d h = new Vector3d(l.clone().add(v).div((l.clone().add(v)).len()));
				h.normalize();
				Vector3d n = new Vector3d(record.normal.clone().normalize());
				
				double ndoth = n.dot(h);
				double fres = this.fresnel(n, l, refractiveIndex);
				
				double microDenom = 1.0/(roughness*roughness*ndoth*ndoth*ndoth*ndoth);
				double microNum = Math.exp(((ndoth*ndoth)-1.0)/(roughness*roughness*ndoth*ndoth));
				double micro = microNum*microDenom;
				double g1 = (2.0*ndoth*(n.dot(v)))/(v.dot(h));
				double g2 =  (2.0*ndoth*(n.dot(l)))/(v.dot(h));
				double geo = Math.min(1.0,Math.min(g1,g2));
				

				//diffuse term
				Vector3d diffuseColor = new Vector3d(this.diffuseColor.clone().mul(Math.max(0.0,n.dot(l))));
				//Clamp
				Colord diffColor = new Colord(diffuseColor);
				diffColor.clamp(0.0, 1.0);
				
				//specular
				double specCoeff = (fres*micro*geo)/(Math.PI*n.dot(v)*n.dot(l));
				Vector3d specularColor = new Vector3d(this.specularColor.clone().mul(specCoeff).mul(Math.max(0.0,n.dot(l))));
				//Clamp
				Colord specColor = new Colord(specularColor);
				specColor.clamp(0.0, 1.0);
				
				Colord finalColor = new Colord(specColor.clone().add(diffColor));
				Vector3d intensity = curLight.intensity.clone();
				double lightdist = curLight.getRSq(record.location);
				finalColor.mul(intensity).div(lightdist);
				
				outIntensity.set(finalColor);
			} 
		}

	}
}
