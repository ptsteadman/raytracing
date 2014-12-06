package cs4620.ray2.surface;

import cs4620.ray2.IntersectionRecord;
import cs4620.ray2.Ray;
import egl.math.Vector3d;

/**
 * Represents a sphere as a center and a radius.
 *
 * @author ags
 */
public class Sphere extends Surface {

	/** The center of the sphere. */
	protected final Vector3d center = new Vector3d();

	public void setCenter(Vector3d center) {
		this.center.set(center);
	}

	/** The radius of the sphere. */
	protected double radius = 1.0;

	public void setRadius(double radius) {
		this.radius = radius;
	}

	protected final double M_2PI = 2 * Math.PI;

	public Sphere() {
	}

	/**
	 * Tests this surface for intersection with ray. If an intersection is found
	 * record is filled out with the information about the intersection and the
	 * method returns true. It returns false otherwise and the information in
	 * outRecord is not modified.
	 *
	 * @param outRecord
	 *            the output IntersectionRecord
	 * @param ray
	 *            the ray to intersect
	 * @return true if the surface intersects the ray
	 */
	public boolean intersect(IntersectionRecord outRecord, Ray rayIn) {
	  	//TODO#A7: Modify the intersect method: transform the ray to object space
	  	//transform the resulting intersection point and normal to world space

		//transform the ray into object space
		Ray ray = untransformRay(rayIn);
		// Rename the common vectors so I don't have to type so much
		Vector3d d = ray.direction;
		Vector3d c = center;
		Vector3d o = ray.origin;

		// Compute some factors used in computation
		double qx = o.x - c.x;
		double qy = o.y - c.y;
		double qz = o.z - c.z;
		double dd = d.lenSq();
		double qd = qx * d.x + qy * d.y + qz * d.z;
		double qq = qx * qx + qy * qy + qz * qz;

		// solving the quadratic equation for t at the pts of intersection
		// dd*t^2 + (2*qd)*t + (qq-r^2) = 0
		double discriminantsqr = (qd * qd - dd * (qq - radius * radius));

		// If the discriminant is less than zero, there is no intersection
		if (discriminantsqr < 0) {
			return false;
		}

		// Otherwise check and make sure that the intersections occur on the ray
		// (t
		// > 0) and return the closer one
		double discriminant = Math.sqrt(discriminantsqr);
		double t1 = (-qd - discriminant) / dd;
		double t2 = (-qd + discriminant) / dd;
		double t = 0;
		if (t1 > ray.start && t1 < ray.end) {
			t = t1;
		} else if (t2 > ray.start && t2 < ray.end) {
			t = t2;
		} else {
			return false; // Neither intersection was in the ray's half line.
		}

		// There was an intersection, fill out the intersection record
		if (outRecord != null) {
			outRecord.t = t;
			ray.evaluate(outRecord.location, t);
	        //TODO#A7 Part 2 MY ADDITION
	        Vector3d locWorld = new Vector3d(outRecord.location);
	        Vector3d norm = new Vector3d(outRecord.location.clone().sub(center).normalize());

	        this.tMat.mulPos(locWorld);
	        outRecord.location.set(locWorld);
	        
	        this.tMatTInv.mulDir(norm);
	        outRecord.normal.set(norm.normalize());
	        
			outRecord.surface = this;
			//outRecord.normal.set(outRecord.location).sub(center).normalize();
			//NORMAL SHOULD BE CORRECT, NOT SURE IF THETA/PHI SHOULD BE COMPUTED IN OBJECT OR WORLD SPACE
			double theta = Math.asin(outRecord.normal.y);
			double phi = Math.atan2(outRecord.normal.x, outRecord.normal.z);
			double u = (phi + Math.PI) / (2 * Math.PI);
			double v = (theta - Math.PI / 2) / Math.PI;
			outRecord.texCoords.set(u, v);
			
			}

		return true;
	}

	public void computeBoundingBox() {
		// TODO#A7: Compute the bounding box and store the result in
		// averagePosition, minBound, and maxBound.

		Vector3d minPt = new Vector3d(center.x - radius, center.y - radius, center.z - radius);
		Vector3d maxPt = new Vector3d(center.x + radius, center.y + radius, center.z + radius);
		
		Vector3d v1 = new Vector3d(minPt);
		Vector3d v2 = new Vector3d(minPt.x,maxPt.y,minPt.z);
		Vector3d v3 = new Vector3d(minPt.x,minPt.y,maxPt.z);
		Vector3d v4 = new Vector3d(minPt.x,maxPt.y,maxPt.z);
		
		Vector3d v5 = new Vector3d(maxPt.x,minPt.y,minPt.z);
		Vector3d v6 = new Vector3d(maxPt.x,minPt.y,maxPt.z);
		Vector3d v7 = new Vector3d(maxPt.x,maxPt.y,minPt.z);
		Vector3d v8 = new Vector3d(maxPt);
		
		this.tMat.mulPos(v1);
		this.tMat.mulPos(v2);
		this.tMat.mulPos(v3);
		this.tMat.mulPos(v4);
		this.tMat.mulPos(v5);
		this.tMat.mulPos(v6);
		this.tMat.mulPos(v7);
		this.tMat.mulPos(v8);
		Vector3d[] varray = {v1,v2,v3,v4,v5,v6,v7,v8};
		double minx = v1.x, miny = v1.y, minz = v1.z;
		double maxx = v1.x, maxy = v1.y, maxz = v1.z;
		
		for (Vector3d v : varray){
			minx = (v.x < minx) ? v.x : minx;
			miny = (v.y < miny) ? v.y : miny;
			minz = (v.z < minz) ? v.z : minz;
			maxx = (v.x > maxx) ? v.x : maxx;
			maxy = (v.y > maxy) ? v.y : maxy;
			maxz = (v.z > maxz) ? v.z : maxz;
		}
		
		this.minBound = new Vector3d(minx,miny,minz);
		this.maxBound = new Vector3d(maxx,maxy,maxz);
		
		this.averagePosition = new Vector3d().set(this.tMat.mulPos(center.clone()));
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "sphere " + center + " " + radius + " " + shader + " end";
	}

}