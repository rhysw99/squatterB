package aiproj.agent.decisionTree;

import java.util.ArrayList;
import java.util.HashMap;

import aiproj.agent.board.Board;

public class Tree<T> {
	private Root<T> root;
	
	public Tree (T data, Board board) {
		root = new Root<T>(data, board);
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
		private Board board;
		
		public Root(T data, Board board) {
			super(data, null);
			this.board = board;
		}
		
		public void setBoard(Board board) {
			this.board = board;
		}
	}
}
