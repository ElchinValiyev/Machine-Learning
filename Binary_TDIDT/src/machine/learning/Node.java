package machine.learning;

import java.util.List;

public class Node {
	private final int OUTCOME_INDEX = 0; // index of result in example arrays

	/** Node number. If even- positive descendant, odd - negative */
	private final int id;
	private Attribute testAttribute; // node's attribute
	private String testValue;// value to compare with during test
	private boolean isLeafNode = false;

	private Node positiveDescendant;// a.k.a. left branch
	private Node negativeDescendant;// a.k.a. right branch

	public Node(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(id).append(" ").append((id % 2 == 0) ? "yes" : "no").append(" ").append(testAttribute.getId());
		if (!isLeafNode)
			sb.append(" ").append(this.positiveDescendant.getId()).append(" ").append(this.negativeDescendant.getId());
		return sb.toString();
	}

	/** Get decision based on learning data */
	public String getDecision(String[] pattern) {
		if (isLeafNode) // return answer if leaf node reached
			return testValue;
		if (test(pattern))
			return positiveDescendant.getDecision(pattern);
		else
			return negativeDescendant.getDecision(pattern);
	}

	/** Checks which branch pattern belongs to */
	public boolean test(String[] example) {
		return example[testAttribute.getId()].equals(testValue);
	}

	/**
	 * Finds best decision test/attribute for Node. Sets value to compare with.
	 * 
	 * @param examples
	 *            patterns for tree induction
	 * @param attributes
	 *            list of available attributes for test
	 * @return best attribute
	 */
	public Attribute findBestSplitAttribute(List<String[]> examples, List<Attribute> attributes) {
		/** no comment */
		Attribute bestAttribute = null;
		/** Lowest possible entropy among all attributes */
		double minEntropy = Double.POSITIVE_INFINITY;

		for (Attribute attribute : attributes) {

			int attributeId = attribute.getId();
			Tuple leftBranch = new Tuple(), rightBranch = new Tuple();
			// separating positive and negative attribute values
			for (String[] pattern : examples) {
				if (pattern[attributeId].equals("1")) {
					// positive
					leftBranch.parse(pattern[OUTCOME_INDEX]);
				} else {
					// negative
					rightBranch.parse(pattern[OUTCOME_INDEX]);
				}
			}
			double currentEntropy = calculateEntropy(leftBranch.getPostitive(), leftBranch.getNegative(),
					rightBranch.getPostitive(), rightBranch.getNegative());
			if (!Double.isNaN(currentEntropy))
				System.out.println(" <= Attribute:" + attributeId);

			// Searching for the best attribute using sum of entropies
			if (currentEntropy < minEntropy) {
				minEntropy = currentEntropy;
				bestAttribute = attribute;
				testValue = "1";
			}
		}
		setTestAttribute(bestAttribute);
		return testAttribute;
	}

	private double calculateEntropy(double leftPos, double leftNeg, double rightPos, double rightNeg) {
		double leftTotal = leftNeg + leftPos;
		double rightTotal = rightNeg + rightPos;
		double total = leftTotal+rightTotal;
		
		/** Entropy for the whole split */
		double generalEntropy = -((leftPos + rightPos) / total) * log2((leftPos + rightPos) / total)
				- ((leftNeg + rightNeg) / total) * log2((leftNeg + rightNeg) / total);

		/** Entropy for left(positive branch) */
		double result = -(leftTotal / total)
				* ((leftPos / leftTotal) * log2(leftPos / leftTotal)
						+ (leftNeg / leftTotal) * log2(leftNeg / leftTotal))
				/** Entropy for right(negative) branch */
				- (rightTotal / total) * ((rightPos / rightTotal) * log2(rightPos / rightTotal)
						+ (rightNeg / rightTotal) * log2(rightNeg / rightTotal));

		if (!Double.isNaN(result)) // if attribute can split the data
			System.out.print("Gain=" + generalEntropy + " - " + result + " = " + (generalEntropy - result));
		return result;
	}

	/**
	 * Logarithm base 2 from (1/x). In case x=0 returns 0.
	 * 
	 * @param number
	 *            argument
	 */
	private double log2(double number) {
		if (number == 0)
			return 0;
		return Math.log((number)) / Math.log(2);
	}

	public void setTestAttribute(Attribute testAttribute) {
		this.testAttribute = testAttribute;
	}

	public Attribute getTestAttribute() {
		return testAttribute;
	}

	public void setLeafNode(boolean isLeafNode) {
		this.isLeafNode = isLeafNode;
	}

	public int getId() {
		return id;
	}

	public void setNegativeDescendant(Node negativeDescendant) {
		this.negativeDescendant = negativeDescendant;
	}

	public void setPositiveDescendant(Node positiveDescendant) {
		this.positiveDescendant = positiveDescendant;
	}

	public Node getNegativeDescendant() {
		return negativeDescendant;
	}

	public Node getPositiveDescendant() {
		return positiveDescendant;
	}

	public boolean isLeafNode() {
		return isLeafNode;
	}

	public void setTestValue(String testValue) {
		this.testValue = testValue;
	}

	public Object getTestValue() {
		return testValue;
	}
}

/** This class is used for storing number of positive and negative outcomes. */
class Tuple {
	private int postitive;
	private int negative;

	public Tuple() {
		super();
		postitive = 0;
		negative = 0;
	}

	public Tuple(String booleanString) {
		this();
		parse(booleanString);
	}

	/**
	 * Increase counter of positive or negative outcomes.
	 * 
	 * @param booleanString
	 *            string with values "1" for positive outcome and any other
	 *            value for negative.
	 */
	public void parse(String booleanString) {
		if (booleanString.equals("1"))
			postitive++;
		else
			negative++;
	}

	public int getNegative() {
		return negative;
	}

	public int getPostitive() {
		return postitive;
	}
}
