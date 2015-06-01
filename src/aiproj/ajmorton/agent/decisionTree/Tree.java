package aiproj.ajmorton.agent.decisionTree;

/** 
2  * COMP30024 Artificial Intelligence 
3  * Project B
4  * ajmorton Andrew Morton 522139  
5  * rhysw    Rhys Williams 661561 
6  */ 

import java.util.ArrayList;

/**
 * the decision tree object used for best next  move detection
 */
public class Tree<T> {
	private Root<T> root;
	
	/* CONSTRUCTOR */
	public Tree (T data) {
		root = new Root<T>(data);
	}
	
	/* GETTERS */
	public Root<T> getRoot() {return root;}

	/* SETTERS */
	public void setRoot(Node<T> newRoot) {this.root = new Root<T>(newRoot.getData());}
	
	
	/* SUBCLASSES */
	/**
	 * a node in the tree
	 */
	public static class Node<T> {
		private T data = null;					// metadata about the node in the game
		private Node<T> parent = null;			// the parent node of this node
		private ArrayList<Node<T>> children;	// a list of all children of this node
		
		/* CONSTRUCTOR */
		public Node(T data, Node<T> parent) {
			this.data = data;
			this.parent = parent;
			this.children = new ArrayList<Node<T>>();
		}
		
		/* GETTERS */
		public T getData() 						{return data;}
		public Node<T> getParent()  			{return parent;}
		public ArrayList<Node<T>> getChildren() {return children;}
		
		/* SETTERS */

		public void setData(T data) 	 {this.data = data;}
		public void setParent(Node<T> p) {this.parent = p;}
		
		
		/* METHODS */
		// inserts a node into the children list
		public boolean insert(T data) {
			Node<T> n = new Node<T>(data, this);
			this.children.add(n);
			return true;
		}
	
		// inserts a node into the children list
		public boolean insert(Node<T> node) {
			this.children.add(node);
			
			return true;
		}
		

		
		
		
		
	}
	/** 
	 * the root node class
	 * extends the node class
	 */
	public static class Root<T> extends Node<T> {
		
		public Root(T data) {
			super(data, null);
		}
		
	}
}
