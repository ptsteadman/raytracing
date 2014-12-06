package cs4620.ray2.surface;

import cs4620.ray2.IntersectionRecord;
import cs4620.ray2.Ray;
import egl.math.Matrix4d;
import egl.math.Vector3d;

public class Cylinder extends Surface {

	/** The center of the bottom of the cylinder x , y ,z components. */
	protected final Vector3d center = new Vector3d();

	public void setCenter(Vector3d center) {
		this.center.set(center);
	}

	/** The radius of the cylinder. */
	protected double radius = 1.0;

	public void setRadius(double radius) {
		this.radius = radius;
	}

	/** The height of the cylinder. */
	protected double height = 1.0;

	public void setHeight(double height) {
		this.height = height;
	}

	public Cylinder() {
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
		  	//TODO#A7: Modify the intersect method: 
		  	//1. transform the ray to object space (use untransformRay)
		  	//2. transform the resulting intersection point and normal to world space		  
		    Ray ray = untransformRay(rayIn);
		    //System.out.println("center: " + center);
		    //System.out.println("tMat: " + this.tMat);
		    //System.out.println("tmatcenter:" + (this.tMat.mulPos(center.clone())));

		    // Rename the common vectors so I don't have to type so much
		    Vector3d d = ray.direction;
		    Vector3d c = center;
		    Vector3d o = ray.origin;
		    
		    double tMin = ray.start, tMax = ray.end;
		    // Compute some factors used in computation
		    double qx = o.x - c.x;
		    double qy = o.y - c.y;
		    //double qz = o.z - c.z;
		    double rr = radius * radius;

		    double dd = d.x * d.x + d.y *d.y;
		    double qd = d.x * qx + d.y * qy;
		    double qq =  qx * qx + qy * qy;

		    double t = 0, td1=0, td2=0;
		    double zMin = c.z - height/2;
		    double zMax = c.z + height/2;

		    // z-plane cap calculations
		    if (d.z >= 0) {
		      td1 = (zMin- o.z) / d.z;
		      td2 = (zMax - o.z) / d.z;
		    }
		    else {
		      td1 = (zMax - o.z) / d.z;
		      td2 = (zMin - o.z) / d.z;
		    }
		    if (tMin > td2 || td1 > tMax)
		      return false;
		    if (td1 > tMin)
		      tMin = td1;
		    if (td2 < tMax)
		      tMax = td2;

		    // solving the quadratic equation for t at the pts of intersection
		    // dd*t^2 + (2*qd)*t + (qq-r^2) = 0
		    double discriminantsqr = (qd * qd - dd * (qq - rr));

		    // If the discriminant is less than zero, there is no intersection
		    if (discriminantsqr < 0) {
		      return false;
		    }

		    // Otherwise check and make sure that the intersections occur on the ray (t
		    // > 0) and return the closer one
		    double discriminant = Math.sqrt(discriminantsqr);
		    double t1 = (-qd - discriminant) / dd;
		    double t2 = (-qd + discriminant) / dd;

		    if (t1 > ray.start && t1 < ray.end) {
		      t = t1;
		    }
		    else if (t2 > ray.start && t2 < ray.end) {
		      t = t2;
		    }

		    Vector3d thit1 = new Vector3d(0); 
		    ray.evaluate(thit1, tMin);
		    Vector3d thit2 = new Vector3d(0); 
		    ray.evaluate(thit2, tMax);

		    double dx1 = thit1.x-c.x;  
		    double dy1 = thit1.y-c.y; 
		    double dx2 = thit2.x-c.x;  
		    double dy2 = thit2.y-c.y; 

		    if ((t < tMin || t > tMax) && dx1 * dx1 + dy1 * dy1 > rr && dx2 * dx2 + dy2 * dy2 > rr) {
		      return false;
		    }

		    // There was an intersection, fill out the intersection record
		    if (outRecord != null) {
		      double tside =Math.min( td1, td2);

		      if (t <tside) {
		        outRecord.t = tside;
		        ray.evaluate(outRecord.location, tside);
		        
		        //TODO#A7 Part 2 MY ADDITION
		        Vector3d locWorld = new Vector3d(outRecord.location);
		        this.tMat.mulPos(locWorld);
		        outRecord.location.set(locWorld);
		        
		        Vector3d norm = new Vector3d(0.0,0.0,1.0);
		        this.tMatTInv.mulDir(norm);
		        outRecord.normal.set(norm.normalize());
		      }
		      else {
		        outRecord.t = t;
		        ray.evaluate(outRecord.location, t);     
		        
		        //TODO#A7 Part 2 MY ADDITION
		        Vector3d locWorld = new Vector3d(outRecord.location);
		        Vector3d norm = new Vector3d().set(outRecord.location.x, outRecord.location.y, 0).sub(c.x, c.y, 0);

		        this.tMat.mulPos(locWorld);
		        outRecord.location.set(locWorld);
		        
		        this.tMatTInv.mulDir(norm);
		        outRecord.normal.set(norm.normalize());
		        //outRecord.normal.set(outRecord.location.x, outRecord.location.y, 0).sub(c.x, c.y, 0);
		      }
		      //MADE A CHANGE, RAY -> RAYIN
		      if (outRecord.normal.dot(rayIn.direction) > 0)
		        outRecord.normal.negate();

		      outRecord.surface = this;

		    }

		    return true;
		  }

	public void computeBoundingBox() {
		// TODO#A7: Compute the bounding box and store the result in
		// averagePosition, minBound, and maxBound.
		// Hint: The bounding box may be transformed by a transformation matrix.

		Vector3d minPt = new Vector3d(center.x - radius, center.y - radius, center.z - height/2);
		Vector3d maxPt = new Vector3d(center.x + radius, center.y + radius, center.z + height/2);
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
		this.minBound = new Vector3d();
		this.maxBound = new Vector3d();
		this.averagePosition = new Vector3d();
		
		this.minBound.set(minx,miny,minz);
		this.maxBound.set(maxx,maxy,maxz);		
		
		this.averagePosition.set(this.tMat.mulPos(center.clone()));
		//System.out.println("center:" + center);
		//System.out.println("avgpos: " + this.averagePosition);
	    //System.out.println("tMat: " + this.tMat);

		/*
		Vector3d sphereDiam = maxPt.sub(minPt);
		double radius = sphereDiam.len()/2.0;
		Vector3d a = new Vector3d(this.averagePosition);
		double minx = a.x - radius, miny = a.y - radius, minz = a.z - radius;
		double maxx = a.x + radius, maxy = a.y + radius, maxz = a.z + radius;
		
		this.minBound.set(minx,miny,minz);
		this.maxBound.set(maxx,maxy,maxz);
		*/
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "Cylinder " + center + " " + radius + " " + height + " "
				+ shader + " end";
	}
}
