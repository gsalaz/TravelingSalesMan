package cmsc420_s21; // Don't delete this or your file won't pass the autograder

import java.util.ArrayList;

import cmsc420_s21.AAXTree.Node;

/**
 * WKDTree (skeleton)
 *
 * MODIFY THE FOLLOWING CLASS.
 *
 * You are free to make whatever changes you like or to create additional
 * classes and files.
 */

public class WKDTree<LPoint extends LabeledPoint2D> {

	// -----------------------------------------------------------------
	// Public members - You should not modify the function signatures
	// -----------------------------------------------------------------
	Node root;							//holds the root node
	int size;							//holds the size of the tree
	ArrayList<String> preList;			//stores list when preordering
	
	//Abstract class for both types of nodes. 	
	private abstract class Node {
		
		//General Operations
		abstract LPoint find (Point2D pt);
		abstract Node insert (LPoint pt) throws Exception;
		abstract Node delete(Point2D pt) throws Exception;
		
		//Make Preorder List
		abstract void getPreorderList();
		
		//Getter and setter for wrapper
		abstract Rectangle2D getWrapper();
		abstract void setWrapper(Rectangle2D w);
		
		//Get max and min for x and y
		abstract LPoint getMinX();
		abstract LPoint getMaxX();	
		abstract LPoint getMinY();
		abstract LPoint getMaxY();
		
		//Get closest smaller or bigger point
		abstract LPoint findSmallerX(float x, LPoint best);
		abstract LPoint findLargerX(float x, LPoint best);
		abstract LPoint findSmallerY(float y, LPoint best);
		abstract LPoint findLargerY(float y, LPoint best);
		
		//Get all points in a range
		abstract ArrayList<LPoint> circularRange(Point2D center, float sqRadius, 
				ArrayList<LPoint>cirList);
		
		abstract LPoint fixedRadNN(Point2D q, double sqRadius, LPoint best);
	}
	
	
	/*
	 * Internal nodes of structure which contain the the cutting dimension
	 * cutting value, the wrapper (biggest and smallest x and y) and the 
	 * left and right node.
	 */
	private class InternalNode extends Node{
		int cutDim;
		float cutVal;
		Rectangle2D wrapper;
		Node left, right;
		
		//Basic Constructor
		public InternalNode(int cD, float cV, Rectangle2D w, Node l, Node r) {
			cutDim = cD;
			cutVal = cV;
			wrapper = w;
			left = l;
			right = r;
		}
		
		//Go left or right depending if pt is smaller then where 
		//the cut is.
		LPoint find(Point2D pt) {
			if(pt.get(cutDim) < cutVal)
				return left.find(pt);
			else
				return right.find(pt);
		}
		
		//Move left or right and add point to wrapper after
		Node insert(LPoint pt) throws Exception {
			if(pt.get(cutDim) < cutVal)
				left = left.insert(pt);
			else
				right = right.insert(pt);
			
			wrapper.add(pt.getPoint2D());
			
			return this;
		}
		
		//Add internal node and move left then right
		void getPreorderList() {
			if(cutDim == 0)
				preList.add("(x=" + cutVal + "): " + wrapper.toString());
			else
				preList.add("(y=" + cutVal + "): " + wrapper.toString());
			left.getPreorderList();
			right.getPreorderList();		
		}
		
		//Move left or right depending on cutDim and cutVal
		//if null returned return back the other child. Fix wrapper 
		//if needed.
		Node delete(Point2D pt) throws Exception {
			boolean isLeft = pt.get(cutDim) < cutVal;
			Node returnNode;
			
			if(isLeft) 
				returnNode =  left.delete(pt);
			else 
				returnNode =  right.delete(pt);
			
			if(returnNode == null)
				return (isLeft ? right : left);
			else {
				if(isLeft)
					left = returnNode;
				else
					right = returnNode;
				
				setWrapper(Rectangle2D.union(left.getWrapper(), right.getWrapper()));
				
				return this;
			}
		}
		
