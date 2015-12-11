package machine.learning;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;

public class FileReader {
	private Scanner scanner;
	private LinkedList<Attribute> attributes;

	public FileReader(String dataSource) throws FileNotFoundException {
		attributes = new LinkedList<>();
		scanner = new Scanner(new File(dataSource));
		for (String attributeString : scanner.nextLine().split(",")) {
			Attribute attribute = new Attribute();
			String[] values = attributeString.split(":");
			switch (values[1]) {
			case "n":
				attribute.setType(Type.numerical);
				break;
			case "b":
				attribute.setType(Type.binary);
				break;
			case "c":
				attribute.setType(Type.categorical);
				break;
			default:
				return;
			}
			attribute.setId(values[0].charAt(0) - 97);
			attributes.add(attribute);
		}
	}

	LinkedList<String[]> getExamples() {
		LinkedList<String[]> examples = new LinkedList<>();
		while (scanner.hasNext())
			examples.add(scanner.nextLine().split(","));
		return examples;

	}

	public LinkedList<Attribute> getAttributes() {
		return this.attributes;
	}
}
