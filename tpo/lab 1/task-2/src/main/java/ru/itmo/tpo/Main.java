package ru.itmo.tpo;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        BucketSort sorter = new BucketSort();
        List<Integer> input = List.of(999, 0, 500, 1, 998, 333, 334);
        BucketSort.SortResult result = sorter.sortWithTrace(input);

        System.out.println("Input : " + input);
        System.out.println("Sorted: " + result.sorted());
        System.out.println("Trace steps: " + result.trace().size());
    }
}