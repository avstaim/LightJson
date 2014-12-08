package com.staim.lightjson.implementations.serializers;

import java.text.CharacterIterator;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.Date;

/**
 * Common Serialization Functions
 * Created by alexeyshcherbinin on 05.12.14.
 */
public final class SerializerUtil {
    public static String serializeObjectEntry(String key, String value) {
        return String.format("\"%1$s\":%2$s", key, (value != null ? value : "\"null\""));
    }

    public static String serializeString(String stringData) {
        return "\"" + stringForJSON(stringData) + "\"";
    }

    public static String serializeNumber(Number numberData) {
        String res = "";
        if (numberData instanceof Byte) res += numberData.byteValue();
        else if (numberData instanceof Short) res += numberData.shortValue();
        else if (numberData instanceof Integer) res += numberData.intValue();
        else if (numberData instanceof Long) res += numberData.longValue();
        else if (numberData instanceof Float) res += numberData.floatValue();
        else if (numberData instanceof Double) res += numberData.doubleValue();
        return res;
    }

    public static String serializeBoolean(boolean booleanData) {
        return booleanData ? "true" : "false";
    }

    public static String serializeDate(Date dateData) {
        @SuppressWarnings("SpellCheckingInspection")
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        return "\"" + dateFormat.format(dateData).replaceAll("\\\\", "\\\\\\\\") + "\"";
    }

    /**
     * Format String for JSON Serialization
     * @param input - string to format
     * @return result String
     */
    private static String stringForJSON(String input) {
        if (input == null || input.isEmpty()) return "";

        final int len = input.length();
        final StringBuilder result = new StringBuilder(len + len / 4);
        final StringCharacterIterator iterator = new StringCharacterIterator(input);
        char ch = iterator.current();
        while (ch != CharacterIterator.DONE) {
            if (ch == '\n') {
                result.append("\\n");
            } else if (ch == '\r') {
                result.append("\\r");
            } else if (ch == '\'') {
                result.append("\\\'");
            } else if (ch == '"') {
                result.append("\\\"");
            } else {
                result.append(ch);
            }
            ch = iterator.next();
        }
        return result.toString();
    }
}
