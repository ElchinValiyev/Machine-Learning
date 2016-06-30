import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.function.Function;

public class Midos {
    private final double G_MIN = 0.2; //g_min value for pruning
    static int attributeCount; // number of attributes
    private double populationProbability;
    private int bestHypoCount; // number of best
    private Function<Hypo, Double> qualityFunction; //evaluates hypos
    private Function<Hypo, Double> estimateFunction; //Optimistic Estimate function

    public static void main(String[] args) throws Exception {
        Midos midos = new Midos();
        midos.runAlgorithm("SPECT_union.txt").forEach(System.out::println);
    }

    public PriorityQueue<Hypo> runAlgorithm(String fileName) throws FileNotFoundException {
        int[][] data = getDataFromFile(fileName);
        PriorityQueue<Hypo> solutions = new PriorityQueue<>(); //set of solutions
        LinkedList<Hypo> hypoQueue = new LinkedList<>(); // set of hypotheses
        hypoQueue.add(new Hypo(0)); //adding initial hypothesis ("entire population")

        while (!hypoQueue.isEmpty()) { // while not checked hypo exists
            LinkedList<Hypo> children = hypoQueue.removeFirst().getRefinedHypos(); //refine
            children.forEach(hypo -> { // loop through children
                hypo.evaluate(data, populationProbability); //calculate g and p_0
                hypo.setQuality(this.qualityFunction.apply(hypo)); // calculate quality of hypo
                if (solutions.size() < this.bestHypoCount)  // not enough solutions
                    solutions.add(hypo);
                else {
                    if (hypo.getQuality() > solutions.peek().getQuality()) { //better that existing worst solution
                        solutions.poll();
                        solutions.add(hypo);
                    }
                }
                //Pruning with optimistic estimates
                if (!(estimateFunction.apply(hypo) < solutions.peek().getQuality() || hypo.getG() < this.G_MIN))
                    hypoQueue.add(hypo);
            });
        }
        return solutions;
    }

    private int[][] getDataFromFile(String fileName) throws FileNotFoundException {

        Scanner fileReader = new Scanner(new File(fileName));
        String[] parameters = fileReader.nextLine().split(",");
        int populationSize = Integer.parseInt(parameters[0]); // number of samples
        Midos.attributeCount = Integer.parseInt(parameters[1]); //number of attributes
        this.bestHypoCount = Integer.parseInt(parameters[2]); //number of k-best hypos
        this.setQualityAndEstimationFunctions(Integer.parseInt(parameters[3]));

        int[][] data = new int[populationSize][];

        for (int i = 0; fileReader.hasNext(); i++) {
            String[] values = fileReader.nextLine().split(",");
            data[i] = new int[values.length];

            for (int j = 0; j < values.length; j++)
                data[i][j] = Integer.parseInt(values[j]);
        }

        double positives = 0;
        for (int[] sample : data) {
            if (sample[attributeCount] == 1) //checking target
                positives++;
        }
        populationProbability = positives / populationSize; //probability of positive samples
        return data;
    }

    private void setQualityAndEstimationFunctions(int F) {
        switch (F) {
            case 1:
                qualityFunction = hypo -> Math.sqrt(hypo.getG()) * Math.abs(this.populationProbability
                        - hypo.getProbability());
                break;
            case 2:
                qualityFunction = hypo ->
                        (hypo.getG() / (1 - hypo.getG())) *
                                Math.pow((this.populationProbability - hypo.getProbability()), 2);
                break;
            case 3:
                qualityFunction = hypo -> hypo.getG() *
                        (2 * hypo.getProbability() - 1) + 1 - this.populationProbability;
                break;
            default:
                throw new IllegalArgumentException("No such  quality function!");
        }
        estimateFunction = hypo -> Math.sqrt(hypo.getG()) *
                Math.max(this.populationProbability, 1 - this.populationProbability);
    }
}

class Hypo implements Comparable<Hypo> {
    private final double Z_SCORE = 2.58; //level of significance alpha=0.001
    private int[] attributeIndexes; // attributes that hypothesis checks
    private int size; //number of literals in conjucnction
    private double g;
    private double probability;
    private double quality;
    private boolean isSignificant;

    Hypo(int size) {
        this.size = size;
        this.attributeIndexes = new int[size];
    }
    /** Calculates g, significanse and probability of positives in sample*/
    void evaluate(int[][] data, double populationProbability) {
        int size = data[0].length;
        double positives = 0; // sample that satisfy and have label 1
        int extensionSize = 0; // number of satisfying samples
        for (int[] sample : data) {
            if (satisfies(sample)) {
                extensionSize++;
                if (sample[size - 1] == 1)
                    positives++;
            }
        }
        this.g = 1.0 * extensionSize / data.length;
        this.probability = (extensionSize == 0) ? 0 : positives / extensionSize;
        this.isSignificant = Z_SCORE < Math.abs((this.probability - populationProbability) /
                Math.sqrt(populationProbability * (1 - populationProbability) / extensionSize));
    }

    private boolean satisfies(int[] sample) {
        for (int attribute : this.attributeIndexes)
            if (sample[attribute] != 1) // sample attribute matches
                return false;
        return true;
    }

    LinkedList<Hypo> getRefinedHypos() {
        LinkedList<Hypo> refinedHypos = new LinkedList<>();

        int start = size > 0 ? this.attributeIndexes[size - 1] + 1 : 0;
        for (int i = start; i < Midos.attributeCount; i++) {
            Hypo newHypo = new Hypo(size + 1);
            newHypo.attributeIndexes = Arrays.copyOf(this.attributeIndexes, size + 1);
            newHypo.attributeIndexes[size] = i;
            refinedHypos.add(newHypo);
        }
        return refinedHypos;
    }

    void setQuality(double quality) {
        this.quality = quality;
    }

    double getQuality() {
        return quality;
    }

    double getG() {
        return g;
    }

    double getProbability() {
        return probability;
    }

    @Override
    public int compareTo(Hypo o) {
        return Double.compare(this.quality, o.quality);
    }

    @Override
    public String toString() {
        int[] temp = new int[Midos.attributeCount + 1];
        for (int index : attributeIndexes)
            temp[index] = 1;
        return Arrays.toString(temp) + " Quality: " + quality + " Significant: " + isSignificant;
    }
}
