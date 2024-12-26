package com.polytech.utils;

/**
 * Console class to print messages in different colors
 */
public class Console {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    static final String ANSI_BOLD = "\u001B[1m";

    public static void errorln(String message) {
        System.err.println(ANSI_RED + ANSI_BOLD + message + ANSI_RESET);
    }

    public static void successln(String message) {
        System.out.println(ANSI_GREEN + ANSI_BOLD + message + ANSI_RESET);
    }

    public static void warnln(String message) {
        System.out.println(ANSI_YELLOW + ANSI_BOLD + message + ANSI_RESET);
    }
}