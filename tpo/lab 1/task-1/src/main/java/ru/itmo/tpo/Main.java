package ru.itmo.tpo;

import ru.itmo.tpo.utils.Calculation;

public class Main {
    public static void main(String[] args) {
        try {
            double x = Double.parseDouble(args[0]);
            double eps = Double.parseDouble(args[1]);
            int maxIter = Integer.parseInt(args[2]);
            double res = Calculation.arcsin(x, eps, maxIter);
            System.out.println("arcsin(" + x + ") = " + res);
        } catch (IllegalArgumentException | ArithmeticException e) {
            System.err.println("Ошибка: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Использование: Main <x> <eps> <maxIter>");
        }
    }
}