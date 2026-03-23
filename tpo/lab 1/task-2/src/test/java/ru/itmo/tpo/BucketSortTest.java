package ru.itmo.tpo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("BucketSort — модульное тестирование")
class BucketSortTest {
    private final BucketSort sorter = new BucketSort();

    @Nested
    @DisplayName("1. Базовые случаи сортировки")
    class BasicSorting {
        @ParameterizedTest(name = "{2}: {0} -> {1}")
        @CsvFileSource(resources = "/bucketsort-basic-values.csv", numLinesToSkip = 1)
        void basicSort(String inputStr, String expectedStr, String description) {
            List<Integer> input = parseList(inputStr);
            List<Integer> expected = parseList(expectedStr);
            assertEquals(expected, sorter.sort(input), description);
        }
    }

    @Nested
    @DisplayName("2. Уже отсортированные и обратно отсортированные списки")
    class PreSortedLists {
        @ParameterizedTest(name = "{2}: {0} -> {1}")
        @CsvFileSource(resources = "/bucketsort-presorted.csv", numLinesToSkip = 1)
        void presortedSort(String inputStr, String expectedStr, String description) {
            List<Integer> input = parseList(inputStr);
            List<Integer> expected = parseList(expectedStr);
            assertEquals(expected, sorter.sort(input), description);
        }
    }

    @Nested
    @DisplayName("3. Дубликаты и повторения")
    class DuplicatesAndRepeats {
        @ParameterizedTest(name = "{2}: {0} -> {1}")
        @CsvFileSource(resources = "/bucketsort-duplicates.csv", numLinesToSkip = 1)
        void duplicateSort(String inputStr, String expectedStr, String description) {
            List<Integer> input = parseList(inputStr);
            List<Integer> expected = parseList(expectedStr);
            assertEquals(expected, sorter.sort(input), description);
        }
    }

    @Nested
    @DisplayName("4. Граничные значения диапазона [0, 999]")
    class BoundaryValues {
        @ParameterizedTest(name = "{2}: {0} -> {1}")
        @CsvFileSource(resources = "/bucketsort-boundaries.csv", numLinesToSkip = 1)
        void boundarySort(String inputStr, String expectedStr, String description) {
            List<Integer> input = parseList(inputStr);
            List<Integer> expected = parseList(expectedStr);
            assertEquals(expected, sorter.sort(input), description);
        }
    }

    @Nested
    @DisplayName("5. Невалидные входные данные")
    class InvalidInputs {
        @Test
        @DisplayName("Выбрасывает исключение при null входе")
        void rejectsNullInput() {
            assertThrows(IllegalArgumentException.class, () -> sorter.sort(null));
        }

        @Test
        @DisplayName("Выбрасывает исключение если список содержит null")
        void rejectsNullElement() {
            List<Integer> input = new ArrayList<>();
            input.add(10);
            input.add(null);
            input.add(20);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> sorter.sort(input));
            assertTrue(ex.getMessage().contains("null"));
        }

