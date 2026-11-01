package com.kiranacademy.rpms.utility;

import com.kiranacademy.rpms.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Console input helper with built-in validation and retry loops.
 */
public final class InputUtil {

    private final Scanner scanner = new Scanner(System.in);

    public String readLine(String prompt) {
        System.out.print(prompt);
        try {
            if (!scanner.hasNextLine()) {
                throw new ConsoleExitException();
            }
            return scanner.nextLine().trim();
        } catch (NoSuchElementException e) {
            throw new ConsoleExitException();
        }
    }

    public String readOptional(String prompt, String currentValue) {
        String current = currentValue == null ? "" : currentValue;
        String value = readLine(prompt + " [" + current + "]: ");
        return value.isBlank() ? current : value;
    }

    public String readRequired(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (!value.isBlank()) {
                return value;
            }
            System.out.println("  ! Value is required.");
        }
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            String value = readLine(prompt);
            try {
                int number = Integer.parseInt(value);
                if (number < min || number > max) {
                    System.out.println("  ! Enter a number between " + min + " and " + max + ".");
                    continue;
                }
                return number;
            } catch (NumberFormatException e) {
                System.out.println("  ! Invalid number. Try again.");
            }
        }
    }

    public int readInt(String prompt, int min, int max, int defaultValue) {
        String value = readLine(prompt + " [" + defaultValue + "]: ");
        if (value.isBlank()) {
            return defaultValue;
        }
        while (true) {
            try {
                int number = Integer.parseInt(value);
                if (number >= min && number <= max) {
                    return number;
                }
                System.out.println("  ! Enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  ! Invalid number. Try again.");
            }
            value = readLine(prompt + " [" + defaultValue + "]: ");
            if (value.isBlank()) {
                return defaultValue;
            }
        }
    }

    public long readLong(String prompt, long min, long max) {
        while (true) {
            String value = readLine(prompt);
            try {
                long number = Long.parseLong(value);
                if (number < min || number > max) {
                    System.out.println("  ! Enter a number between " + min + " and " + max + ".");
                    continue;
                }
                return number;
            } catch (NumberFormatException e) {
                System.out.println("  ! Invalid number. Try again.");
            }
        }
    }

    public BigDecimal readDecimal(String prompt, double min, double max) {
        while (true) {
            String value = readLine(prompt);
            try {
                BigDecimal number = new BigDecimal(value);
                if (number.doubleValue() < min || number.doubleValue() > max) {
                    System.out.println("  ! Enter a value between " + min + " and " + max + ".");
                    continue;
                }
                return number;
            } catch (NumberFormatException e) {
                System.out.println("  ! Invalid decimal value. Try again.");
            }
        }
    }

    public BigDecimal readDecimal(String prompt, double min, double max, BigDecimal defaultValue) {
        String value = readLine(prompt + " [" + defaultValue.toPlainString() + "]: ");
        if (value.isBlank()) {
            return defaultValue;
        }
        while (true) {
            try {
                BigDecimal number = new BigDecimal(value);
                if (number.doubleValue() >= min && number.doubleValue() <= max) {
                    return number;
                }
                System.out.println("  ! Enter a value between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  ! Invalid decimal value. Try again.");
            }
            value = readLine(prompt + " [" + defaultValue.toPlainString() + "]: ");
            if (value.isBlank()) {
                return defaultValue;
            }
        }
    }

    public LocalDate readDate(String prompt) {
        while (true) {
            String value = readLine(prompt);
            try {
                return DateUtil.parseDate(value);
            } catch (ValidationException e) {
                System.out.println("  ! " + e.getMessage());
            }
        }
    }

    public LocalDate readDate(String prompt, LocalDate defaultValue) {
        while (true) {
            System.out.print(prompt);
            try {
                if (!scanner.hasNextLine()) {
                    throw new ConsoleExitException();
                }
                String value = scanner.nextLine().trim();
                if (value.isBlank()) {
                    return defaultValue;
                }
                return DateUtil.parseDate(value);
            } catch (NoSuchElementException e) {
                throw new ConsoleExitException();
            } catch (ValidationException e) {
                System.out.println("  ! " + e.getMessage());
            }
        }
    }

    public LocalDateTime readDateTime(String prompt) {
        while (true) {
            String value = readLine(prompt);
            try {
                return DateUtil.parseDateTime(value);
            } catch (ValidationException e) {
                System.out.println("  ! " + e.getMessage());
            }
        }
    }

    public boolean readYesNo(String prompt) {
        while (true) {
            String value = readLine(prompt + " (y/n): ");
            if (value.equalsIgnoreCase("y") || value.equalsIgnoreCase("yes")) {
                return true;
            }
            if (value.equalsIgnoreCase("n") || value.equalsIgnoreCase("no")) {
                return false;
            }
            System.out.println("  ! Enter y or n.");
        }
    }

    public <E extends Enum<E>> E readEnum(String prompt, Class<E> type) {
        E[] values = type.getEnumConstants();
        StringBuilder options = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            options.append(i + 1).append('=').append(values[i].name());
            if (i < values.length - 1) {
                options.append(", ");
            }
        }
        System.out.println("  " + options);
        while (true) {
            String value = readLine(prompt);
            try {
                int index = Integer.parseInt(value);
                if (index >= 1 && index <= values.length) {
                    return values[index - 1];
                }
            } catch (NumberFormatException ignored) {
                for (E candidate : values) {
                    if (candidate.name().equalsIgnoreCase(value)) {
                        return candidate;
                    }
                }
            }
            System.out.println("  ! Invalid option.");
        }
    }

    public void pause() {
        System.out.print("\nPress Enter to continue...");
        try {
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
        } catch (NoSuchElementException ignored) {
            throw new ConsoleExitException();
        }
    }

    public void close() {
        scanner.close();
    }
}
