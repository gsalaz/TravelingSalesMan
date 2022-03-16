package cmsc420_s21; // Don't delete this or your file won't pass the autograder

import java.util.ArrayList;

/** AAXTree (skeleton)
*
* MODIFY THE FOLLOWING CLASS.
*
* You are free to make whatever changes you like or to create additional
* classes and files, but avoid reusing/modifying the other files given in this
* folder.
*/

public class AAXTree<Key extends Comparable<Key>, Value> {
	
	Node root;						//root of tree
	int size;						//num of nodes in tree
	ArrayList<String> preList;		//prelist of nodes

	//------------------------------------------------------------------
	// Needed for Part a
	//------------------------------------------------------------------
	
	//Abstract class for both types of nodes
	public abstract class Node {
		Key key;
		
		abstract Value find (Key x);
		abstract Node insert (Key x, Value v) throws Exception;
		
		abstract Node getLeft();
		abstract void setLeft(Node n);
		abstract Node getRight();
		abstract void setRight(Node n);
		
		abstract int getLevel();
		abstract void setLevel(int lvl);
		
		abstract Node skew();
		abstract Node split();
		abstract void updateHeight();
		abstract Node fixAfterDelete(Node p);
		
		abstract void getPreorderList();
		
		abstract Node delete(Key x) throws Exception;
		
		abstract Value getMin();
		abstract Value getMax();
		
		abstract Key getMinKey();
		abstract Key getMaxKey();
		
		abstract Value findSmaller(Key x, Node middleNode);
		abstract Value findLarger (Key x, Node middleNode);
		
		abstract void update (Key x, Value v);
	}
	
	/*
	 * Class of internal nodes containing key, left and right
	 * node and the level. All not at the leaf level.
	 */
	private class InternalNode extends Node{
		Key key;
		Node left, right;
		int level;
		
		//Constructor
		public InternalNode(Key k, Node l, Node r, int lvl) {
			key = k;
			left = l;
			right = r;
			level = lvl;
		}
		
		//Getters and setters
		Node getLeft() { return left;}
		void setLeft(Node n) { left = n; };
		Node getRight() { return right;}
		void setRight(Node n) { right = n; };
		int getLevel() { return level;}
		void setLevel(int lvl) { level = lvl; }
		
		//Find node with key
		Value find(Key x) {
			if(x.compareTo(key) < 0)	//x < key
				return left.find(x);
			else						//x >= key
				return right.find(x);
			
		}
		
		//Move to left or right and skew and split on the way up
		Node insert(Key x, Value v) throws Exception { 
			if(x.compareTo(key) < 0) 
				left = left.insert(x, v);
			else
				right = right.insert(x, v);
			
			return this.skew().split();	
		}
		
		//rotate nodes to right
		Node skew() {
			if (this == null)
				return this;
			
			if (this.getLeft().getLevel() == this.getLevel()) {
				Node q = this.getLeft();
				this.setLeft(q.getRight());
				q.setRight(this);
					
				return q;
			}
			else
				return this;	
		}

		//If has double red child split to 2 black with 2 children each
		Node split() {
			
			if (this == null)
				return this;
			
			if (this.getRight().getRight().getLevel() == this.getLevel()) {
				Node q = this.getRight();
				this.setRight(q.getLeft());
				q.setLeft(this);
				q.setLevel(q.getLevel() + 1);
				
				return q;
			}
			else
				return this;
		}
		
		void getPreorderList(){
			
			preList.add("(" + key.toString() + ") " + level);
			left.getPreorderList();
			right.getPreorderList();
		}
		
		
		//Go left or right depending on key and assign return to the correct child
		//if null return delete current node by returning the other child. Fix height and 
		//structure before returning
		Node delete(Key x) throws Exception {
			Node returnValue;
			boolean toLeft = x.compareTo(key) < 0;
			
			if(toLeft)
				returnValue = this.getLeft().delete(x);
			else
				returnValue = this.getRight().delete(x);
			
			//if below level node deleted set this other child
			if(returnValue == null) {
				
				if(toLeft)
					return this.getRight();
				else
					return this.getLeft();
			}
			else {
				if(toLeft)
					this.setLeft(returnValue);
				else
					this.setRight(returnValue);
			}
				
			return fixAfterDelete(this);
		}
		
		//fix by doing 3 skews and 2 splits
		Node fixAfterDelete(Node p) {
			
			p.updateHeight();
			p = p.skew();
			
			//p == p.getRight().skew();
			p.setRight(p.getRight().skew());
			
			//p = p.getRight().getRight().skew();
			p.getRight().setRight(p.getRight().getRight().skew());
			
			p = p.split();
			//p = p.getRight().split();
			p.setRight(p.getRight().split());
			
			return p;
		}
		
		//fix height if not right
		void updateHeight() {
			int l = 1 + Math.min(this.getLeft().getLevel(), this.getRight().getLevel());
			
			if(this.level > l) {
				this.setLevel(l);
				if(this.getRight().getLevel() > l)
					this.getRight().setLevel(l);
			}		
		}
		
		//Get min or max value from tree
		Value getMin() { return this.getLeft().getMin(); }
		Value getMax() { return this.getRight().getMax(); }
		
		//get min or max key from tree
		Key getMinKey() { return this.getLeft().getMinKey(); }
		Key getMaxKey() { return this.getRight().getMaxKey(); }
		
		//go left or right depending on key if its a right make the new
		//middle node to be this
		Value findSmaller(Key x, Node middleNode) {
			if(x.compareTo(key) < 0) {
				return this.getLeft().findSmaller(x, middleNode);
			}
			else {
				return this.getRight().findSmaller(x, this);
			}
				
		}
		
