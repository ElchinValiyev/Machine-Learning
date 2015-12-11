package machine.learning;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public class Node {

	private Attribute testAttribute;
	private Object testValue;// value to compare with during test
	private boolean isLeafNode = false;

	private Node positiveDescendant;// a.k.a. left branch
	private Node negativeDescendant;// a.k.a. right branch

	/** Get decision based on learning data */
	public Object getDecision(String[] pattern) {
		if (isLeafNode) // return answer if leaf node reached
			return testValue;
		if (test(pattern))
			return positiveDescendant.getDecision(pattern);
		else
			return negativeDescendant.getDecision(pattern);
	}

	/** Checks which branch pattern belongs to */
	public boolean test(String[] example) {
		String exampleValue = example[testAttribute.getId()];
		switch (testAttribute.getType()) {
		case numerical:
			Double doubleValue = Double.parseDouble(exampleValue);
			return !(((Double) testValue).compareTo(doubleValue) > 0);
		case categorical:
			return ((LinkedList<String>) testValue).contains(exampleValue);
		case binary:
			return example[testAttribute.getId()].equals("yes");
		}
		throw new IllegalArgumentException();
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
	public Attribute findBestSplit(List<String[]> examples, List<Attribute> attributes) {
		/** no comment */
		Attribute bestAttribute = null;
		/** Lowest possible entropy among all attributes */
		double minEntropy = Double.POSITIVE_INFINITY;

		for (Attribute attribute : attributes) {
			// System.err.println(attribute.getType());
			int attributeId = attribute.getId();

			switch (attribute.getType()) {
			/************************************************************
			 * ***************** BINARY ATTRIBUTE ***********************
			 ************************************************************/
			case binary:
				Tuple leftBranch = new Tuple(), rightBranch = new Tuple();
				// separating positive and negative attribute values
				for (String[] pattern : examples) {
					if (pattern[attributeId].equals("yes")) {
						leftBranch.parse(pattern[pattern.length - 1]);
					} else {
						rightBranch.parse(pattern[pattern.length - 1]);
					}
				}
				double currentEntropy = calculateEntropy(leftBranch.getPostitive(), leftBranch.getNegative(),
						rightBranch.getPostitive(), rightBranch.getNegative());

				if (currentEntropy < minEntropy) {
					minEntropy = currentEntropy;
					bestAttribute = attribute;
					testValue = true;
				}
				break;
			/******************************************************************
			 **************** CATEGORICAL ATTRIBUTE ***************************
			 ******************************************************************/
			case categorical:
				// map to save corresponding positive an negative result for
				// each attribute value
				HashMap<String, Tuple> data = new HashMap<>();
				for (String[] pattern : examples) {

					String patternResult = pattern[pattern.length - 1];
					String key = pattern[attributeId];

					if (!data.containsKey(key)) {
						data.put(key, new Tuple(patternResult, key));
					} else {
						Tuple tuple = data.get(key);
						tuple.parse(patternResult);
					}
				}

				LinkedList<Tuple> initialSet = new LinkedList<>(data.values());
				LinkedList<Tuple> newSet = new LinkedList<>();
				data = null;

				double bestCategoricalEntropy = Double.POSITIVE_INFINITY;
				int initialSetSize = initialSet.size();
				while (true) {
					Tuple element = null;
					Tuple bestElement = null;
					for (int i = 0; i < initialSet.size(); i++) {
						if ((initialSetSize / 2) <= newSet.size())
							break;
						element = initialSet.pollFirst();
						newSet.addLast(element);

						Tuple right = initialSet.parallelStream().reduce((t1, t2) -> t1.add(t2)).get();
						Tuple left = newSet.parallelStream().reduce((t1, t2) -> t1.add(t2)).get();

						double entropy = calculateEntropy(left.getPostitive(), left.getNegative(), right.getPostitive(),
								right.getNegative());
						// System.err.println(newSet + "\n");
						if (entropy < bestCategoricalEntropy) {
							bestCategoricalEntropy = entropy;
							bestElement = element;
						}
						initialSet.addLast(newSet.pollLast());

					}
					if (bestElement == null)
						break;
					else {
						if (!initialSet.remove(bestElement))
							throw new NoSuchElementException();
						newSet.add(bestElement);
					}
				}

				if (bestCategoricalEntropy < minEntropy) {
					minEntropy = bestCategoricalEntropy;
					bestAttribute = attribute;

					LinkedList<String> test = new LinkedList<>();
					newSet.forEach(tuple -> test.add(tuple.getCategory()));
					testValue = test;
				}
				break;
			case numerical:
				/***********************************************************************
				 ******************** NUMERICAL ATTRIBUTE ******************************
				 ***********************************************************************/
				/** attribute values */
				Integer[] keys = new Integer[examples.size()];
				/** corresponding outcomes */
				String[] values = new String[examples.size()];

				/**
				 * Counting positive and negative outcomes for current attribute
				 */
				int index = 0;
				Tuple total = new Tuple();
				for (String[] x : examples) {
					String outcome = x[x.length - 1];
					total.parse(outcome);
					keys[index] = Integer.parseInt(x[attributeId]);
					values[index++] = outcome;
				}

				/**
				 * Used to imitate double ordered map functionality, since we
				 * can have repeated keys, we have to maintain order also among
				 * values to have correct splits.
				 */
				Integer[] idx = new Integer[keys.length];
				for (int i = 0; i < keys.length; i++)
					idx[i] = i;
				Arrays.sort(idx, new Comparator<Integer>() {
					@Override
					public int compare(Integer o1, Integer o2) {
						int res = Integer.compare(keys[o1], keys[o2]);
						return res == 0 ? values[o1].compareTo(values[o2]) : res;
					}
				});

				Tuple leftSideOfSplit = new Tuple();
				double curEntropy = Double.POSITIVE_INFINITY;
				for (int i = 0; i < keys.length - 1; i++) {
					String result = values[idx[i]];

					leftSideOfSplit.parse(result);

					if (!result.equals(values[idx[i + 1]]) && keys[idx[i]] != keys[idx[i + 1]]) {

						curEntropy = calculateEntropy(leftSideOfSplit.getPostitive(), leftSideOfSplit.getNegative(),
								total.getPostitive() - leftSideOfSplit.getPostitive(),
								total.getNegative() - leftSideOfSplit.getNegative());
						// System.err.println((keys[idx[i]] + keys[idx[i + 1]])
						// / 2.0 + "\n");
					}

					if (curEntropy < minEntropy) {
						minEntropy = curEntropy;
						bestAttribute = attribute;
						testValue = (keys[idx[i]] + keys[idx[i + 1]]) / 2.0;
					}
				}
				break;
			default:
				throw new IllegalArgumentException("Wrong attribute type");
			}
		}
		setTestAttribute(bestAttribute);
		return testAttribute;
	}

	private double calculateEntropy(double leftPos, double leftNeg, double rightPos, double rightNeg) {
		double total = leftNeg + leftPos + rightNeg + rightPos;
		double leftTotal = leftNeg + leftPos;
		double rightTotal = rightNeg + rightPos;

		/** Entropy for the whole split */
		// double generalEntropy = ((leftPos + rightPos) / total) *
		// inverseLog2((leftPos + rightPos) / total)
		// + ((leftNeg + rightNeg) / total) * inverseLog2((leftNeg + rightNeg) /
		// total);

		/** Entropy for left(positive branch) */
		double result = (leftTotal / total)
				* ((leftPos / leftTotal) * inverseLog2(leftPos / leftTotal)
						+ (leftNeg / leftTotal) * inverseLog2(leftNeg / leftTotal))
				/** Entropy for right(negative) branch */
				+ (rightTotal / total) * ((rightPos / rightTotal) * inverseLog2(rightPos / rightTotal)
						+ (rightNeg / rightTotal) * inverseLog2(rightNeg / rightTotal));

		// System.out.println("H(S) = " + "-\\frac{" + (leftPos + rightPos) +
		// "}{ " + total + "} \\cdot \\log_2(\\frac{"
		// + (leftPos + rightPos) + "}{ " + total + "}" + ")- (\\frac{" +
		// (leftNeg + rightNeg) + "}{" + total
		// + "})\\cdot \\log_2(\\frac{" + (leftNeg + rightNeg) + "}{" + total +
		// "}) =" + generalEntropy);
		//
		// System.out.println("Entropy = \\frac{" + leftTotal + "}{" + total +
		// "}" + "\\cdot (-\\frac{" + leftPos + "}{"
		// + leftTotal + "} \\cdot log_2(\\frac{" + leftPos + "}{ " + leftTotal
		// + "}) - \\frac{" + leftNeg + "}{" + leftTotal
		// + "} \\cdot log_2(\\frac{" + leftNeg + "}{" + leftTotal + "}))+
		// \\frac{" + rightTotal + "}{" + total + "}\\cdot (-\\frac{"
		// + rightPos + "}{" + rightTotal + "}\\cdot log_2(\\frac{" + rightPos +
		// "}{ " + rightTotal + "})- \\frac{" + rightNeg
		// + " }{ " + rightTotal + "} \\cdot log_2(\\frac{" + rightNeg + "}{" +
		// rightTotal + "})) = " + result);
		//
		// System.out.println("Gain = " + (generalEntropy - result));
		return result;
	}

	/**
	 * Logarithm base 2 from (1/x). In case x=0 returns 0.
	 * 
	 * @param number
	 *            argument
	 */
	private double inverseLog2(double number) {
		if (number == 0)
			return 0;
		return Math.log(1.0 / (number)) / Math.log(2);
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

	public void setTestValue(Object testValue) {
		this.testValue = testValue;
	}

	public Object getTestValue() {
		return testValue;
	}
}
