package machine.learning;

/** This class is used for storing number of positive and negative outcomes. */
public class Tuple {
	private String category;
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

	public Tuple(String booleanString, String category) {
		this(booleanString);
		this.category = category;
	}

	/**
	 * Adds two tuples.
	 * 
	 * @param other
	 *            tuple to be added
	 * @return new tuple, resulting from adding
	 */
	public Tuple add(Tuple other) {
		Tuple tuple = new Tuple();
		tuple.postitive = this.postitive + other.getPostitive();
		tuple.negative = this.negative + other.getNegative();
		return tuple;
	}

	/**
	 * Increase counter of positive or negative outcomes.
	 * 
	 * @param booleanString
	 *            string with values "yes" for positive outcome and any other
	 *            value for negative.
	 */
	public void parse(String booleanString) {
		if (booleanString.equals("yes"))
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

	public void increasePositive() {
		postitive++;
	};

	public void increaseNegative() {
		negative++;
	};

	public Tuple setCategory(String category) {
		this.category = category;
		return this;
	}

	public String getCategory() {
		return category;
	}

	@Override
	public String toString() {
		String category = this.category == null ? "" : this.category;
		return category + " +:" + postitive + " -:" + negative;
	}

	@Override
	public boolean equals(Object obj) {
		Tuple tuple = (Tuple) obj;
		return category.equals(tuple.category);
	}

}
