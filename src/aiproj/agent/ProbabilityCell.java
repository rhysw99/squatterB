package aiproj.agent;

public class ProbabilityCell implements Comparable {
	
	private float probability;
	private int x;
	private int y;
	
	public ProbabilityCell(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public float getProbability() {
		return probability;
	}

	public void setProbability(float i) {
		this.probability = i;	
	}
	
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