		//go left or right depending on key if its a left make the new
		//middle node to be this
		Value findLarger(Key x, Node middleNode) {
			if(x.compareTo(key) < 0) {
				return this.getLeft().findLarger(x, this);
			}
			else {
				return this.getRight().findLarger(x, middleNode);
			}
		}

		void update(Key x, Value v) {
			if(x.compareTo(key) < 0)	//x < key
				left.update(x, v);
			else						//x >= key
				right.update(x, v);
		}
		
	}
	
	
	/*
	 * Class for all nodes on the leaf level. Contains the key and value
	 * of the structure. Make helper functions return self or do nothing to
	 * mimic nill.
	 */
	private class ExternalNode extends Node{
		Key key;
		Value value;
		
		//Constructor
		public ExternalNode (Key k, Value v) {
			key = k;
			value = v;
		}
		
		//Getters and setters
		Node getLeft() { return this;}
		void setLeft(Node n) { };
		Node getRight() { return this;}
		void setRight(Node n) { };
		int getLevel() { return 0;}
		void setLevel(int lvl) { }
		
		
		Value find (Key x) {
			if(x.compareTo(key) == 0)
				return value;
			else 
				return null;
		}
		
		//if not here make new external and internal and link external to internal
		Node insert(Key x, Value v) throws Exception { 
			if(x.compareTo(key) == 0)
				throw new Exception("Insertion of duplicate key");
			else {
				ExternalNode newExt = new ExternalNode(x, v);
				InternalNode newInt;
				
				if(x.compareTo(key) < 0) {
					newInt = new InternalNode(key, newExt, this, 1);
				}
				else {
					newInt = new InternalNode(x, this, newExt, 1);
				}
				size++;
				
				return newInt;
			}
				
		}
		
		Node skew() { return this; }
		Node split() { return this; }
		
		void getPreorderList(){
			preList.add("[" + key + " " + value + "]");
		}
		
		//if found return null to tell internal to change pointers
		Node delete(Key x) throws Exception{
			if(x.compareTo(key) == 0) {
				size--;
				return null;
			}
			
			throw new Exception("Deletion of nonexistent key");
		}

		void updateHeight() {}
		Node fixAfterDelete(Node p) {return p;}
		
		Value getMin() { return this.value; }
		Value getMax() { return this.value; }
		
		Key getMinKey() { return this.key; }
		Key getMaxKey() { return this.key; }
		
		
		//at node if its less then x then return this if not go to middleNode and 
		//find the max of the left child.
		Value findSmaller(Key x, Node middleNode) {
			if(middleNode != null) {
				if(x.compareTo(key) > 0)
					return this.value;
				else if (x.compareTo(key) < 0)
					return null;
				else
					return middleNode.getLeft().getMax();
			}
			
			return null;
		}
		
		//at node if less then x then return this if not go to middle and find
		//min of the right child.
		Value findLarger(Key x, Node middleNode) {
			if(middleNode != null) {
				if(x.compareTo(key) < 0)
					return this.value;
				//else if (x.compareTo(key) > 0)
				//	return null;
				else
					return middleNode.getRight().getMin();
			
			}
			
			return null;
		}

		void update(Key x, Value v) {
			if(x.compareTo(key) == 0)
				value = v;
		}
	}
	
	
	/*
	 * Constructor and main methods to call helper on the internal and external 
	 * nodes of the tree.
	 */
	public AAXTree() { 
		root = null;
		size = 0;
	}
	public Value find(Key k) { 
		if (root == null)
			return null; 
		else
			return root.find(k);		
	}
	
	public void insert(Key x, Value v) throws Exception { 
		if(root == null) {
			root = new ExternalNode(x, v);
			size++;
		}
		else
			root = root.insert(x, v);
	}
	
	public void clear() {
		root = null;
		size = 0;
	}
	
	public ArrayList<String> getPreorderList() { 
		
		preList = new ArrayList<String>();
		
		if (root != null)
			root.getPreorderList();
		
		return preList; 
	}

	
	
	//------------------------------------------------------------------
	// Needed for Part b
	//------------------------------------------------------------------
	public void delete(Key x) throws Exception { 
		if(root == null) {
			throw new Exception("Deletion of nonexistent key");
		}
		root = root.delete(x);
	}
	
	public int size() { 
		return size;
	}
	
	/*
	 * Check if tree is empty if not call its helper method on root.
	 */
	public Value getMin() { 
		if(root == null)
			return null;
		else
			return root.getMin();
	}
	
	public Value getMax() {
		if(root == null)
			return null;
		else
			return root.getMax();
	}
	
	public Value findSmaller(Key x) { 
		if(root == null) {
			return null;
		}
		return root.findSmaller(x, null); 
	}
	
	public Value findLarger(Key x) {
		
		if(root == null) {
			return null;
		}
		return root.findLarger(x, null); 
	}
	
	//Get min Value and call delete on it
	public Value removeMin() throws Exception {
		if(root == null)
			return null; 
		
		Value deleteValue = root.getMin();
		root  = root.delete(root.getMinKey());
		return deleteValue;
	}
	
	//Get max value and call delete on it
	public Value removeMax() throws Exception{ 
		if(root == null)
			return null; 
		
		Value deleteValue = root.getMax();
		root = root.delete(root.getMaxKey());
		return deleteValue;
	}
	
	public void update(Key k, Value v) {
		root.update(k, v);
	}

}
