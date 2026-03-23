package ru.itmo.tpo;

import java.util.ArrayList;
import java.util.List;

public class BucketSort {
    public static final int BUCKET_COUNT = 30;
    public static final int MAX_DATA_VALUE = 999;

    public enum Point {
        START,
        CALC_INDEX,
        INSERT_EMPTY,
        INSERT_HEAD,
        INSERT_MIDDLE,
        INSERT_TAIL,
        MERGE,
        END
    }

    public static final class TraceStep {
        private final Point point;
        private final String detail;

        public TraceStep(Point point, String detail) {
            this.point = point;
            this.detail = detail;
        }

        public Point point() {
            return point;
        }

        public String detail() {
            return detail;
        }
    }

    public static final class SortResult {
        private final List<Integer> sorted;
        private final List<TraceStep> trace;

        public SortResult(List<Integer> sorted, List<TraceStep> trace) {
            this.sorted = sorted;
            this.trace = trace;
        }

        public List<Integer> sorted() {
            return sorted;
        }

        public List<TraceStep> trace() {
            return trace;
        }
    }

    private static final class Node {
        private final int data;
        private Node next;

        private Node(int data) {
            this.data = data;
        }
    }

    public List<Integer> sort(List<Integer> input) {
        return sortWithTrace(input).sorted();
    }

    public SortResult sortWithTrace(List<Integer> input) {
        validateInput(input);

        List<TraceStep> trace = new ArrayList<>();
        trace.add(new TraceStep(Point.START, "n=" + input.size()));

        Node[] buckets = new Node[BUCKET_COUNT];

        for (int value : input) {
            int index = bucketIndex(value);
            trace.add(new TraceStep(Point.CALC_INDEX, "v=" + value + ",b=" + index));

            Node node = new Node(value);
            Node head = buckets[index];

            if (head == null) {
                buckets[index] = node;
                trace.add(new TraceStep(Point.INSERT_EMPTY, "b=" + index + ",v=" + value));
                continue;
            }

            if (head.data >= node.data) {
                node.next = head;
                buckets[index] = node;
                trace.add(new TraceStep(Point.INSERT_HEAD, "b=" + index + ",v=" + value));
                continue;
            }

            Node current = head;
            while (current.next != null && current.next.data < node.data) {
                current = current.next;
            }

            if (current.next != null) {
                node.next = current.next;
                current.next = node;
                trace.add(new TraceStep(Point.INSERT_MIDDLE, "b=" + index + ",v=" + value));
            } else {
                current.next = node;
                trace.add(new TraceStep(Point.INSERT_TAIL, "b=" + index + ",v=" + value));
            }
        }

        List<Integer> out = new ArrayList<>(input.size());
        for (int i = 0; i < BUCKET_COUNT; i++) {
            Node current = buckets[i];
            while (current != null) {
                out.add(current.data);
                trace.add(new TraceStep(Point.MERGE, "b=" + i + ",v=" + current.data + ",pos=" + (out.size() - 1)));
                current = current.next;
            }
        }

        trace.add(new TraceStep(Point.END, "n=" + out.size()));
        return new SortResult(out, trace);
    }

    static int bucketIndex(int value) {
        return (value * BUCKET_COUNT) / (MAX_DATA_VALUE + 1);
    }

    private static void validateInput(List<Integer> input) {
        if (input == null) {
            throw new IllegalArgumentException("input must not be null");
        }
        for (Integer v : input) {
            if (v == null) {
                throw new IllegalArgumentException("input contains null");
            }
            if (v < 0 || v > MAX_DATA_VALUE) {
                throw new IllegalArgumentException("value out of range [0.." + MAX_DATA_VALUE + "]: " + v);
            }
        }
    }
}
