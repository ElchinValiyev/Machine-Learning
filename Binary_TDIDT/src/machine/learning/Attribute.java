package machine.learning;

public class Attribute {
	private int id;

	public void setId(int i) {
		this.id = i;
	}

	public int getId() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		return this.id == ((Attribute) obj).id;
	}

	@Override
	public String toString() {
		return "Attribute:" + id;
	}
}
