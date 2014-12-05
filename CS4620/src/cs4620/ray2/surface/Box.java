package cs4620.ray2.surface;

import java.util.ArrayList;

import org.lwjgl.BufferUtils;

import cs4620.mesh.MeshData;
import cs4620.ray2.IntersectionRecord;
import cs4620.ray2.Ray;
import egl.math.Vector3d;

/**
 * A class that represents an Axis-Aligned box. When the scene is built, the Box
 * is split up into a Mesh of 12 Triangles.
 * 
 * @author sjm324
 *
 */
public class Box extends Surface {

	/* The mesh that represents this Box. */
	private Mesh mesh;

	/* The corner of the box with the smallest x, y, and z components. */
	protected final Vector3d minPt = new Vector3d();

	public void setMinPt(Vector3d minPt) {
		this.minPt.set(minPt);
	}

	/* The corner of the box with the largest x, y, and z components. */
	protected final Vector3d maxPt = new Vector3d();

	public void setMaxPt(Vector3d maxPt) {
		this.maxPt.set(maxPt);
	}

	/* Generate a Triangle mesh that represents this Box. */
	private void buildMesh() {
		// Create the OBJMesh
		MeshData box = new MeshData();

		box.vertexCount = 8;
		box.indexCount = 36;

		// Add positions
		box.positions = BufferUtils.createFloatBuffer(box.vertexCount * 3);
		box.positions.put(new float[] { (float) minPt.x, (float) minPt.y,
				(float) minPt.z, (float) minPt.x, (float) maxPt.y,
				(float) minPt.z, (float) maxPt.x, (float) maxPt.y,
				(float) minPt.z, (float) maxPt.x, (float) minPt.y,
				(float) minPt.z, (float) minPt.x, (float) minPt.y,
				(float) maxPt.z, (float) minPt.x, (float) maxPt.y,
				(float) maxPt.z, (float) maxPt.x, (float) maxPt.y,
				(float) maxPt.z, (float) maxPt.x, (float) minPt.y,
				(float) maxPt.z });

		box.indices = BufferUtils.createIntBuffer(box.indexCount);
		box.indices.put(new int[] { 0, 1, 2, 0, 2, 3, 0, 5, 1, 0, 4, 5, 0, 7,
				4, 0, 3, 7, 4, 6, 5, 4, 7, 6, 2, 5, 6, 2, 1, 5, 2, 6, 7, 2, 7,
				3 });
		this.mesh = new Mesh(box);
		
		//set transformations and absorptioins
		this.mesh.setTransformation(this.tMat, this.tMatInv, this.tMatTInv);
		
		this.mesh.shader = this.shader;
	}

	public void computeBoundingBox() {
		// TODO#A7: Compute the bounding box and store the result in
		// averagePosition, minBound, and maxBound.
		// Hint: The bounding box is not the same as just minPt and maxPt,
		// because
		// this object can be transformed by a transformation matrix.
		
		//worldCorner 1 and 2 
		
		/*
		Vector3d wc1 = new Vector3d(minPt);
		Vector3d wc2 = new Vector3d(maxPt);
		this.tMat.mulPos(wc1);
		this.tMat.mulPos(wc2);
		
		double minx = Math.min(world)
		*/
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
		
		
		Vector3d avg = new Vector3d(minBound.clone().add(maxBound.clone()).div(2.0));
		this.averagePosition = new Vector3d().set(this.tMat.mulPos(avg));
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

	public boolean intersect(IntersectionRecord outRecord, Ray ray) {
		return false;
	}

	public void appendRenderableSurfaces(ArrayList<Surface> in) {
		buildMesh();
		mesh.appendRenderableSurfaces(in);
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "Box ";
	}

}