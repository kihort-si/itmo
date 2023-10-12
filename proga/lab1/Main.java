import java.util.Random;
import java.util.Arrays;
import static java.lang.Math.*;

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

        short value = C_END;
        for (int i = 0; i < c.length; i++) {
            c[i] = value;
            value -= 2;
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
                if (c[i] == 17) { // stop at
                    array[i][j] = (0.5/(sin((sin(x[j])))));
                }
                else if (c[i] == 5 || c[i] == 11 || c[i] == 15 || c[i] == 19) {
                    array[i][j] = (pow((0.5 / (0.25 + cbrt(x[j]))), 3)) / atan(pow(((x[j] +0.5) / 15), 2)) + 1;
                }
                else {
                    array[i][j] = asin(cos(atan(cos(cbrt(atan(x[j] + 0.5 / 15))))));
                }
            }
        }

//вывод результата

        System.out.print("Array c: " + Arrays.toString(c));

        System.out.println();

        System.out.print("Array x: " + Arrays.toString(x));

        System.out.println();

        // вывод массива
        for (int i = 0; i < C_LENGTH; i++){
            for (int j = 0; j < X_LENGTH; j++) {
                System.out.printf("%10.3f\t", array[i][j]); // jdb 
            }
            System.out.println();
        }

    }
}