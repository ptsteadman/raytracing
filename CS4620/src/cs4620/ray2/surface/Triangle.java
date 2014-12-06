package cs4620.ray2.surface;

import cs4620.ray2.IntersectionRecord;
import cs4620.ray2.Ray;
import egl.math.Vector3d;
import egl.math.Vector3i;
import cs4620.ray2.shader.Shader;

/**
 * Represents a single triangle, part of a triangle mesh
 *
 * @author ags
 */
public class Triangle extends Surface {
	/** The normal vector of this triangle, if vertex normals are not specified */
	Vector3d norm;

	/** The mesh that contains this triangle */
	Mesh owner;

	/** 3 indices to the vertices of this triangle. */
	Vector3i index;

	double a, b, c, d, e, f;

	public Triangle(Mesh owner, Vector3i index, Shader shader) {
		this.owner = owner;
		this.index = new Vector3i(index);

		Vector3d v0 = owner.getPosition(index.x);
		Vector3d v1 = owner.getPosition(index.y);
		Vector3d v2 = owner.getPosition(index.z);

		if (!owner.hasNormals()) {
			Vector3d e0 = new Vector3d(), e1 = new Vector3d();
			e0.set(v1).sub(v0);
			e1.set(v2).sub(v0);
			norm = new Vector3d();
			norm.set(e0).cross(e1);
			norm.normalize();
		}
		a = v0.x - v1.x;
		b = v0.y - v1.y;
		c = v0.z - v1.z;

		d = v0.x - v2.x;
		e = v0.y - v2.y;
		f = v0.z - v2.z;

		this.setShader(shader);
	}

	/**
	 * Tests this surface for intersection with ray. If an intersection is found
	 * record is filled out with the information about the intersection and the
	 * method returns true. It returns false otherwise and the information in
	 * outRecord is not modified.
	 *
	 * @param outRecord
	 *            the output IntersectionRecord
	 * @param rayIn
	 *            the ray to intersect
	 * @return true if the surface intersects the ray
	 */
	public boolean intersect(IntersectionRecord outRecord, Ray rayIn) {
	  	//TODO#A7: Modify the intersect method: transform the ray to object space
	  	//transform the resulting intersection point and normal to world space

		Ray ray = untransformRay(rayIn);		
		//System.out.println("Ray Direction: " + ray.direction);
		
		Vector3d v0 = owner.getPosition(index.x).clone();
		
		double g = ray.direction.x;
		double h = ray.direction.y;
		double i = ray.direction.z;
		double j = v0.x - ray.origin.x;
		double k = v0.y - ray.origin.y;
		double l = v0.z - ray.origin.z;
		double M = a * (e * i - h * f) + b * (g * f - d * i) + c
				* (d * h - e * g);

		double ei_hf = e * i - h * f;
		double gf_di = g * f - d * i;
		double dh_eg = d * h - e * g;
		double ak_jb = a * k - j * b;
		double jc_al = j * c - a * l;
		double bl_kc = b * l - k * c;

		double t = -(f * (ak_jb) + e * (jc_al) + d * (bl_kc)) / M;
		if (t > ray.end || t < ray.start)
			return false;

		double beta = (j * (ei_hf) + k * (gf_di) + l * (dh_eg)) / M;
		if (beta < 0 || beta > 1)
			return false;

		double gamma = (i * (ak_jb) + h * (jc_al) + g * (bl_kc)) / M;
		if (gamma < 0 || gamma + beta > 1)
			return false;

		// There was an intersection, fill out the intersection record
		if (outRecord != null) {
			outRecord.t = t;
			ray.evaluate(outRecord.location, t);
	        //TODO#A7 Part 2 MY ADDITION
	        Vector3d locWorld = new Vector3d(outRecord.location);
	        Vector3d normal = new Vector3d();

	        this.tMat.mulPos(locWorld);
	        outRecord.location.set(locWorld);
	        
	        //this.tMatTInv.mulDir(norm);
	        //outRecord.normal.set(norm);
			
			outRecord.surface = this;

			if (norm != null) {
				normal.set(norm);
		        this.tMatTInv.mulDir(normal);
		        //System.out.println("normal: " + normal);
				outRecord.normal.set(normal);
			} else {
				outRecord.normal
						.setZero()
						.addMultiple(1 - beta - gamma, owner.getNormal(index.x))
						.addMultiple(beta, owner.getNormal(index.y))
						.addMultiple(gamma, owner.getNormal(index.z));
				normal.set(outRecord.normal);
		        this.tMatTInv.mulDir(normal);
		        outRecord.normal.set(normal);
			}
			
			
			outRecord.normal.normalize();
			if (owner.hasUVs()) {
				outRecord.texCoords.setZero()
						.addMultiple(1 - beta - gamma, owner.getUV(index.x))
						.addMultiple(beta, owner.getUV(index.y))
						.addMultiple(gamma, owner.getUV(index.z));
			}
		}

		return true;

	}

	public void computeBoundingBox() {
		// TODO#A7: Compute the bounding box and store the result in
		// averagePosition, minBound, and maxBound.

		
		Vector3d tv0 = owner.getPosition(index.x);
		Vector3d tv1 = owner.getPosition(index.y);
		Vector3d tv2 = owner.getPosition(index.z);
		
		Vector3d minPt = new Vector3d(Math.min(tv2.x,Math.min(tv0.x,tv1.x)),Math.min(tv2.y,Math.min(tv0.y,tv1.y)),Math.min(tv2.z,Math.min(tv0.z,tv1.z)));
		Vector3d maxPt = new Vector3d(Math.max(tv2.x,Math.max(tv0.x,tv1.x)),Math.max(tv2.y,Math.max(tv0.y,tv1.y)),Math.max(tv2.z,Math.max(tv0.z,tv1.z)));
		
		Vector3d v1 = new Vector3d(minPt);
		Vector3d v2 = new Vector3d(minPt.x,maxPt.y,minPt.z);
		Vector3d v3 = new Vector3d(minPt.x,minPt.y,maxPt.z);
		Vector3d v4 = new Vector3d(minPt.x,maxPt.y,maxPt.z);
		
		Vector3d v5 = new Vector3d(maxPt.x,minPt.y,minPt.z);
		Vector3d v6 = new Vector3d(maxPt.x,minPt.y,maxPt.z);
		Vector3d v7 = new Vector3d(maxPt.x,maxPt.y,minPt.z);
		
		Vector3d v8 = new Vector3d(maxPt);
		//if(this.tMat != null){
		this.tMat.mulPos(v1);
		this.tMat.mulPos(v2);
		this.tMat.mulPos(v3);
		this.tMat.mulPos(v4);
		this.tMat.mulPos(v5);
		this.tMat.mulPos(v6);
		this.tMat.mulPos(v7);
		this.tMat.mulPos(v8);
		//}
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
		
		Vector3d avg = new Vector3d(tv0.clone().add(tv1.clone().add(tv2.clone())).div(3.0));
		//if(this.tMat != null){
			this.averagePosition = new Vector3d().set(this.tMat.mulPos(avg.clone()));
		//} else {
		//	this.averagePosition = new Vector3d().set(avg);

		//}
		 
		 
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "Triangle ";
	}
}