package cmsc420_s21; // Don't delete this or your file won't pass the autograder

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Tour (skeleton)
 *
 * MODIFY THE FOLLOWING CLASS.
 *
 * You are free to make whatever changes you like or to create additional
 * classes and files.
 */

public class Tour<LPoint extends LabeledPoint2D> {
	
	ArrayList<LPoint> allPoints;
	AAXTree<String, Integer> locator;
	WKDTree<LPoint> wkdTree;
	
	//basic constructor
	public Tour() { 
		allPoints = new ArrayList<LPoint>(); 		//hold tour of points
		locator = new AAXTree<String, Integer>();	//aaxtree to get index from label
		wkdTree = new WKDTree<LPoint>();			//wkdtree for range queries
	}
	
	public void append(LPoint pt) throws Exception { 
		if(locator.find(pt.getLabel()) != null) {
			throw new Exception("Duplicate label");
		}
		if(allPoints.contains(pt)) {
			throw new Exception("Duplicate coordinates");
		}
		else {	//add to all the trees and list
			allPoints.add(pt);
			locator.insert(pt.getLabel(), allPoints.size() - 1);
			wkdTree.insert(pt);
		}
		
	}
	
	public ArrayList<LPoint> list() { 
		return allPoints; 
	}
	
	public void clear() { 
		allPoints = new ArrayList<LPoint>();
		locator = new AAXTree<String, Integer>();
		wkdTree = new WKDTree<LPoint>();
	}
	
	//add up total cost of tour through list
	public double cost() {
		double total = 0;
		
		for(int i = 0; i < allPoints.size(); i++) {
			total += allPoints.get(i).getPoint2D()
					.distanceSq(allPoints.get((i+1) % allPoints.size()).getPoint2D());
		}
		return total; 
	}
	
	//Make i go to j+1 then go in reverse until i+1 then go to j 
	public void reverse(String label1, String label2) throws Exception { 
		if(locator.find(label1) == null  || locator.find(label2) == null) {
			throw new Exception("Label not found");
		}
		if(label1.equals(label2)) {
			throw new Exception("Duplicate label");
		}
		//get higher and lower index for i j
		int i = Math.min(locator.find(label1),locator.find(label2));
		int j = Math.max(locator.find(label1),locator.find(label2));
		int revSize = j - i + 1;
		
		//make list 0 to i
		ArrayList<LPoint> newLst = new ArrayList<LPoint>(allPoints.subList(0, i + 1)); 
		
		//add all points going backwards from j to i
		for(int newPos = i + 1, rIter = j; rIter >= i + 1; rIter--, newPos++) {	
			newLst.add(allPoints.get(rIter));
			locator.update(allPoints.get(rIter).getLabel(), newPos);
		}
		
		//add rest of points (j to end)
		newLst.addAll(allPoints.subList(j+1, allPoints.size()));					
		allPoints = newLst;
				
	}
	
	//if swapping edges make for shorter path do it
	public boolean twoOpt(String label1, String label2) throws Exception {
		
		if(locator.find(label1) == null || locator.find(label2) == null) {
			throw new Exception ("Label not found");
		}
		
		int i = locator.find(label1);
		int j = locator.find(label2);
		
		int size = allPoints.size();
		
		Point2D pi = allPoints.get(i % size).getPoint2D();			//label1 point
		Point2D piPlus1 = allPoints.get((i+1) % size).getPoint2D();	//next point from label1
		
		Point2D pj = allPoints.get(j % size).getPoint2D();			//label2 point
		Point2D pjPlus1 = allPoints.get((j+1) % size).getPoint2D();	//next point from label2
		
		//if improve the distance then do reversal
		if(((pi.distanceSq(pj) + piPlus1.distanceSq(pjPlus1)) - (pi.distanceSq(piPlus1) + pj.distanceSq(pjPlus1)))
				< 0) {
			reverse(label1, label2);
			return true;
		}
		
		return false; 
	}
	
	//find point closer to this point then swap edges
	public LPoint twoOptNN(String label) throws Exception { 
		
		if(locator.find(label) == null) {
			throw new Exception("Label not found");
		}
		
		int i = locator.find(label);
		int size = allPoints.size();
		
		Point2D pi = allPoints.get(i % size).getPoint2D();
		Point2D piPlus1 = allPoints.get((i+1) % size).getPoint2D();
		
		//get nearest neighbor and if better then current next point swap edges
		LPoint nearest = wkdTree.fixedRadNN(pi, pi.distanceSq(piPlus1));
		
		if(nearest != null && twoOpt(label, nearest.getLabel())) {
			return nearest;
		}
		
		return null; 
	}
	
	//brute force keep swapping for all possible points
	public int allTwoOpt() throws Exception { 
		int totalTwoOpts = 0;
		
		for(int i = 0; i < allPoints.size(); i++) {
			for(int j = i + 1; j < allPoints.size(); j++) {
				if (twoOpt(allPoints.get(i).getLabel(), allPoints.get(j).getLabel())) {
					totalTwoOpts++;
				}
			}
		}
		
		return totalTwoOpts; 
	}

}
