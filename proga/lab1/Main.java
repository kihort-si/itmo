import java.util.Random;
import java.util.Arrays;

public class Main{
    public static void main(String[] args){

        final short C_START = 5;
        final short C_END = 19;
        final int C_LENGTH = 8;

        final float X_START = -7.0f;
        final float X_END = 8.0f;
        final int X_LENGTH = 11;

//создание массива, заполненного нечётными числами от 5 до 19 включительно в порядке убывания

        short[] c = new short[C_LENGTH];

        for (int i = 0; i < c.length; i++) {
            for (int j = C_START; j <= C_END; j++) {
                if (j % 2 != 0) {
                    boolean exist = false; //существует ли элемент в массиве
                    short value = (short) j; //текущее значение

                    //проверяем, существует ли элемент в массиве

                    for (int z = 0; z < i; z++) {
                        if (c[z] == value) {
                            exist = true;
                            break;
                        }
                    }

                    //если не существует, добавляем

                    if (!exist) {
                        c[i] = value;
                        break;
                    }
                }
            }
        }

        //сортировка пузырьком в порядке убывания

        short temp;
        boolean isSorted = false;

        while (!isSorted) {
            isSorted = true;

            for (int i = 0; i < c.length - 1; i++) {
                if (c[i] < c[i + 1]) {
                    temp = c[i];
                    c[i] = c[i + 1];
                    c[i + 1] = temp;
                    isSorted = false;
                }
            }
        }

//генератор случайных чисел от -7.0 до 8.0

        float[] x = new float[X_LENGTH];

        Random random = new Random();

        for (int i = 0; i < x.length; i++) {
            x[i] = X_START + random.nextFloat() * (X_END - X_START);
        }

//создание двумерного массива

        double[][] array = new double[C_LENGTH][X_LENGTH];

        for (int i = 0; i < C_LENGTH; i++) {
            for (int j = 0; j < X_LENGTH; j++) {
                if (c[i] == 17) {
                    array[i][j] = (0.5/(Math.sin((Math.sin(x[j])))));
                }
                else if (c[i] == 5 || c[i] == 11 || c[i] == 15 || c[i] == 19) {
                    array[i][j] = (Math.pow((0.5 / (0.25 + Math.cbrt(x[j]))), 3)) / Math.atan(Math.pow(((x[j] +0.5) / 15), 2)) + 1;
                }
                else {
                    array[i][j] = Math.asin(Math.cos(Math.atan(Math.cos(Math.cbrt(Math.atan(x[j] + 0.5 / 15))))));
                }
            }
        }

//вывод результата

        System.out.print("Array c: " + Arrays.toString(c));

        System.out.println();

        System.out.print("Array x: " + Arrays.toString(x));

        System.out.println();

        // вывод номеров столбцов
        System.out.print("\t\t");
        for (int j = 0; j < X_LENGTH; j++) {
            System.out.printf("[%d]\t\t\t", j + 1);
        }
        System.out.println();

        for (int i = 0; i < C_LENGTH; i++) {
            // вывод номера строки
            System.out.printf("[%d]\t", i + 1);

            // вывод массива
            for (int j = 0; j < X_LENGTH; j++) {
                System.out.printf("%8.3f\t", array[i][j]);
            }
            System.out.println();
        }


    }
}