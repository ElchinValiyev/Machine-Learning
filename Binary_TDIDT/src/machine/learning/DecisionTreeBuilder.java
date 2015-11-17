package machine.learning;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.LinkedList;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

public class DecisionTreeBuilder {
	private final int OUTCOME_INDEX = 0;

	/**
	 * Builds tree according TDIDT algorithm.
	 * 
	 * @param examples
	 *            learning data
	 * @param attributes
	 *            properties of data pattern
	 * @param tree
	 *            root node
	 * @param node
	 *            current node
	 */
	public Node buildTree(LinkedList<String[]> examples, LinkedList<Attribute> attributes, Node tree, Node node) {
		if (examples.isEmpty() || !canSplit(examples, node))
			return tree;
		Attribute bestAttribute = node.findBestSplitAttribute(examples, attributes);

		// if there is no attribute that can split examples
		if (bestAttribute == null) {

			for (String example[] : examples) {
				System.out.println(Arrays.toString(example));
			}

			node.setLeafNode(true);
			Tuple outcomes = new Tuple();
			examples.forEach(x -> outcomes.parse(x[OUTCOME_INDEX]));
			int positive = outcomes.getPostitive();
			int negative = outcomes.getNegative();
			// take majority as outcome
			node.setTestValue(positive > negative ? "1" : "0");
			return tree;
		}

		LinkedList<String[]> positiveExamples = new LinkedList<>();
		LinkedList<String[]> negativeExamples = new LinkedList<>();

		for (String[] pattern : examples) {
			if (node.test(pattern))
				positiveExamples.add(pattern);
			else
				negativeExamples.add(pattern);
		}
		examples = null;

		// going down negative branch
		Node negativeNode = new Node(node.getId() * 2 + 1);
		node.setNegativeDescendant(negativeNode);
		buildTree(negativeExamples, attributes, tree, negativeNode);

		// going down positive branch
		Node positiveNode = new Node(node.getId() * 2);
		node.setPositiveDescendant(positiveNode);
		buildTree(positiveExamples, attributes, tree, positiveNode);
		return tree;
	}

	/**
	 * Checks whether examples can be splited and assigns value for leaf nodes
	 */
	public boolean canSplit(LinkedList<String[]> examples, Node node) {
		// take first outcome to compare with others
		String[] firstExample = examples.getFirst();
		String firstOutcome = firstExample[OUTCOME_INDEX];
		for (String[] pattern : examples) {
			if (!pattern[OUTCOME_INDEX].equals(firstOutcome))
				return true;
		}
		node.setLeafNode(true);
		node.setTestValue(firstOutcome);
		// System.err.println("Leaf Node ! "+ firstOutcome);
		return false;
	}

	/**
	 * Prints the structure of tree in ASCII in form of quadruples:
	 * "id isLeftBranch attributeID leftChildID rigthChildID", example: "5 yes 14 10 11"
	 * @param node - the root of a tree or subtree
	 */
	public void ASCIITree(Node node) {
		if (node.isLeafNode())
			return;
		System.out.println(node);
		ASCIITree(node.getPositiveDescendant());
		ASCIITree(node.getNegativeDescendant());
	}

	/** Draw graph of a decision tree using GraphStream library */
	public void drawTree(Node tree) {
		Graph graph = new SingleGraph("TDIDT");
		graph.addAttribute("ui.antialias");
		graph.setStrict(false);
		graph.addAttribute("ui.stylesheet",
				"graph {fill-color:#424242;} " + "node { fill-color: #00E676; text-color: #00E676;}"
						+ " node#0 { fill-color: #FF1744; text-color: #FF1744;}");
		graph.setAutoCreate(true);
		addNodes(graph, tree, "0");

		graph.display();
	}

	/** Adds node to the graph */
	private void addNodes(Graph graph, Node node, String str) {
		graph.addNode(str).setAttribute("ui.label",
				node.isLeafNode() ? node.getTestValue().toString() : "A:" + node.getTestAttribute().getId());
		if (!node.isLeafNode()) {
			addNodes(graph, node.getNegativeDescendant(), "0" + str);
			graph.addEdge("e0" + str, str, "0" + str).setAttribute("ui.label", "n");

			addNodes(graph, node.getPositiveDescendant(), "1" + str);
			graph.addEdge("e1" + str, str, "1" + str).setAttribute("ui.label", "y");
		}
	}

	public static void main(String[] args) throws FileNotFoundException {
		DecisionTreeBuilder builder = new DecisionTreeBuilder();
		FileReader fileReader = new FileReader("SPECT.test.txt");
		System.out.println(fileReader.getAttributes().toString());
		Node tree = new Node(1);
		builder.buildTree(fileReader.getExamples(), fileReader.getAttributes(), tree, tree);
		builder.drawTree(tree);
		builder.ASCIITree(tree);
	}
}
