import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class KMeans {
    private Random random;
    private int distanceChoice; //distance to be used

    public KMeans(int distance) {
        this.random = new Random();
        this.distanceChoice = distance;
    }

    /**
     * Assigns each point to the closes center
     */
    private int[] EStep(double[][] data, double[][] centers) {
        int[] assignments = new int[data.length];
        int closest;
        double minDist, currentDist;
        for (int i = 0; i < data.length; i++) {
            closest = 0;
            minDist = Double.MAX_VALUE;
            for (int j = 0; j < centers.length; j++) {
                currentDist = distance(centers[j], data[i]);
                if (currentDist < minDist) {
                    minDist = currentDist;
                    closest = j;
                }
            }
            assignments[i] = closest;
        }
        return assignments;
    }

    /**
     * Calculates and return new centers
     */
    private double[][] MStep(double[][] data, int[] assignments, int k) {
        double[][] newCenters = new double[k][data[0].length];
        int[] count = new int[k];
        int closest;
        for (int i = 0; i < data.length; i++) {
            closest = assignments[i];
            count[closest]++;
            for (int j = 0; j < newCenters[0].length; j++)
                newCenters[closest][j] += data[i][j];
        }

        for (int i = 0; i < newCenters.length; i++)
            for (int j = 0; j < newCenters[0].length; j++)
                newCenters[i][j] = newCenters[i][j] / count[i];
        return newCenters;
    }

    //Main loop
    public double[][] run(double[][] data, int k) {
        if (k < 0 || k > data.length)
            throw new IllegalArgumentException("Number of clusters should be >=1 and >= number of samples");
        double[][] centers = initializeCenters(data, k); // initializing centers
        double[][] oldCenters;
        do {
            oldCenters = centers;
            int[] assignments = EStep(data, oldCenters); // finding closest center
            centers = MStep(data, assignments, k);  // calculating new centers
        } while (!equal(oldCenters, centers)); // checking if new centers and old centers are different
        return centers;
    }

    /**
     * Generate samples at random according to the Gaussian with given standard deviation
     */
    private double[][] generateSamples(double[][] centers, int numberOfSamples, double standardDeviation) {
        int dimensions = centers[0].length;
        double[][] samples = new double[numberOfSamples][dimensions];
        for (int i = 0; i < numberOfSamples; i++) {
            double[] randomCenter = centers[random.nextInt(centers.length)]; // uniformly choose one of centers
            for (int j = 0; j < dimensions; j++) {
                //calculating coordinates of sample
                samples[i][j] = random.nextGaussian() * standardDeviation + randomCenter[j];
            }
        }
        return samples;
    }

    private void test() {
        int dimensions = 2;
        int numberOfCenters = 3;
        int intervalSize = 10;
        int numberOfSamples = 200;
        double standardDeviation = random.nextDouble();

        //Generating random centers
        double[][] centers = new double[numberOfCenters][dimensions];
        for (int i = 0; i < numberOfCenters; i++)
            for (int j = 0; j < dimensions; j++)
                centers[i][j] = random.nextInt(intervalSize);

        double[][] samples = generateSamples(centers, numberOfSamples, standardDeviation);
        double[][] calculatedCenters = run(samples, numberOfCenters);

        System.out.println("Standard deviation: " + standardDeviation);
        System.out.println("Initial centers:");

        printSamples(centers);

        System.out.println("Calculated centers:");

        printSamples(calculatedCenters);
    }

    /**
     * Distance function is chosen during object initialization.
     * 0 - Euclidean, 1 - Manhattan
     */
    private double distance(double[] p1, double[] p2) {
        switch (this.distanceChoice) {
            case 0:
                return euclideanDistance(p1, p2);
            case 1:
                return manhattanDistance(p1, p2);
            default:
                throw new IllegalArgumentException("No such function!");
        }
    }

    private double manhattanDistance(double[] p1, double[] p2) {
        double sum = 0;
        for (int i = 0; i < p1.length; i++) {
            sum += Math.abs(p1[i] - p2[i]);
        }
        return sum;
    }

    private double euclideanDistance(double[] p1, double[] p2) {
        double sum = 0;
        for (int i = 0; i < p1.length; i++) {
            sum += (p1[i] - p2[i]) * (p1[i] - p2[i]);
        }
        return Math.sqrt(sum);
    }

    /**
     * Checking if 2d arrays are equal
     */
    private boolean equal(double[][] p1, double[][] p2) {
        if (p1.length != p2.length) throw new IllegalArgumentException("Number of centers should be equal");
        for (int i = 0; i < p1.length; i++)
            for (int j = 0; j < p1[0].length; j++)
                if (Double.compare(p1[i][j], p2[i][j]) != 0)
                    return false;
        return true;
    }

    /**
     * Initializes centers. Picks every data.length / k element from given data
     */
    private double[][] initializeCenters(double[][] data, int k) {
        double[][] centers = new double[k][];
        int chunkSize = data.length / k;

        for (int i = 0; i < centers.length; i++)
            centers[i] = data[i * chunkSize];
        return centers;
    }

    /**
     * Outputs 2d array to the console
     */
    private void printSamples(double[][] data) {
        for (double[] sample : data)
            System.out.println(Arrays.toString(sample));
    }

    /**
     * Reads ans returns n-1 columns from file
     */
    private double[][] readIrisData(String fileName, int numberOfLines) throws FileNotFoundException {

        Scanner sc = new Scanner(new File(fileName));
        double[][] data = new double[numberOfLines][];
        int k = 0;
        while (sc.hasNext()) {

            String[] attributes = sc.nextLine().split(",");
            double[] sample = new double[attributes.length - 1];
            for (int i = 0; i < sample.length; i++)
                sample[i] = Double.parseDouble(attributes[i]);
            data[k++] = sample;
        }
        sc.close();
        return data;
    }

    /**
     * Reads n-th column from file
     */
    private String[] readIrisLabels(String fileName) throws FileNotFoundException {
        ArrayList<String> labels = new ArrayList<>();
        Scanner sc = new Scanner(new File(fileName));
        while (sc.hasNext()) {
            String[] sample = sc.nextLine().split(",");
            labels.add(sample[sample.length - 1]);
        }
        sc.close();
        return labels.toArray(new String[0]);
    }

    private double computePurity(double[][] data, double[][] centers, String[] labels) {
        if (data.length != labels.length)
            throw new IllegalArgumentException("Number of samples and labels are different");
        int N = data.length;
        int[] assignments = EStep(data, centers); //calculating closest centers

        Map<String, Integer> uniqueLabels = new HashMap<>(); // will hold label IDs
        int k = 0;
        int[][] freq = new int[centers.length][labels.length]; // frequency matrix
        for (int i = 0; i < assignments.length; i++) {
            String label = labels[i];
            int closest = assignments[i];
            if (!uniqueLabels.containsKey(label))
                uniqueLabels.put(label, k++);
            freq[closest][uniqueLabels.get(label)] += 1;
        }

        double sum = 0.0;
        //Searching for maximum in each row
        for (int i = 0; i < centers.length; i++) {
            int max = Integer.MIN_VALUE;
            for (int j = 0; j < labels.length; j++) {
                if (max < freq[i][j])
                    max = freq[i][j];
            }
            sum += max;
        }
        return sum / N;
    }

    public static void main(String[] args) throws Exception {
        KMeans kMeans = new KMeans(0);
        System.out.println("****************************** Testing K-means with euclidean: ******************************");
        for (int i = 0; i < 10; i++) {
            System.out.println("*************** test " + i + "***************");
            kMeans.test();
        }
        double[][] data = kMeans.readIrisData("iris.data.txt", 150);
        double[][] centers = kMeans.run(data, 3);
        String[] labels = kMeans.readIrisLabels("iris.data.txt");
        System.out.println("Purity of Iris:" + kMeans.computePurity(data, centers, labels));

        KMeans kMeans2 = new KMeans(1);
        System.out.println("\n\n****************************** Testing K-means with manhattan: ******************************");
        for (int i = 0; i < 10; i++) {
            System.out.println("*************** test " + i + "***************");
            kMeans2.test();
        }
        double[][] data2 = kMeans2.readIrisData("iris.data.txt", 150);
        double[][] centers2 = kMeans2.run(data2, 3);
        String[] labels2 = kMeans2.readIrisLabels("iris.data.txt");
        System.out.println("Purity of Iris:" + kMeans2.computePurity(data2, centers2, labels2));
    }
}
