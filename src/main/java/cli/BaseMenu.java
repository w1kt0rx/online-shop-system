package cli;

import exception.handler.GlobalExceptionHandler;

import java.util.Scanner;

public abstract class BaseMenu {
    protected static final String NL = "\n";
    protected static final String LINE = "─".repeat(55);
    protected static final String DLINE = "═".repeat(55);

    protected final Scanner scanner;
    protected final Session session;
    protected final GlobalExceptionHandler exHandler;

    protected BaseMenu(
            Scanner scanner,
            Session session,
            GlobalExceptionHandler exHandler
    ) {
        this.scanner = scanner;
        this.session = session;
        this.exHandler = exHandler;
    }

    protected void print(String text) {
        System.out.println(text);
    }

    protected int readInt() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception e) {
            return -1;
        }
    }

    protected long readLong() {
        try {
            return Long.parseLong(scanner.nextLine().trim());
        } catch (Exception e) {
            return -1L;
        }
    }


}