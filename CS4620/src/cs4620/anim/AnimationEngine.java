package cs4620.anim;

import java.util.HashMap;
import java.util.Iterator;

import cs4620.common.Scene;
import cs4620.common.SceneObject;
import cs4620.common.event.SceneTransformationEvent;
import egl.math.Matrix3;
import egl.math.Matrix4;
import egl.math.Quat;
import egl.math.Vector3;
import egl.math.Vector4;

/**
 * A Component Resting Upon Scene That Gives
 * Animation Capabilities
 * @author Cristian
 *
 */
public class AnimationEngine {
	/**
	 * The First Frame In The Global Timeline
	 */
	private int frameStart = 0;
	/**
	 * The Last Frame In The Global Timeline
	 */
	private int frameEnd = 100;
	/**
	 * The Current Frame In The Global Timeline
	 */
	private int curFrame = 0;
	/**
	 * Scene Reference
	 */
	private final Scene scene;
	/**
	 * Animation Timelines That Map To Object Names
	 */
	public final HashMap<String, AnimTimeline> timelines = new HashMap<>();

	/**
	 * An Animation Engine That Works Only On A Certain Scene
	 * @param s The Working Scene
	 */
	public AnimationEngine(Scene s) {
		scene = s;
	}
	
	/**
	 * Set The First And Last Frame Of The Global Timeline
	 * @param start First Frame
	 * @param end Last Frame (Must Be Greater Than The First
	 */
	public void setTimelineBounds(int start, int end) {
		// Make Sure Our End Is Greater Than Our Start
		if(end < start) {
			int buf = end;
			end = start;
			start = buf;
		}
		
		frameStart = start;
		frameEnd = end;
		moveToFrame(curFrame);
	}
	/**
	 * Add An Animating Object
	 * @param oName Object Name
	 * @param o Object
	 */
	public void addObject(String oName, SceneObject o) {
		timelines.put(oName, new AnimTimeline(o));
	}
	/**
	 * Remove An Animating Object
	 * @param oName Object Name
	 */
	public void removeObject(String oName) {
		timelines.remove(oName);
	}

	/**
	 * Set The Frame Pointer To A Desired Frame (Will Be Bounded By The Global Timeline)
	 * @param f Desired Frame
	 */
	public void moveToFrame(int f) {
		if(f < frameStart) f = frameStart;
		else if(f < frameEnd) f = frameEnd;
		curFrame = f;
	}
	/**
	 * Looping Forwards Play
	 * @param n Number Of Frames To Move Forwards
	 */
	public void advance(int n) {
		curFrame += n;
		if(curFrame > frameEnd) curFrame = frameStart + (curFrame - frameEnd - 1);
	}
	/**
	 * Looping Backwards Play
	 * @param n Number Of Frames To Move Backwards
	 */
	public void rewind(int n) {
		curFrame -= n;
		if(curFrame < frameStart) curFrame = frameEnd - (frameStart - curFrame - 1);
	}

	public int getCurrentFrame() {
		return curFrame;
	}
	public int getFirstFrame() {
		return frameStart;
	}
	public int getLastFrame() {
		return frameEnd;
	}
	public int getNumFrames() {
		return frameEnd - frameStart + 1;
	}

	/**
	 * Adds A Keyframe For An Object At The Current Frame
	 * Using The Object's Transformation - (CONVENIENCE METHOD)
	 * @param oName Object Name
	 */
	public void addKeyframe(String oName) {
		AnimTimeline tl = timelines.get(oName);
		if(tl == null) return;
		tl.addKeyFrame(getCurrentFrame(), tl.object.transformation);
	}
	/**
	 * Removes A Keyframe For An Object At The Current Frame
	 * Using The Object's Transformation - (CONVENIENCE METHOD)
	 * @param oName Object Name
	 */
	public void removeKeyframe(String oName) {
		AnimTimeline tl = timelines.get(oName);
		if(tl == null) return;
		tl.removeKeyFrame(getCurrentFrame(), tl.object.transformation);
	}
	
