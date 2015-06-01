package aiproj.agent;

//TODO comment
/**
 * 
 */
public class ProbabilityCell implements Comparable<Object> {
	
	private float probability;
	private int x;
	private int y;
	
	/* CONSTRUCTOR */
	public ProbabilityCell(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/* GETTERS */
	public int getX() 				{return x;}
	public int getY() 				{return y;}
	public float getProbability() 	{return probability;}
	
	/* SETTERS */
	public void setProbability(float i) {this.probability = i;}
	
	/* METHODS */
	public void incrementProbability(float i) {
		this.probability += i;
	}

	@Override
	public int compareTo(Object o) {
		ProbabilityCell other = (ProbabilityCell) o;
		float oCell = other.getProbability();
		if (probability == oCell) {
			return 0;
		} else if (probability < oCell) {
			return -1;
		} else {
			return 1;
		}
	}

}