		//Getter an setter for wrapper
		Rectangle2D getWrapper() { return wrapper; }
		void setWrapper(Rectangle2D w) { wrapper = w; }
		
		//if cut by x axis and less just search left side otherwise
		//get min of both children and choose the closer one.
		LPoint getMinX() {
			if(cutDim == 0)
				return left.getMinX();
			else {
				LPoint leftMin = left.getMinX();
				LPoint rightMin = right.getMinX();
				
				if(leftMin.getX() < rightMin.getX())
					return leftMin;
				else if (leftMin.getX() > rightMin.getX())
					return rightMin;
				else {
					if(leftMin.getY() < rightMin.getY())
						return leftMin;
					else
						return rightMin;
				}
			}
		}
		
		//if cut by x axis and greater just search right side otherwise
		//get max of both children and choose the closer one.
		LPoint getMaxX() {
			if(cutDim == 0)
				return right.getMaxX();
			else {
				LPoint leftMax = left.getMaxX();
				LPoint rightMax = right.getMaxX();
				
				if(leftMax.getX() > rightMax.getX())
					return leftMax;
				else if (leftMax.getX() < rightMax.getX())
					return rightMax;
				else {
					if(leftMax.getY() > rightMax.getY())
						return leftMax;
					else
						return rightMax;
				}
			}
		}
		
		//if cut by y axis and less just search left side otherwise
		//get min of both children and choose the closer one.
		LPoint getMinY() {
			if(cutDim == 1)
				return left.getMinY();
			else {
				LPoint leftMin = left.getMinY();
				LPoint rightMin = right.getMinY();
				
				if(leftMin.getY() < rightMin.getY())
					return leftMin;
				else if (leftMin.getY() > rightMin.getY())
					return rightMin;
				else {
					if(leftMin.getX() < rightMin.getX())
						return leftMin;
					else
						return rightMin;
				}
			}
		}
		
		//if cut by y axis and greater just search right side otherwise
		//get max of both children and choose the closer one.
		LPoint getMaxY() {
			if(cutDim == 1)
				return right.getMaxY();
			else {
				LPoint leftMax = left.getMaxY();
				LPoint rightMax = right.getMaxY();
				
				if(leftMax.getY() > rightMax.getY())
					return leftMax;
				else if (leftMax.getY() < rightMax.getY())
					return rightMax;
				else {
					if(leftMax.getX() > rightMax.getX())
						return leftMax;
					else
						return rightMax;
				}
			}
		}

		/*
		 * if wrapper worst then best or on other side (larger or smaller) 
		 * return best otherwise get the best of the left and right side.
		 */
		LPoint findSmallerX(float x, LPoint best) {
			
			if(best != null && 
					(wrapper.getHigh().getX() < best.getX() || wrapper.getLow().getX() > x)) {
				return best;
			}
			else {
				best = left.findSmallerX(x, best);
				best = right.findSmallerX(x, best);
				return best;
			}
			
		}
		LPoint findLargerX(float x, LPoint best) {
			
			if(best != null && 
					(wrapper.getLow().getX() > best.getX() || wrapper.getHigh().getX() < x)) {
				return best;
			}
			else {
				best = left.findLargerX(x, best);
				best = right.findLargerX(x, best);
				return best;
			}
		}
		LPoint findSmallerY(float y, LPoint best) {
			
			if((best != null) && 
					(wrapper.getHigh().getY() < best.getY() || wrapper.getLow().getY() > y)) {
				return best;
			}
			else {
				best = left.findSmallerY(y, best);
				best = right.findSmallerY(y, best);
				return best;
			}
		}
		LPoint findLargerY(float y, LPoint best) {

			if(best != null  &&
					(wrapper.getLow().getY() > best.getY() || wrapper.getHigh().getY() < y)) {
				return best;
			}
			else {
				best = left.findLargerY(y, best);
				best = right.findLargerY(y, best);
				return best;
			}
		}

		
		ArrayList<LPoint> circularRange(Point2D center, float sqRadius, ArrayList<LPoint> cirList) {
			
			if(wrapper.distanceSq(center) <= sqRadius) {
				cirList = left.circularRange(center, sqRadius, cirList);
				cirList = right.circularRange(center, sqRadius, cirList);
			}
			
			return cirList;
		}

