package cs4620.ray2.accel;

import cs4620.ray2.Ray;
import egl.math.Vector3d;

/**
 * A class representing a node in a bounding volume hierarchy.
 * 
 * @author pramook 
 */
public class BvhNode {

	/** The current bounding box for this tree node.
	 *  The bounding box is described by 
	 *  (minPt.x, minPt.y, minPt.z) - (maxBound.x, maxBound.y, maxBound.z).
	 */
	public final Vector3d minBound, maxBound;
	
	/**
	 * The array of children.
	 * child[0] is the left child.
	 * child[1] is the right child.
	 */
	public final BvhNode child[];

	/**
	 * The index of the first surface under this node. 
	 */
	public int surfaceIndexStart;
	
	/**
	 * The index of the surface next to the last surface under this node.	 
	 */
	public int surfaceIndexEnd; 
	
	/**
	 * Default constructor
	 */
	public BvhNode()
	{
		minBound = new Vector3d();
		maxBound = new Vector3d();
		child = new BvhNode[2];
		child[0] = null;
		child[1] = null;		
		surfaceIndexStart = -1;
		surfaceIndexEnd = -1;
	}
	
	/**
	 * Constructor where the user can specify the fields.
	 * @param minBound
	 * @param maxBound
	 * @param leftChild
	 * @param rightChild
	 * @param start
	 * @param end
	 */
	public BvhNode(Vector3d minBound, Vector3d maxBound, BvhNode leftChild, BvhNode rightChild, int start, int end) 
	{
		this.minBound = new Vector3d();
		this.minBound.set(minBound);
		this.maxBound = new Vector3d();
		this.maxBound.set(maxBound);
		this.child = new BvhNode[2];
		this.child[0] = leftChild;
		this.child[1] = rightChild;		   
		this.surfaceIndexStart = start;
		this.surfaceIndexEnd = end;
	}
	
	/**
	 * @return true if this node is a leaf node
	 */
	public boolean isLeaf()
	{
		return child[0] == null && child[1] == null; 
	}
	
	/** 
	 * Check if the ray intersects the bounding box.
	 * @param ray
	 * @return true if ray intersects the bounding box
	 */
	public boolean intersects(Ray ray) {
		// Check whether the given ray intersects the AABB of this BvhNode
		// find the t where the ray intersects with the bounding slab
		double tMinX = (minBound.x - ray.origin.x)/ray.direction.x;
		double tMaxX = (maxBound.x - ray.origin.x)/ray.direction.x;
		double tMinY = (minBound.y - ray.origin.y)/ray.direction.y;
		double tMaxY = (maxBound.y - ray.origin.y)/ray.direction.y;
		double tMinZ = (minBound.z - ray.origin.z)/ray.direction.z;
		double tMaxZ = (maxBound.z - ray.origin.z)/ray.direction.z;
		
		// determine the entry and exit t for each slab
		double tEnterX = Math.min(tMinX, tMaxX);
		double tExitX = Math.max(tMinX, tMaxX);
		double tEnterY = Math.min(tMinY, tMaxY);
		double tExitY = Math.max(tMinY, tMaxY);
		double tEnterZ = Math.min(tMinZ, tMaxZ);
		double tExitZ = Math.max(tMinZ, tMaxZ);
		
		// determine the overall entry and exit t
		double tEnter = Math.max(Math.max(tEnterX, tEnterY), tEnterZ);
		double tExit = Math.min(Math.min(tExitX, tExitY), tExitZ);
		if (tEnter < tExit) return true;
		return false;
		
	}
}
