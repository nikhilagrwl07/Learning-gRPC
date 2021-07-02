package com.learninggrpc.server;

import com.learninggrpc.models.Balance;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AccountDatabase {
    /*
    This is db
    1 => 10
    2 => 20
    .
    .
    10 => 100
     */
    private static final Map<Integer, Integer> MAP = IntStream
            .rangeClosed(1, 10)
            .boxed()
            .collect(Collectors.toMap(
                    Function.identity(),
                    v -> 100
            ));

    public static int getBalance(int accountNumber) {
        return MAP.get(accountNumber);
    }

    public static Integer addBalance(int accountNumber, int amount) {
        return MAP.computeIfPresent(accountNumber, (k, v) -> v + amount);
    }

    public static Integer deductBalance(int accountNumber, int amount) {
        return MAP.computeIfPresent(accountNumber, (k, v) -> v - amount);
    }

    public static void printAccountDetails(){
        System.out.println(
                MAP
        );
    }
}
