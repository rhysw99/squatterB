package aiproj.agent.decisionTree;

import java.util.ArrayList;

public class Tree<T> {
	private Root<T> root;
	
	public Tree (byte[][] board) {
		root = new Root<T>(board);
	}
	
	public Root<T> getRoot() {
		return root;
	}
	
	public static class Node<T> {
		private T data = null;
		private Node<T> parent = null;
		private ArrayList<Node<T>> children;
		
		public Node(T data, Node<T> parent) {
			this.data = data;
			this.parent = parent;
			this.children = new ArrayList<Node<T>>();
		}
		
		public boolean insert(T data) {
			Node<T> n = new Node<T>(data, this);
			this.children.add(n);
			
			return true;
		}
		
		public T getData() {
			return data;
		}
		
		public Node<T> getParent() {
			return parent;
		}
		
	}
	
	public static class Root<T> extends Node<T> {
		private byte[][] board;
		
		public Root(byte[][] board) {
			super(null, null);
			this.board = board;
		}
	}
}
