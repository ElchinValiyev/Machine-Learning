package machine.learning;
//stupid change
enum Type {
	binary, categorical, numerical
}

public class Attribute {
	private int id;
	private Type type;

	public void setId(int i) {
		this.id = i;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public int getId() {
		return id;
	}

	public Type getType() {
		return type;
	}

	@Override
	public boolean equals(Object obj) {
		return this.id == ((Attribute) obj).id;
	}

	@Override
	public String toString() {
		return id + ":" + type;
	}
}
