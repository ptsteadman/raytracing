package cs4620.ray2.shader;

import cs4620.ray2.RayTracer;
import cs4620.ray2.IntersectionRecord;
import cs4620.ray2.Ray;
import cs4620.ray2.Scene;
import egl.math.Colord;
import egl.math.Vector3d;

/**
 * A Phong material.
 *
 * @author ags, pramook
 */
public class Glass extends Shader {

	/**
	 * The index of refraction of this material. Used when calculating Snell's Law.
	 */
	protected double refractiveIndex;
	public void setRefractiveIndex(double refractiveIndex) { this.refractiveIndex = refractiveIndex; }


	public Glass() { 
		refractiveIndex = 1.0;
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {    
		return "glass " + refractiveIndex + " end";
	}

	/**
	 * Evaluate the intensity for a given intersection using the Glass shading model.
	 *
	 * @param outIntensity The color returned towards the source of the incoming ray.
	 * @param scene The scene in which the surface exists.
	 * @param ray The ray which intersected the surface.
	 * @param record The intersection record of where the ray intersected the surface.
	 * @param depth The recursion depth.
	 */
	@Override
	public void shade(Colord outIntensity, Scene scene, Ray ray, IntersectionRecord record, int depth) {
		// TODO#A7: fill in this function.
		depth++;
		Vector3d outgoing = new Vector3d().set(ray.direction).negate();
		Vector3d effectiveNormal = new Vector3d();
		
		double theta1; 
		double theta2;
		double n2; 
		double n1;
		if(outgoing.dot(record.normal) > 0){
			theta1 = Math.acos(outgoing.dot(record.normal));
			theta2 = Math.asin(Math.sin(theta1)/refractiveIndex);
			n2 = refractiveIndex;
			n1 = 1.0;
			effectiveNormal.set(record.normal);
		} else {
			theta1 = Math.acos(outgoing.dot(record.normal.clone().negate()));
			theta2 = Math.asin(Math.sin(theta1)/refractiveIndex);
			n2 = 1.0;
			n1 = refractiveIndex;
			effectiveNormal.set(record.normal.clone().negate());
		}
		
		double criticalAngle = Math.asin(n1/n2);
		if((n1/n2) < 1 && theta1 > criticalAngle){
			// total internal reflection
			Colord reflectedColor = new Colord();
			Ray reflection = new Ray();
			reflection.origin.set(record.location);
			reflection.direction.set(effectiveNormal.clone());
			reflection.direction.mul(2*(effectiveNormal.dot(outgoing))).sub(outgoing);
			reflection.makeOffsetRay();
			RayTracer.shadeRay(reflectedColor, scene, reflection, depth);
			outIntensity.add(reflectedColor);
			
		} else {
			double r = this.fresnel(effectiveNormal, outgoing, this.refractiveIndex);
			Colord reflectedColor = new Colord();
			Ray reflection = new Ray();
			reflection.origin.set(record.location);
			reflection.direction.set(effectiveNormal.clone());
			reflection.direction.mul(2*(effectiveNormal.dot(outgoing))).sub(outgoing);
			reflection.makeOffsetRay();
			RayTracer.shadeRay(reflectedColor, scene, reflection, depth);
			
			Colord refractedColor = new Colord();
			Ray refraction = new Ray();
			refraction.origin.set(record.location);
			refraction.direction.set(ray.direction.clone().mul(n1/n2).add(effectiveNormal.clone().mul((n1/n2)*outgoing.dot(effectiveNormal)-Math.sqrt(1-Math.pow(Math.sin(theta2), 2)))));
			//refraction.direction.set(ray.direction);
			refraction.makeOffsetRay();
			RayTracer.shadeRay(refractedColor, scene, refraction, depth);
			
			
			outIntensity.add(refractedColor.clone()).mul(1 - r).add(reflectedColor.clone().mul(r));
			
		
		}
		
	}
	

}