		//return closes point to q thats within radius
		LPoint fixedRadNN(Point2D q, double sqRadius, LPoint best) {
			double wrapperDis = wrapper.distanceSq(q);
			
			if(wrapperDis >= sqRadius) {
				return best;
			}
			else {
				if (best != null && wrapperDis > q.distanceSq(best.getPoint2D())) {
					return best;
				}
				else {
					best = left.fixedRadNN(q, sqRadius, best);
					best = right.fixedRadNN(q, sqRadius, best);
					return best;
				}		
			}
		}
			
	}
		
	/*
	 * External nodes of tree. Has the point and a dummy wrapper with
	 * the same point on both corners.
	 */
	private class ExternalNode extends Node{
		LPoint point;
		Rectangle2D wrapper;
		
		//Basic Constructor
		public ExternalNode(LPoint newpt){
			point = newpt;
			wrapper = new Rectangle2D(point.getPoint2D(), point.getPoint2D());
		}

		//return point if found
		LPoint find(Point2D pt) {
			if(pt.equals(point.getPoint2D()))
				return point;
			else
				return null;
		}

		//if point same return error other wise add make external node, 
		//calculate the cutDim and cutVal to make new internal value and return
		Node insert(LPoint pt) throws Exception {
			if(point.getPoint2D().equals(pt.getPoint2D()))
				throw new Exception("Insertion of point with duplicate coordinates");
			else {
				ExternalNode newExt = new ExternalNode(pt);
				
				Rectangle2D newRect = new Rectangle2D(point.getPoint2D(), pt.getPoint2D());
				int cutDim = newRect.getWidth(0) >= newRect.getWidth(1) ? 0 : 1;
				float cutVal = (float) ((newRect.getHigh().get(cutDim) + newRect.getLow().get(cutDim)) / 2);
				
				ExternalNode left;
				ExternalNode right;
				if(point.get(cutDim) < cutVal) {
					left = this;
					right = newExt;
				}
				else {
					left = newExt;
					right = this;
				}
				
				InternalNode newInter = new InternalNode(cutDim, cutVal, newRect, left, right);
				size++;
				
				return newInter;
			}
			
		}

		//add point
		void getPreorderList() {
			preList.add("[" + point.toString() + "]");
		}

		//return null if found to start deletion above
		Node delete(Point2D pt) throws Exception {
			if(point.getPoint2D().equals(pt)) {
				size--;
				return null;
			}
			
			throw new Exception ("Deletion of nonexistent point");
		}

		
		//Getter and setter for dummy wrapper
		Rectangle2D getWrapper() { return wrapper; }
		void setWrapper(Rectangle2D w) { wrapper = w; }

		//return this point
		LPoint getMinX() { return point; }
		LPoint getMaxX() { return point; }
		
		LPoint getMinY() { return point; }
		LPoint getMaxY() { return point; }

		
		/*
		 * Check if on the correct side if so determine if point
		 * or best is better and return it. If first axis align 
		 * check second.
		 */
		LPoint findSmallerX(float x, LPoint best) {
			if (point.getX() < x) {
				
				if (best == null || point.getX() > best.getX())
					return point;
				else if(point.getX() < best.getX())
					return best;
				else {
					return (point.getY() < best.getY()) ? best : point;
				}
			}
			else 
				return best;
		}
		LPoint findLargerX(float x, LPoint best) {
			if (point.getX() > x) {
				
				if (best == null || point.getX() < best.getX())
					return point;
				else if(point.getX() > best.getX())
					return best;
				else {
					return (point.getY() > best.getY()) ? best : point;
				}
			}
			else 
				return best;
		}
		LPoint findSmallerY(float y, LPoint best) {
			if (point.getY() < y) {
				
				if (best == null || point.getY() > best.getY())
					return point;
				else if(point.getY() < best.getY())
					return best;
				else {
					return (point.getX() < best.getX()) ? best : point;
				}
			}
			else 
				return best;
		}

