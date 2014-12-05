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
		Vector3d n = new Vector3d();
		
		double theta1; 
		double theta2;
		double n2; 
		double n1;
		if(outgoing.dot(record.normal) > 0){
			theta1 = Math.acos(outgoing.dot(record.normal));
			theta2 = Math.asin(Math.sin(theta1)/refractiveIndex);
			n2 = refractiveIndex;
			n1 = 1.0;
			n.set(record.normal);
		} else {
			theta1 = Math.acos(outgoing.dot(record.normal.clone().negate()));
			theta2 = Math.asin(Math.sin(theta1)/refractiveIndex);
			n2 = 1.0;
			n1 = refractiveIndex;
			n.set(record.normal.clone().negate());
		}
		Vector3d d = ray.direction;
		double r = this.fresnel(n, outgoing, this.refractiveIndex);
		
		Colord reflectedColor = new Colord();
		Ray reflection = new Ray();
		reflection.origin.set(record.location);
		reflection.direction.set(n.clone());
		reflection.direction.mul(2*(n.dot(outgoing))).sub(outgoing);
		reflection.makeOffsetRay();
		RayTracer.shadeRay(reflectedColor, scene, reflection, depth);
		outIntensity.add(reflectedColor.clone().mul(r));
		double discriminant = 1 - (Math.pow(n1, 2)*(1-Math.pow(d.dot(n),2)))/Math.pow(n2, 2);
		if(discriminant > 0){
			Colord refractedColor = new Colord();
			Ray refraction = new Ray();
			refraction.origin.set(record.location);
			refraction.direction.set(d);
			refraction.direction.sub(n.clone().mul(d.dot(n)));
			refraction.direction.mul(n1/n2);
			refraction.direction.sub(n.mul(Math.sqrt(discriminant)));
			refraction.makeOffsetRay();
			RayTracer.shadeRay(refractedColor, scene, refraction, depth);
			
			outIntensity.add(refractedColor.clone().mul(1 - r));
		}
		
	}
	

}