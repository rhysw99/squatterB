package aiproj.agent.decisionTree;

import java.util.ArrayList;

public class Tree<T> {
	private Root<T> root;
	
	public Tree (T data, int playerID) {
		root = new Root<T>(data);
	}
	
	public Root<T> getRoot() {
		return root;
	}
	
	public static class Node<T> {
		private T data = null;
		private Node<T> parent = null;
		private ArrayList<Node<T>> children;
		private int score;						// the heuristic score value
		private int capDifference;				// number of captures of (player - opponent) 
		
		public Node(T data, Node<T> parent) {
			this.data = data;
			this.parent = parent;
			this.children = new ArrayList<Node<T>>();
			this.score = 0;
			this.capDifference = 0;
		}
		
		public void setScore(int score)	{this.score = score;}
		public int getScore(){return this.score;}
		
		public void setCapDifference(boolean playerCapture){
			if(playerCapture){
				capDifference += 1;
			}else{
				capDifference -=1;
			}
			return;
		}
		
		public int getCapDifference()	{return capDifference;}
		
		public boolean insert(T data) {
			Node<T> n = new Node<T>(data, this);
			this.children.add(n);
			
			return true;
		}
		
		public boolean insert(Node<T> node) {
			this.children.add(node);
			
			return true;
		}
		
		public T getData() {
			return data;
		}
		
		public Node<T> getParent() {
			return parent;
		}
		
		public ArrayList<Node<T>> getChildren() { 
			return children;
		}
		
		public void setData(T data) {
			this.data = data;
		}
		
		public void setParent(Node<T> p) {
			this.parent = p;
		}
		
		public void setChildren(ArrayList<Node<T>> children) {
			this.children = children;
		}
		
	}
	
	public static class Root<T> extends Node<T> {
		
		public Root(T data) {
			super(data, null);
		}
		
	}
}
