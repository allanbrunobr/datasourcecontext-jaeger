package com.bruno.jaegerclient.thread;

public class LocalThreadTest {

    private static final ThreadLocal<String> temp = new ThreadLocal<>();


    public static void setValue(Integer value) {
        if (value == null) {
            throw new NullPointerException();
        }
        temp.set(value.toString());
    }

    public static String getValue () {
        return temp.get();
    }

    public static void clearValue() {
        temp.remove();
    }
}