		LPoint findLargerY(float y, LPoint best) {
			if (point.getY() > y) {
				
				if (best == null || point.getY() < best.getY())
					return point;
				else if(point.getY() > best.getY())
					return best;
				else {
					return (point.getX() > best.getX()) ? best : point;
				}
			}
			else 
				return best;
		}

		//Check if point is in the range.
		ArrayList<LPoint> circularRange(Point2D center, float sqRadius, ArrayList<LPoint> cirList) {
			
			if(wrapper.distanceSq(center) <= sqRadius)
				cirList.add(point);
			
			return cirList;
		}

		//if this point is closer than best return this point
		LPoint fixedRadNN(Point2D q, double sqRadius, LPoint best) {
			if(q.distanceSq(point.getPoint2D()) < sqRadius) {
				if ((best == null || q.distanceSq(point.getPoint2D()) < q.distanceSq(best.getPoint2D()))
						&& q.distanceSq(point.getPoint2D()) > 0) {
					return point;
				}
				else {
					return best;
				}
			}
			return best;
		}
		
	}

	
	/*
	 * ALL THE TOP LEVEL CASES THAT CALL HELPER FUNCTIONS IN NODES
	 */
	public WKDTree() { 
		root = null;
		size = 0;
	}
	
	public LPoint find(Point2D pt) { 
		if (root == null)
			return null;
		else
			return root.find(pt); 
	}
	
	public void insert(LPoint pt) throws Exception {
		if(root == null) {
			root = new ExternalNode(pt);
			size++;
		}
		else
			root = root.insert(pt);
	}
	
	public void delete(Point2D pt) throws Exception { 
		if(root == null)
			throw new Exception ("Deletion of nonexistent point");
		else
			root = root.delete(pt);
		
	}
	
	public ArrayList<String> getPreorderList() { 
		preList = new ArrayList<String>();
		
		if (root != null)
			root.getPreorderList();
		
		return preList; 
	}
	public void clear() { 
		root = null;
		size = 0;
	}
	
	public int size() { return size; }
	
	public LPoint getMinX() { 
		if(root != null)
			return root.getMinX(); 
		
		return null;
	}
	public LPoint getMaxX() { 
		if(root != null)
			return root.getMaxX(); 
		
		return null;
	}
	
	public LPoint getMinY() { 
		if(root != null)
			return root.getMinY(); 
	
		return null;
	}
	public LPoint getMaxY() {
		if(root != null)
			return root.getMaxY(); 
		
		return null;
	}
	
	public LPoint findSmallerX(float x) { 
		if(root == null)
			return null;
		
		return root.findSmallerX(x, null); //root.getMinX();
	}
	public LPoint findLargerX(float x)  { 
		if(root == null)
			return null;
		
		return root.findLargerX(x, null); 
	}
	public LPoint findSmallerY(float y) { 
		if(root == null)
			return null;
		
		return root.findSmallerY(y, null); 
	}
	public LPoint findLargerY(float y)  { 
		if(root == null)
			return null;
		
		return root.findLargerY(y, null); 
	}
	
	public ArrayList<LPoint> circularRange(Point2D center, float sqRadius) { 
		ArrayList<LPoint> cirList = new ArrayList<LPoint>();
		
		if(root != null)
			cirList = root.circularRange(center, sqRadius, cirList);
		
		return cirList; 
	}
	
	public LPoint fixedRadNN(Point2D q, double sqRadius) {
		if (root != null) {
			return root.fixedRadNN(q, sqRadius, null);
		}
		return null;
	}

}
