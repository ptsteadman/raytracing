package cs4620.ray2.shader;

import cs4620.ray2.shader.Shader;
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
public class Glazed extends Shader {

	/**
	 * The index of refraction of this material. Used when calculating Snell's Law.
	 */
	protected double refractiveIndex;
	public void setRefractiveIndex(double refractiveIndex) { this.refractiveIndex = refractiveIndex; }

	/**
	 * The underlying material beneath the glaze.
	 */
	protected Shader substrate;
	public void setSubstrate(Shader substrate) {
		this.substrate = substrate; 
	}
	
	public Glazed() { 
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
		Colord reflectedColor = new Colord();
		Colord substrateColor = new Colord();

		Vector3d outgoing = new Vector3d().set(ray.direction).negate();
		double r = this.fresnel(record.normal.clone(), outgoing.clone(), this.refractiveIndex);
		
		// calculate reflected color
		Ray reflection = new Ray();
		reflection.origin.set(record.location);
		reflection.direction.set(record.normal.clone());
		reflection.direction.mul(2*(record.normal.dot(outgoing))).sub(outgoing.clone()).normalize();
		reflection.makeOffsetRay();
		//reflection.makeOffsetRay();
		RayTracer.shadeRay(reflectedColor, scene, reflection, depth);
		
		// calculate substrate color
		IntersectionRecord test = new IntersectionRecord();
		test.set(record);
		substrate.shade(substrateColor, scene, ray, test, depth);
		
		// scale reflected and substrate colors by R and (1 - R)
		//System.out.println(r);
		
		outIntensity.add(substrateColor.clone().mul(1 - r));
		outIntensity.add(reflectedColor.clone().mul(r));
	
	}
}