package machine.learning;

import java.io.FileNotFoundException;
import java.util.LinkedList;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

public class DecisionTreeBuilder {

    /**
     * Builds tree according TDIDT algorithm.
     *
     * @param examples   learning data
     * @param attributes properties of data pattern
     * @param tree       root node
     * @param node       current node
     */
    public Node buildTree(LinkedList<String[]> examples, LinkedList<Attribute> attributes, Node tree, Node node) {
        if (examples.isEmpty() || !canSplit(examples, node))
            return tree;
        Attribute bestAttribute = node.findBestSplit(examples, attributes);

        // if there is no attribute that can split examples
        if (bestAttribute == null) {
            node.setLeafNode(true);
            Tuple outcomes = new Tuple();
            examples.forEach(x -> outcomes.parse(x[x.length - 1]));
            int positive = outcomes.getPostitive();
            int negative = outcomes.getNegative();
            // take majority as outcome
            node.setTestValue(positive > negative ? "yes" : "no");
            return tree;
        }

        // System.err.println("Best attribute => "+bestAttribute.getId()+" :
        // "+bestAttribute.getType());
        // System.out.println("\n****************************************************************************");

        if (bestAttribute.getType() == Type.categorical) {
            attributes.remove(bestAttribute);
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
        Node negativeNode = new Node();
        node.setNegativeDescendant(negativeNode);
        if (canSplit(negativeExamples, negativeNode)) {
            LinkedList<Attribute> clone2 = new LinkedList<Attribute>();
            for (Attribute attribute : attributes) {
                try {
                    clone2.add((Attribute) attribute.clone());
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
            buildTree(negativeExamples, clone2, tree, negativeNode);
        }

        // going down positive branch
        Node positiveNode = new Node();
        node.setPositiveDescendant(positiveNode);
        if (canSplit(positiveExamples, positiveNode)) {

            LinkedList<Attribute> clone = new LinkedList<Attribute>();
            for (Attribute attribute : attributes) {
                try {
                    clone.add((Attribute) attribute.clone());
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
            buildTree(positiveExamples, clone, tree, positiveNode);
        }
        return tree;
    }

    /**
     * Checks whether examples can be splited and assigns value for leaf nodes
     */
    public boolean canSplit(LinkedList<String[]> examples, Node node) {
        // take first outcome to compare with others
        String[] firstExample = examples.getFirst();
        String firstOutcome = firstExample[firstExample.length - 1];
        for (String[] pattern : examples) {
            if (!pattern[pattern.length - 1].equals(firstOutcome))
                return true;
        }
        node.setLeafNode(true);
        node.setTestValue(firstOutcome);
        // System.err.println("Leaf Node ! "+ firstOutcome);
        return false;
    }

    /**
     * Draw graph of a decision tree using GraphStream library
     */
    public void drawTree(Node tree) {
        Graph graph = new SingleGraph("TDIDT");
        graph.addAttribute("ui.antialias");
        graph.addAttribute("ui.stylesheet",
                "graph {fill-color:#424242;} node { fill-color: #00E676; text-color:#00E676;} node#0 { fill-color: #FF1744; text-color: #FF1744;}");
        graph.setAutoCreate(true);
        addNodes(graph, tree, "0");

        graph.display();
    }

    /**
     * Adds node to the graph
     */
    private void addNodes(Graph graph, Node node, String str) {
        graph.addNode(str).setAttribute("ui.label", node.isLeafNode() ? node.getTestValue().toString()
                : node.getTestAttribute().getId() + ":" + node.getTestValue().toString());
        if (!node.isLeafNode()) {
            addNodes(graph, node.getNegativeDescendant(), "0" + str);
            graph.addEdge("e0" + str, str, "0" + str).setAttribute("ui.label", "no");

            addNodes(graph, node.getPositiveDescendant(), "1" + str);
            graph.addEdge("e1" + str, str, "1" + str).setAttribute("ui.label", "yes");
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        DecisionTreeBuilder builder = new DecisionTreeBuilder();
        FileReader fileReader = new FileReader("new_data.txt");
        // System.out.println(fileReader.getAttributes().toString());
        Node tree = new Node();
        builder.buildTree(fileReader.getExamples(), fileReader.getAttributes(), tree, tree);
        builder.drawTree(tree);
    }
}
