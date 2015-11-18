package machine.learning;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;
/** Class for simplifying file processing*/
public class FileReader {
	private Scanner scanner;
	private LinkedList<Attribute> attributes;
	private File dataSource;

	public FileReader(String dataSource) throws FileNotFoundException {
		attributes = new LinkedList<>();
		this.dataSource = new File(dataSource);
		scanner = new Scanner(this.dataSource);
		for (int i = 1; i < scanner.nextLine().split(",").length; i++) {
			Attribute attribute = new Attribute();
			attribute.setType(Type.binary);
			attribute.setId(i);
			attributes.add(attribute);
		}
		scanner = new Scanner(new File(dataSource));// resetting scanner
	}

	/** Get all training examples from file */
	LinkedList<String[]> getExamples() {
		LinkedList<String[]> examples = new LinkedList<>();
		while (scanner.hasNext())
			examples.add(scanner.nextLine().split(","));
		return examples;
	}

	/** Returns list of attributes for given dataset */
	public LinkedList<Attribute> getAttributes() {
		return this.attributes;
	}

	private int[] sequence;
	private RandomAccessFile file;
	private long lengthOfExample;

	public void prepareForExperiments() throws IOException {
		file = new RandomAccessFile(this.dataSource, "r");
		int count = 0;
		while (file.readLine() != null)
			count++;
		sequence = new int[count];

		for (int i = 0; i < count; i++) {
			sequence[i] = i;
		}
		lengthOfExample = file.length() / count+1;
	}

	/** Array shuffling algorithm */
	private void shuffleSequence(int[] array) {
		Random rnd = new Random();
		int temp;
		for (int i = array.length - 1; i > 0; i--) {
			int index = rnd.nextInt(i + 1);
			temp = array[index];
			array[index] = array[i];
			array[i] = temp;
		}
	}

	/**
	 * Returns specified percent of data for training, leaving the rest as test
	 * data
	 * 
	 * @throws IOException
	 */
	LinkedList<String[]> getShuffledExamples(int percent) throws IOException {
		shuffleSequence(sequence);
		LinkedList<String[]> trainExamples = new LinkedList<>();

		for (int i = 0; i < sequence.length * percent / 100; i++) {
			file.seek(lengthOfExample * sequence[i]);
			String str = file.readLine();
			trainExamples.add(str.split(","));
		}
		return trainExamples;
	}

	/**
	 * Returns specified percent of data for testing
	 * 
	 * @throws IOException
	 */
	LinkedList<String[]> getTestExamples(int percent) throws IOException {
		LinkedList<String[]> testExamples = new LinkedList<>();
		for (int i = sequence.length * percent / 100; i < sequence.length; i++) {
			file.seek(lengthOfExample * sequence[i]);
			testExamples.add(file.readLine().split(","));
		}
		return testExamples;
	}
}
