package com.sheng.hikingbackend.service.landscape.rf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SimpleRandomForestClassifier {

    private final List<Node> trees;

    private SimpleRandomForestClassifier(List<Node> trees) {
        this.trees = trees;
    }

    public static SimpleRandomForestClassifier train(
            double[][] features,
            int[] labels,
            int treeCount,
            int maxDepth,
            int minLeafSize,
            int maxFeatures,
            long seed) {
        Random random = new Random(seed);
        List<Node> trees = new ArrayList<>(treeCount);
        for (int i = 0; i < treeCount; i++) {
            int[] bootstrap = bootstrapIndexes(features.length, random);
            trees.add(buildNode(features, labels, bootstrap, 0, maxDepth, minLeafSize, maxFeatures, random));
        }
        return new SimpleRandomForestClassifier(trees);
    }

    public double predictProbability(double[] input) {
        if (trees.isEmpty()) {
            return 0.5;
        }
        return trees.stream().mapToDouble(tree -> tree.predict(input)).average().orElse(0.5);
    }

    private static Node buildNode(
            double[][] features,
            int[] labels,
            int[] indexes,
            int depth,
            int maxDepth,
            int minLeafSize,
            int maxFeatures,
            Random random) {
        double positiveRate = positiveRate(labels, indexes);
        if (depth >= maxDepth || indexes.length <= minLeafSize || positiveRate == 0 || positiveRate == 1) {
            return new LeafNode(positiveRate);
        }

        int featureCount = features[0].length;
        int[] candidateFeatures = randomFeatureIndexes(featureCount, Math.min(maxFeatures, featureCount), random);
        Split bestSplit = null;
        for (int featureIndex : candidateFeatures) {
            Split candidate = bestSplitForFeature(features, labels, indexes, featureIndex, minLeafSize);
            if (candidate != null && (bestSplit == null || candidate.gini < bestSplit.gini)) {
                bestSplit = candidate;
            }
        }

        if (bestSplit == null) {
            return new LeafNode(positiveRate);
        }

        Node left = buildNode(features, labels, bestSplit.leftIndexes, depth + 1, maxDepth, minLeafSize, maxFeatures, random);
        Node right = buildNode(features, labels, bestSplit.rightIndexes, depth + 1, maxDepth, minLeafSize, maxFeatures, random);
        return new DecisionNode(bestSplit.featureIndex, bestSplit.threshold, left, right);
    }

    private static Split bestSplitForFeature(double[][] features, int[] labels, int[] indexes, int featureIndex, int minLeafSize) {
        double[] values = Arrays.stream(indexes).mapToDouble(index -> features[index][featureIndex]).sorted().toArray();
        if (values.length < minLeafSize * 2) {
            return null;
        }
        Split bestSplit = null;
        for (int i = 1; i < values.length; i++) {
            if (values[i] == values[i - 1]) {
                continue;
            }
            double threshold = (values[i] + values[i - 1]) / 2.0;
            int[] leftIndexes = Arrays.stream(indexes).filter(index -> features[index][featureIndex] <= threshold).toArray();
            int[] rightIndexes = Arrays.stream(indexes).filter(index -> features[index][featureIndex] > threshold).toArray();
            if (leftIndexes.length < minLeafSize || rightIndexes.length < minLeafSize) {
                continue;
            }
            double gini = weightedGini(labels, leftIndexes, rightIndexes);
            if (bestSplit == null || gini < bestSplit.gini) {
                bestSplit = new Split(featureIndex, threshold, gini, leftIndexes, rightIndexes);
            }
        }
        return bestSplit;
    }

    private static double weightedGini(int[] labels, int[] leftIndexes, int[] rightIndexes) {
        double total = leftIndexes.length + rightIndexes.length;
        return gini(labels, leftIndexes) * (leftIndexes.length / total)
                + gini(labels, rightIndexes) * (rightIndexes.length / total);
    }

    private static double gini(int[] labels, int[] indexes) {
        double positiveRate = positiveRate(labels, indexes);
        double negativeRate = 1 - positiveRate;
        return 1 - (positiveRate * positiveRate + negativeRate * negativeRate);
    }

    private static double positiveRate(int[] labels, int[] indexes) {
        if (indexes.length == 0) {
            return 0.0;
        }
        return Arrays.stream(indexes).filter(index -> labels[index] == 1).count() / (double) indexes.length;
    }

    private static int[] bootstrapIndexes(int size, Random random) {
        int[] indexes = new int[size];
        for (int i = 0; i < size; i++) {
            indexes[i] = random.nextInt(size);
        }
        return indexes;
    }

    private static int[] randomFeatureIndexes(int featureCount, int pickCount, Random random) {
        List<Integer> indexes = new ArrayList<>(featureCount);
        for (int i = 0; i < featureCount; i++) {
            indexes.add(i);
        }
        java.util.Collections.shuffle(indexes, random);
        return indexes.stream().limit(pickCount).mapToInt(Integer::intValue).toArray();
    }

    private interface Node {
        double predict(double[] input);
    }

    private record DecisionNode(int featureIndex, double threshold, Node left, Node right) implements Node {
        @Override
        public double predict(double[] input) {
            return input[featureIndex] <= threshold ? left.predict(input) : right.predict(input);
        }
    }

    private record LeafNode(double probability) implements Node {
        @Override
        public double predict(double[] input) {
            return probability;
        }
    }

    private record Split(int featureIndex, double threshold, double gini, int[] leftIndexes, int[] rightIndexes) {
    }
}
