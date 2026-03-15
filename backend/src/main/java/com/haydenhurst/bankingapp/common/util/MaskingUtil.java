package com.haydenhurst.bankingapp.common.util;

public final class MaskingUtil {
    private MaskingUtil() {
        throw new UnsupportedOperationException("Utility class - do not instantiate");
    }

    public static String maskAllButFirstN(String value, int visibleChars) {
        if (value == null || value.isEmpty()) {
            return "***";
        }
        if (visibleChars >= value.length()) {
            return value;
        }
        return value.substring(0, visibleChars) + "*".repeat(value.length() - visibleChars);
    }

    public static String maskAllButLastN(String value, int visibleChars) {
        if (value == null || value.isEmpty()) {
            return "***";
        }
        if (visibleChars >= value.length()) {
            return value;
        }
        return "*".repeat(value.length() - visibleChars) + value.substring(value.length() - visibleChars);
    }

    public static String maskSsn(String rawSSN) {
        if (rawSSN == null || rawSSN.length() < 4) {
            return "***-**-****";
        }

        String sanitizedSSN = rawSSN.replaceAll("[^0-9]", "");

        String masked = maskAllButLastN(sanitizedSSN, 4);

        return "***-**-" + masked.substring(masked.length() - 4);
    }

    public static String maskPhoneNumber(String rawPhoneNumber) {
        if (rawPhoneNumber == null || rawPhoneNumber.length() < 4) {
            return "***-***-****";
        }

        String sanitizedPhoneNumber= rawPhoneNumber.replaceAll("[^0-9]", "");

        String masked = maskAllButLastN(sanitizedPhoneNumber, 4);

        return "***-***-" + masked.substring(masked.length() - 4);
    }

    public static String maskEmail(String rawEmail) {
        if (rawEmail == null || !rawEmail.contains("@")) {
            return "***@***.***";
        }

        int atIndex = rawEmail.indexOf('@');
        String localPart = rawEmail.substring(0, atIndex);
        String domain = rawEmail.substring(atIndex);

        String maskedLocal = maskAllButFirstN(localPart, 2);

        return maskedLocal + domain;
    }
}