        @ParameterizedTest(name = "value={0} — вне диапазона")
        @CsvFileSource(resources = "/bucketsort-invalid-values.csv", numLinesToSkip = 1)
        void rejectsOutOfRangeValues(int value, String description) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sorter.sort(List.of(500, value, 300)));
            assertTrue(ex.getMessage().contains("out of range"),
                    "Ошибка должна содержать 'out of range' для " + value);
        }

        @ParameterizedTest(name = "value={0}")
        @CsvFileSource(resources = "/bucketsort-invalid-single.csv", numLinesToSkip = 1)
        void rejectsSingleInvalidValues(int value, String description) {
            assertThrows(IllegalArgumentException.class, () -> sorter.sort(List.of(value)), description);
        }
    }

    @Nested
    @DisplayName("6. Большие списки и производительность")
    class LargeLists {
        @Test
        @DisplayName("Сортирует список из 100 случайных элементов")
        void sorts100Elements() {
            List<Integer> input = List.of(
                    999, 0, 500, 123, 456, 789, 111, 222, 333, 444,
                    555, 666, 777, 888, 100, 200, 300, 400, 600, 700,
                    800, 900, 50, 150, 250, 350, 450, 550, 650, 750,
                    850, 950, 25, 75, 125, 175, 225, 275, 325, 375,
                    425, 475, 525, 575, 625, 675, 725, 775, 825, 875,
                    925, 975, 10, 20, 30, 40, 60, 80, 90, 99,
                    101, 201, 301, 401, 501, 601, 701, 801, 901, 151,
                    251, 351, 451, 551, 651, 751, 851, 951, 76, 176,
                    276, 376, 476, 576, 676, 776, 876, 976, 42, 84,
                    126, 168, 210, 252, 294, 336, 378, 420, 462, 504
            );
            List<Integer> result = sorter.sort(input);

            for (int i = 0; i < result.size() - 1; i++) {
                assertTrue(result.get(i) <= result.get(i + 1),
                        "Элементы не отсортированы: " + result.get(i) + " > " + result.get(i + 1));
            }

            assertEquals(input.size(), result.size());
        }

        @Test
        @DisplayName("Сортирует список из 1000 элементов с повторениями")
        void sorts1000ElementsWithDuplicates() {
            List<Integer> input = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                input.add(i * 10);
                input.add(i * 10);
                input.add(i * 10);
                input.add(i * 10);
                input.add(i * 10);
                input.add(i * 10);
                input.add(i * 10);
                input.add(i * 10);
                input.add(i * 10);
                input.add(i * 10);
            }

            List<Integer> result = sorter.sort(input);

            for (int i = 0; i < result.size() - 1; i++) {
                assertTrue(result.get(i) <= result.get(i + 1));
            }

            assertEquals(1000, result.size());
        }
    }

    @Nested
    @DisplayName("7. Трассировка (trace) сортировки")
    class TraceValidation {

        @Test
        @DisplayName("Трассирует пустой вставку в пустой бакет")
        void traceInsertEmptyOnly() {
            BucketSort.SortResult result = sorter.sortWithTrace(List.of(0, 999));

            List<String> expected = List.of(
                    "START:n=2",
                    "CALC_INDEX:v=0,b=0",
                    "INSERT_EMPTY:b=0,v=0",
                    "CALC_INDEX:v=999,b=29",
                    "INSERT_EMPTY:b=29,v=999",
                    "MERGE:b=0,v=0,pos=0",
                    "MERGE:b=29,v=999,pos=1",
                    "END:n=2"
            );

            assertEquals(expected, toStrings(result.trace()));
        }

        @Test
        @DisplayName("Трассирует вставку в начало (head)")
        void traceInsertHead() {
            BucketSort.SortResult result = sorter.sortWithTrace(List.of(10, 5));

            List<String> expected = List.of(
                    "START:n=2",
                    "CALC_INDEX:v=10,b=0",
                    "INSERT_EMPTY:b=0,v=10",
                    "CALC_INDEX:v=5,b=0",
                    "INSERT_HEAD:b=0,v=5",
                    "MERGE:b=0,v=5,pos=0",
                    "MERGE:b=0,v=10,pos=1",
                    "END:n=2"
            );

            assertEquals(expected, toStrings(result.trace()));
            assertEquals(List.of(5, 10), result.sorted());
        }

        @Test
        @DisplayName("Трассирует вставку в конец (tail) и посередину (middle)")
        void traceInsertTailAndMiddle() {
            BucketSort.SortResult result = sorter.sortWithTrace(List.of(5, 15, 10));

            List<String> expected = List.of(
                    "START:n=3",
                    "CALC_INDEX:v=5,b=0",
                    "INSERT_EMPTY:b=0,v=5",
                    "CALC_INDEX:v=15,b=0",
                    "INSERT_TAIL:b=0,v=15",
                    "CALC_INDEX:v=10,b=0",
                    "INSERT_MIDDLE:b=0,v=10",
                    "MERGE:b=0,v=5,pos=0",
                    "MERGE:b=0,v=10,pos=1",
                    "MERGE:b=0,v=15,pos=2",
                    "END:n=3"
            );

            assertEquals(expected, toStrings(result.trace()));
            assertEquals(List.of(5, 10, 15), result.sorted());
        }

        @Test
        @DisplayName("Трассирует вставку дубликатов в один бакет")
        void traceInsertDuplicatesInBucket() {
            BucketSort.SortResult result = sorter.sortWithTrace(List.of(100, 100));

            List<String> traceStr = toStrings(result.trace());

            assertTrue(traceStr.get(0).startsWith("START"));
            assertTrue(traceStr.get(traceStr.size() - 1).startsWith("END"));

            long insertCount = traceStr.stream()
                    .filter(s -> s.contains("INSERT"))
                    .count();
            assertTrue(insertCount >= 1, "Должны быть операции INSERT");

            assertEquals(List.of(100, 100), result.sorted());
        }

        @Test
        @DisplayName("Трассирует вставку с разными бакетами")
        void traceInsertMultipleBuckets() {
            BucketSort.SortResult result = sorter.sortWithTrace(List.of(0, 333, 666, 999));

            List<String> traceStr = toStrings(result.trace());

            long calcCount = traceStr.stream()
                    .filter(s -> s.startsWith("CALC_INDEX"))
                    .count();
            assertEquals(4, calcCount, "Должны быть 4 CALC_INDEX");

            assertEquals(List.of(0, 333, 666, 999), result.sorted());
        }

        @Test
        @DisplayName("Проверяет структуру trace с START и END")
        void traceStartAndEnd() {
            BucketSort.SortResult result = sorter.sortWithTrace(List.of(50, 25, 75));
            List<String> traceStr = toStrings(result.trace());

            assertEquals("START:n=3", traceStr.get(0));
            assertEquals("END:n=3", traceStr.get(traceStr.size() - 1));
        }
    }

    @Nested
    @DisplayName("8. Граничные индексы бакетов")
    class BucketIndexCalculation {
        @ParameterizedTest(name = "value={0} -> bucket={1}")
        @CsvFileSource(resources = "/bucketsort-bucket-indices.csv", numLinesToSkip = 1)
        void bucketIndexCalculation(int value, int expectedBucket, String description) {
            int actualBucket = BucketSort.bucketIndex(value);
            assertEquals(expectedBucket, actualBucket,
                    "Значение " + value + " должно попасть в бакет " + expectedBucket);
        }
    }

    @Nested
    @DisplayName("9. Специальные последовательности")
    class SpecialSequences {
        @ParameterizedTest(name = "{2}: {0} -> {1}")
        @CsvFileSource(resources = "/bucketsort-special-sequences.csv", numLinesToSkip = 1)
        void specialSequences(String inputStr, String expectedStr, String description) {
            List<Integer> input = parseList(inputStr);
            List<Integer> expected = parseList(expectedStr);
            List<Integer> result = sorter.sort(input);
            for (int i = 0; i < result.size() - 1; i++) {
                assertTrue(result.get(i) <= result.get(i + 1));
            }
            assertEquals(input.size(), result.size());
        }
    }

    @Nested
    @DisplayName("10. Проверка целостности результата")
    class ResultIntegrity {
        @Test
        @DisplayName("Результат содержит все исходные элементы")
        void resultContainsAllElements() {
            List<Integer> input = List.of(100, 50, 75, 25, 150);
            List<Integer> result = sorter.sort(input);

            assertEquals(input.size(), result.size(), "Размер должен совпадать");
            assertTrue(result.containsAll(input), "Результат должен содержать все элементы");
        }

        @Test
        @DisplayName("Результат не содержит лишних элементов")
        void resultContainsNoExtraElements() {
            List<Integer> input = List.of(10, 20, 30);
            List<Integer> result = sorter.sort(input);

            assertEquals(3, result.size());
            assertEquals(input.size(), result.size());
        }

        @Test
        @DisplayName("Результат отсортирован по неубыванию")
        void resultIsSorted() {
            List<Integer> input = List.of(999, 0, 555, 111, 888, 333);
            List<Integer> result = sorter.sort(input);

            for (int i = 0; i < result.size() - 1; i++) {
                assertTrue(result.get(i) <= result.get(i + 1),
                        "Элементы не в порядке: " + result.get(i) + " > " + result.get(i + 1));
            }
        }
    }

    @Nested
    @DisplayName("11. Граничные случаи между бакетами")
    class BucketBoundaryTests {
        @ParameterizedTest(name = "Граница бакета 0-1: value={0}")
        @CsvFileSource(resources = "/bucketsort-boundary-buckets.csv", numLinesToSkip = 1)
        @DisplayName("Значения на границе между первыми бакетами")
        void boundaryBetweenBuckets(int value) {
            List<Integer> result = sorter.sort(List.of(value, 0, 999));
            assertEquals(3, result.size());
            assertTrue(result.get(0) <= result.get(1) && result.get(1) <= result.get(2));
        }

        @Test
        @DisplayName("Все значения в одном бакете (0-33)")
        void allInOneBucket() {
            List<Integer> input = List.of(10, 20, 30, 5, 15, 25);
            List<Integer> result = sorter.sort(input);
            assertEquals(List.of(5, 10, 15, 20, 25, 30), result);
        }

        @Test
        @DisplayName("Значения распределены по разным бакетам")
        void distributedAcrossBuckets() {
            List<Integer> input = List.of(100, 200, 300, 400, 500, 600, 700, 800, 900);
            List<Integer> result = sorter.sort(input);
            assertEquals(input, result);
        }
    }

    @Nested
    @DisplayName("12. Специальные значения (граничные диапазоны)")
    class EdgeValueTests {
        @ParameterizedTest(name = "{2}: {0} -> {1}")
        @CsvFileSource(resources = "/bucketsort-edge-values.csv", numLinesToSkip = 1)
        void edgeValueSort(String inputStr, String expectedStr, String description) {
            List<Integer> input = parseList(inputStr);
            List<Integer> expected = parseList(expectedStr);
            assertEquals(expected, sorter.sort(input), description);
        }
    }

    @Nested
    @DisplayName("13. Точность трассировки для сложных сценариев")
    class AdvancedTraceTests {
        @Test
        @DisplayName("Трассирует пустой список")
        void traceEmptyList() {
            BucketSort.SortResult result = sorter.sortWithTrace(List.of());
            List<String> trace = toStrings(result.trace());

            assertEquals("START:n=0", trace.get(0));
            assertEquals("END:n=0", trace.get(trace.size() - 1));
            assertEquals(List.of(), result.sorted());
        }

        @Test
        @DisplayName("Трассирует один элемент")
        void traceSingleElement() {
            BucketSort.SortResult result = sorter.sortWithTrace(List.of(500));
            List<String> trace = toStrings(result.trace());

            assertTrue(trace.stream().anyMatch(s -> s.contains("CALC_INDEX")));
            assertTrue(trace.stream().anyMatch(s -> s.contains("INSERT")));
            assertEquals(List.of(500), result.sorted());
        }

        @Test
        @DisplayName("Трассирует длинную последовательность дубликатов")
        void traceLongDuplicateSequence() {
            List<Integer> input = List.of(250, 250, 250, 250, 250);
            BucketSort.SortResult result = sorter.sortWithTrace(input);

            assertEquals(List.of(250, 250, 250, 250, 250), result.sorted());
            List<String> trace = toStrings(result.trace());
            long insertCount = trace.stream().filter(s -> s.contains("INSERT")).count();
            assertTrue(insertCount >= 4, "Должны быть операции INSERT для каждого дубликата");
        }

        @Test
        @DisplayName("MERGE события содержат правильные позиции")
        void mergePosistionsCorrect() {
            BucketSort.SortResult result = sorter.sortWithTrace(List.of(10, 20, 30));
            List<String> trace = toStrings(result.trace());

            long mergeCount = trace.stream().filter(s -> s.startsWith("MERGE")).count();
            assertEquals(3, mergeCount, "Должны быть 3 MERGE события");

            assertTrue(trace.stream().anyMatch(s -> s.contains("pos=0")));
            assertTrue(trace.stream().anyMatch(s -> s.contains("pos=1")));
            assertTrue(trace.stream().anyMatch(s -> s.contains("pos=2")));
        }
    }

    @Nested
    @DisplayName("14. Производительность и объём данных")
    class PerformanceTests {
        @Test
        @DisplayName("Сортирует 500 элементов быстро")
        void sorts500Elements() {
            List<Integer> input = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                for (int j = 0; j < 10; j++) {
                    input.add((i * 20 + j) % 1000);
                }
            }

            long startTime = System.currentTimeMillis();
            List<Integer> result = sorter.sort(input);
            long elapsed = System.currentTimeMillis() - startTime;

            assertEquals(500, result.size());
            assertTrue(elapsed < 1000, "Сортировка 500 элементов должна быть < 1 сек");

            for (int i = 0; i < result.size() - 1; i++) {
                assertTrue(result.get(i) <= result.get(i + 1));
            }
        }

        @Test
        @DisplayName("Сортирует 100 элементов с 99% дубликатов")
        void sortMostlyDuplicates() {
            List<Integer> input = new ArrayList<>();
            for (int i = 0; i < 99; i++) {
                input.add(500);
            }
            input.add(100);

            List<Integer> result = sorter.sort(input);
            assertEquals(100, result.size());
            assertEquals(100, result.get(0).intValue());
            for (int i = 1; i < result.size(); i++) {
                assertEquals(500, result.get(i).intValue());
            }
        }

        @Test
        @DisplayName("Сортирует случайное распределение 200 элементов")
        void sortRandomDistribution() {
            List<Integer> input = new ArrayList<>();

            for (int i = 0; i < 200; i++) {
                int bucket = i % 30;
                int value = (bucket * 34 + i / 30) % 1000;
                input.add(value);
            }

            List<Integer> result = sorter.sort(input);
            assertEquals(200, result.size());

            for (int i = 0; i < result.size() - 1; i++) {
                assertTrue(result.get(i) <= result.get(i + 1));
            }
        }
    }

    @Nested
    @DisplayName("15. Невалидные данные и исключения")
    class ExceptionHandlingTests {
        @Test
        @DisplayName("null в середине списка выбрасывает исключение")
        void nullInMiddle() {
            List<Integer> input = new ArrayList<>();
            input.add(100);
            input.add(200);
            input.add(null);
            input.add(300);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sorter.sort(input));
            assertTrue(ex.getMessage().contains("null"));
        }

        @Test
        @DisplayName("Отрицательное значение -500 выбрасывает исключение")
        void negativeValue() {
            assertThrows(IllegalArgumentException.class,
                    () -> sorter.sort(List.of(-500)));
        }

        @Test
        @DisplayName("Значение 1500 выбрасывает исключение")
        void largeValue() {
            assertThrows(IllegalArgumentException.class,
                    () -> sorter.sort(List.of(1500)));
        }

        @Test
        @DisplayName("Integer.MIN_VALUE выбрасывает исключение")
        void minIntValue() {
            assertThrows(IllegalArgumentException.class,
                    () -> sorter.sort(List.of(Integer.MIN_VALUE)));
        }

        @Test
        @DisplayName("Integer.MAX_VALUE выбрасывает исключение")
        void maxIntValue() {
            assertThrows(IllegalArgumentException.class,
                    () -> sorter.sort(List.of(Integer.MAX_VALUE)));
        }

        @Test
        @DisplayName("Смешанные валидные и невалидные значения выбрасывают исключение")
        void mixedValidInvalid() {
            assertThrows(IllegalArgumentException.class,
                    () -> sorter.sort(List.of(100, 200, 1001, 300)));
        }

        @Test
        @DisplayName("Очень отрицательное значение -999999 выбрасывает исключение")
        void veryNegativeValue() {
            assertThrows(IllegalArgumentException.class,
                    () -> sorter.sort(List.of(-999999)));
        }
    }

    @Nested
    @DisplayName("16. Стабильность и мультипрогон")
    class StabilityTests {
        @Test
        @DisplayName("Повторный прогон дает тот же результат")
        void repeatRunSameResult() {
            List<Integer> input = List.of(750, 250, 500, 100, 900);
            List<Integer> result1 = sorter.sort(input);
            List<Integer> result2 = sorter.sort(input);

            assertEquals(result1, result2, "Два прогона должны дать одинаковый результат");
        }

        @Test
        @DisplayName("Большой список дает стабильный результат")
        void largeListStability() {
            List<Integer> input = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                input.add((i * 113) % 1000);
            }

            List<Integer> result1 = sorter.sort(input);
            List<Integer> result2 = sorter.sort(input);
            List<Integer> result3 = sorter.sort(input);

            assertEquals(result1, result2);
            assertEquals(result2, result3);
        }
    }

    private static List<String> toStrings(List<BucketSort.TraceStep> trace) {
        return trace.stream().map(t -> t.point() + ":" + t.detail()).collect(Collectors.toList());
    }

    private static List<Integer> parseList(String str) {
        if (str == null || str.isEmpty()) {
            return new ArrayList<>();
        }
        return java.util.Arrays.stream(str.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
}
