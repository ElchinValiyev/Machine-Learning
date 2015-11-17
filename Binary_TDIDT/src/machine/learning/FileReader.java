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
		for (int i = 1; i < scanner.nextLine().split(",").length; i++) {
			Attribute attribute = new Attribute();
			attribute.setType(Type.binary);
			attribute.setId(i);
			attributes.add(attribute);
		}
		scanner = new Scanner(new File(dataSource));
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
