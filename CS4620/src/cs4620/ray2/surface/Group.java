package cs4620.ray2.surface;

import java.util.ArrayList;
import java.util.Iterator;

import cs4620.ray2.IntersectionRecord;
import cs4620.ray2.Ray;
import egl.math.Matrix4d;
import egl.math.Vector3d;

public class Group extends Surface {

  /** List of objects under this group. */
  ArrayList<Surface> objs = new ArrayList<Surface>();
  
  /** The transformation matrix associated with this group. */
  private Matrix4d transformMat;
  
  /** A shared temporary matrix */
  static Matrix4d tmp = new Matrix4d();
  
  public Group() {
    transformMat = new Matrix4d();
    transformMat.setIdentity();
  }
  
  /**
   * Compute tMat, tMatInv, tMatTInv for this group and propagate values to the children under it.
   * @param pMat The transformation matrix of the parent for this node.
   * @param pMatInv The inverse of pMat.
   * @param pMatTInv The inverse of the transpose of pMat.
   */
  public void setTransformation(Matrix4d pMat, Matrix4d pMatInv, Matrix4d pMatTInv) {
    // TODO#A7: Compute tMat, tMatInv, tMatTInv using transformMat.
    // Hint: We apply the transformation from bottom up the tree. 
    // i.e. The child's transformation will be applied to objects before its parent's.
	  this.tMat = new Matrix4d();
	  this.tMatInv = new Matrix4d();
	  this.tMatTInv = new Matrix4d();
	  
	  this.tMat.set(pMat.clone()).mulBefore(transformMat);
	  this.tMatInv.set(pMatInv.clone().mulAfter(transformMat.clone().invert()));
	  this.tMatTInv.set(pMatTInv.clone().mulBefore(transformMat.clone().invert().transpose()));
	  //this.tMatInv.set(this.tMat.clone()).invert();
	  //this.tMatTInv.set(this.tMat).invert().transpose();
	  //computeBoundingBox();
	  //System.out.println("tMat:" + this.tMat);
    // TODO#A7: Call setTransformation(tMat, tMatInv, tMatTInv) on each of the children.
	  for (Surface s : objs){

		  s.setTransformation(this.tMat.clone(), this.tMatInv.clone(), this.tMatTInv.clone());
	  }
	
  }
  
  
  public void setTranslate(Vector3d T) {
	Matrix4d.createTranslation(T, tmp);
    transformMat.mulAfter(tmp);
  }
  
  public void setRotate(Vector3d R) {
    Matrix4d.createRotationX(R.x * Math.PI/180, tmp);
    transformMat.mulAfter(tmp);
    Matrix4d.createRotationY(R.y * Math.PI/180, tmp);
    transformMat.mulAfter(tmp);
    Matrix4d.createRotationZ(R.z * Math.PI/180, tmp);
    transformMat.mulAfter(tmp);
  }
  
  public void setScale(Vector3d S) { 
	Matrix4d.createScale(S, tmp);
    transformMat.mulAfter(tmp);
  }
  
  public void addSurface(Surface a) {
    objs.add(a);
  }
  
  public boolean intersect(IntersectionRecord outRecord, Ray ray) { return false; }
  public void computeBoundingBox() {  }

  public void appendRenderableSurfaces (ArrayList<Surface> in) {
    for (Iterator<Surface> iter = objs.iterator(); iter.hasNext();)
      iter.next().appendRenderableSurfaces(in);
  }
}