	/**
	 * Loops Through All The Animating Objects And Updates Their Transformations To
	 * The Current Frame - For Each Updated Transformation, An Event Has To Be 
	 * Sent Through The Scene Notifying Everyone Of The Change
	 */
	public void updateTransformations() {
		// TODO: Loop Through All The Timelines
		// And Update Transformations Accordingly
		// (You WILL Need To Use this.scene)
		boolean useCatmullRom = false;
		if(!useCatmullRom){
			int currentframe = this.getCurrentFrame();
			AnimKeyframe curFrame = new AnimKeyframe(currentframe);
			System.out.println("CURRENT FRAME:" + getCurrentFrame());
			//For Every timeline
			for(AnimTimeline a: timelines.values()){
				Matrix4 curTransf = new Matrix4();
	
				if (a.frames.contains(curFrame)){
					//Iterate over frames in the timeline
					Iterator<AnimKeyframe> it = a.frames.iterator();
					//Find animkeyframe with correct frame number
					while(it.hasNext()){
						AnimKeyframe current = it.next();
						//Correct frame number found
						if (current.frame == currentframe){
							//Set current transformation
							curTransf.set(current.transformation);
							break;
						}
					}
				}else{
					//Previous and Next AnimKeyFrames of Current Frame
					AnimKeyframe floor = a.frames.floor(curFrame);
					AnimKeyframe ceil = a.frames.ceiling(curFrame);
					
					if (floor == null) floor = a.frames.first();
					if (ceil == null) ceil = a.frames.last();
					
					//Floor Translation Matrix
					Vector3 floorTransVec = new Vector3(floor.transformation.getTrans());
					//Ceil Translation Matrix				
					Vector3 ceilTransVec = new Vector3(ceil.transformation.getTrans());

					//Polar Decomposition of RS
					Matrix3 floorRS = new Matrix3(floor.transformation);
					Matrix3 ceilRS = new Matrix3(ceil.transformation);
					Matrix3 floorRot = new Matrix3();
					Matrix3 floorScale = new Matrix3();
					Matrix3 ceilRot = new Matrix3();
					Matrix3 ceilScale = new Matrix3();
					floorRS.polar_decomp(floorRot, floorScale);
					ceilRS.polar_decomp(ceilRot, ceilScale);
					
					//Quaternions and Spherical Interpolation
					Quat quatFloor = new Quat(floorRot).normalize();
					Quat quatCeil = new Quat(ceilRot).normalize();
					Vector4 qF = new Vector4(quatFloor.w,quatFloor.x,quatFloor.y,quatFloor.z);
					Vector4 qC = new Vector4(quatCeil.w, quatCeil.x, quatCeil.y, quatCeil.z);
					float quatDot = qF.dot(qC);
					
					//Fixes slerping issue of q and -q being the same rotation
					if (quatDot <= 0){
						quatCeil.negate();
						qC.negate();
					}
					//Find Angle
					double psi = Math.acos(qF.dot(qC));
							
					//Calculate t-value
					float framediff = (float) (ceil.frame - floor.frame);
					float t =0.0f;
					if(framediff != 0.0f) {
						t = (currentframe - floor.frame)/framediff;
					}
					Matrix4 interpRot = new Matrix4();
					// according to notes, psi will be innaccurate if it is small
					if (psi < 0.01f){
						Matrix3 interpRot3D = new Matrix3(floorRot);
						interpRot3D.interpolate(floorRot, ceilRot, (float)t);
						Matrix4 temp = new Matrix4(interpRot3D);
						interpRot.set(temp);
					}else {
						float floorCoeff = (float) (Math.sin((1.0-t)*psi)/Math.sin(psi));
						float ceilCoeff = (float) (Math.sin(t*psi)/Math.sin(psi));
						Vector4 interp = new Vector4(qF.clone().mul(floorCoeff).add(qC.clone().mul(ceilCoeff)));
						Quat quatInterp = new Quat(interp.x,interp.y,interp.z, interp.w);
						quatInterp.toRotationMatrix(interpRot);
					}
					
					//Translation and Scale Linear Interpolation
					Matrix3 interpScale = new Matrix3();
					interpScale.interpolate(floorScale, ceilScale, t);
					Matrix4 interpScale4D = new Matrix4(interpScale);
					
					Vector3 interpTrans = new Vector3(floorTransVec);
					interpTrans.lerp(ceilTransVec, (float) t);
					Matrix4 interpTrans4D = new Matrix4();
					Matrix4.createTranslation(interpTrans,interpTrans4D);
					curTransf.set(interpTrans4D.clone().mulBefore(interpRot.clone().mulBefore(interpScale4D.clone())));
				}
	
				//Set current transformation of object and sendEvent
				a.object.transformation.set(curTransf);
				scene.sendEvent(new SceneTransformationEvent(a.object)); 
			}
		} else {
			// catmull-rom spline interpolation
			int currentframe = this.getCurrentFrame();
			AnimKeyframe curFrame = new AnimKeyframe(currentframe);
			System.out.println("CURRENT FRAME:" + getCurrentFrame());
			//For Every timeline
			for(AnimTimeline a: timelines.values()){
				Matrix4 curTransf = new Matrix4();
	
				if (a.frames.contains(curFrame)){
					//Iterate over frames in the timeline
					Iterator<AnimKeyframe> it = a.frames.iterator();
					//Find animkeyframe with correct frame number
					while(it.hasNext()){
						AnimKeyframe current = it.next();
						//Correct frame number found
						if (current.frame == currentframe){
							//Set current transformation
							curTransf.set(current.transformation);
							break;
						}
					}
				}else{
					// set control frames 
					AnimKeyframe f0;
					AnimKeyframe f1 = a.frames.floor(curFrame);
					AnimKeyframe f2 = a.frames.ceiling(curFrame);
					AnimKeyframe f3;
					
					// logic for edge cases
					
					if (f1 == null){
						f1 = a.frames.first();
						f0 = a.frames.first();
					} else {
						AnimKeyframe f1MinusOne = new AnimKeyframe(f1.frame - 1);
						f0 = a.frames.floor(f1MinusOne);
						if (f0 == null) f0 = a.frames.first();
					}
					
					if (f2 == null) {
						f2 = a.frames.last();
						f3 = a.frames.last();
					} else {
						AnimKeyframe f2PlusOne = new AnimKeyframe(f2.frame + 1);
						f3 = a.frames.ceiling(f2PlusOne);
						if (f3 == null) f3 = a.frames.last();	
					}

					Vector3 f0TransVec = new Vector3(f0.transformation.getTrans());
					Vector3 f1TransVec = new Vector3(f1.transformation.getTrans());
					Vector3 f2TransVec = new Vector3(f2.transformation.getTrans());
					Vector3 f3TransVec = new Vector3(f3.transformation.getTrans());
			
					//System.out.println("floorTransVec:" + floorTransVec);
					//System.out.println("ceilTransVec:" + ceilTransVec);

					//Polar Decomposition of RS
					Matrix3 f0RS = new Matrix3(f0.transformation);
					Matrix3 f1RS = new Matrix3(f1.transformation);
					Matrix3 f2RS = new Matrix3(f2.transformation);
					Matrix3 f3RS = new Matrix3(f3.transformation);
					Matrix3 f0Rot = new Matrix3();
					Matrix3 f1Rot = new Matrix3();
					Matrix3 f2Rot = new Matrix3();
					Matrix3 f3Rot = new Matrix3();
					Matrix3 f0Scale = new Matrix3();
					Matrix3 f1Scale = new Matrix3();
					Matrix3 f2Scale = new Matrix3();
					Matrix3 f3Scale = new Matrix3();

					f0RS.polar_decomp(f0Rot, f0Scale);
					f1RS.polar_decomp(f1Rot, f1Scale);
					f2RS.polar_decomp(f2Rot, f2Scale);
					f3RS.polar_decomp(f3Rot, f3Scale);
					
					//Quaternions and Spherical Interpolation (just using the interpolated CPs)
					Quat quatF1 = new Quat(f1Rot).normalize();
					Quat quatF2 = new Quat(f2Rot).normalize();
					Vector4 qF1 = new Vector4(quatF1.w,quatF1.x,quatF1.y,quatF1.z);
					Vector4 qF2 = new Vector4(quatF2.w, quatF2.x, quatF2.y, quatF2.z);
					float quatDot = qF1.dot(qF2);
					
					//Fixes slerping issue of q and -q being the same rotation
					if (quatDot <= 0){
						quatF2.negate();
						qF2.negate();
					}
					//Find Angle
					double psi = Math.acos(qF1.dot(qF2));
										
					//Calculate t-value
					float framediff = (float) (f2.frame - f1.frame);
					float t =0.0f;
					if(framediff != 0.0f) {
						t = (currentframe - f1.frame)/framediff;
					}
					Matrix4 interpRot = new Matrix4();

					if (psi < 0.01f){
						Matrix3 interpRot3D = new Matrix3(f1Rot);
						interpRot3D.interpolate(f1Rot, f2Rot, (float)t);
						Matrix4 temp = new Matrix4(interpRot3D);
						interpRot.set(temp);
					}else {
						float floorCoeff = (float) (Math.sin((1.0-t)*psi)/Math.sin(psi));
						float ceilCoeff = (float) (Math.sin(t*psi)/Math.sin(psi));
						Vector4 interp = new Vector4(qF1.clone().mul(floorCoeff).add(qF2.clone().mul(ceilCoeff)));
						Quat quatInterp = new Quat(interp.x,interp.y,interp.z, interp.w);
						quatInterp.toRotationMatrix(interpRot);
					}
					
					// Catmull-Rom Spline calcuation
					Matrix4 u = new Matrix4(.5f, t/2, (float) Math.pow(t,2)/2, (float)Math.pow(t,3)/2,
											0f, 0f, 0f, 0f,
											0f, 0f, 0f, 0f,
											0f, 0f, 0f, 0f);
					Matrix4 A = new Matrix4(0f, 2f, 0f, 0f,
											-1f,0f, 1f, 0f,
											2f,-5f, 4f,-1f,
											-1f,3f, -3f,1f);
					u.mulBefore(A);
					
					// Use basis function to interpolate scale
					Matrix3 interpScale = new Matrix3();
					Matrix3 scalarT = new Matrix3().setIdentity();
					// y u no scalar multiply?
					scalarT.set(0,0,u.get(0,0));
					scalarT.set(1,1,u.get(0,0));
					scalarT.set(2,2,u.get(0,0));
					interpScale.set(f0Scale.clone().mulAfter(scalarT));
					scalarT.set(0,0,u.get(0,1));
					scalarT.set(1,1,u.get(0,1));
					scalarT.set(2,2,u.get(0,1));
					interpScale.add(f1Scale.clone().mulAfter(scalarT));
					scalarT.set(0,0,u.get(0,2));
					scalarT.set(1,1,u.get(0,2));
					scalarT.set(2,2,u.get(0,2));
					interpScale.add(f2Scale.clone().mulAfter(scalarT));
					scalarT.set(0,0,u.get(0,3));
					scalarT.set(1,1,u.get(0,3));
					scalarT.set(2,2,u.get(0,3));
					interpScale.add(f3Scale.clone().mulAfter(scalarT));
					Matrix4 interpScale4D = new Matrix4(interpScale);

					// Use basis function to interpolate translation
					Vector3 interpTrans = new Vector3(f0TransVec).mul(u.get(0,0));
					interpTrans.add(new Vector3(f1TransVec).mul(u.get(0,1)));
					interpTrans.add(new Vector3(f2TransVec).mul(u.get(0,2)));
					interpTrans.add(new Vector3(f3TransVec).mul(u.get(0,3)));
					Matrix4 interpTrans4D = new Matrix4();
					Matrix4.createTranslation(interpTrans,interpTrans4D);
					
					// do the TRS multiplication
					curTransf.set(interpTrans4D.clone().mulBefore(interpRot.clone().mulBefore(interpScale4D.clone())));
				}
	
	
				//Set current transformation of object and sendEvent
				a.object.transformation.set(curTransf);
				scene.sendEvent(new SceneTransformationEvent(a.object)); 
			}
		}
	}
}
