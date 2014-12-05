package com.staim.lightjson.implementations.serializers;

import com.staim.lightjson.JsonElement;
import com.staim.lightjson.JsonSerializer;

import java.text.CharacterIterator;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * Serializer Using ForkJoinPool
 *
 * Created by alexeyshcherbinin on 05.12.14.
 */
public class SerializerForkJoin implements JsonSerializer {
    public String serialize(JsonElement element) {
        return new ForkJoinPool().invoke(new SerializerWorker(element));
    }

    @SuppressWarnings("unchecked")
    private class SerializerWorker extends RecursiveTask<String> {
        private final JsonElement element;

        public SerializerWorker(JsonElement element) { this.element = element; }

        @Override
        protected String compute() {
            if (element == null) return null;

            switch (element.getType()) {
                case OBJECT: return serializeObject();
                case ARRAY: return serializeArray();
                case STRING: return serializeString();
                case NUMBER: return serializeNumber();
                case BOOLEAN: return serializeBoolean();
                case DATE: return serializeDate();
                case NULL:
                default: return "null";
            }
        }

        private String serializeObject() {
            String resObject = "";
            Map<String, JsonElement> objectData = element.getData();
            Map<String, SerializerWorker> subTasks = new HashMap<>();

            for (Map.Entry<String, JsonElement> childEntry : objectData.entrySet()) {
                SerializerWorker task = new SerializerWorker(childEntry.getValue());
                task.fork(); // execute async
                subTasks.put(childEntry.getKey(), task);
            }

            for (Map.Entry<String, SerializerWorker> taskEntry : subTasks.entrySet()) {
                final String value = taskEntry.getValue().join();
                if (!resObject.isEmpty()) resObject += ", ";
                final String key = taskEntry.getKey();
                resObject += String.format("\"%1$s\" : %2$s", key, (value != null ? value : "\"null\""));
            }

            return "{" + resObject + "}";
        }

        private String serializeArray() {
            String resArray = "";
            Collection<JsonElement> arrayData = element.getData();
            List<SerializerWorker> subTasks = new LinkedList<>();

            for (JsonElement child : arrayData) {
                SerializerWorker task = new SerializerWorker(child);
                task.fork(); // execute async
                subTasks.add(task);
            }

            for (SerializerWorker task : subTasks) {
                final String value = task.join();
                if (!resArray.isEmpty()) resArray += ", ";
                resArray += value;
            }

            return "[" + resArray + "]";
        }

        private String serializeString() {
            return "\"" + stringForJSON(element.getStringData()) + "\"";
        }

        private String serializeNumber() {
            String res = "";
            Number numberData = element.getNumberData();
            if (numberData instanceof Byte) res += numberData.byteValue();
            else if (numberData instanceof Short) res += numberData.shortValue();
            else if (numberData instanceof Integer) res += numberData.intValue();
            else if (numberData instanceof Long) res += numberData.longValue();
            else if (numberData instanceof Float) res += numberData.floatValue();
            else if (numberData instanceof Double) res += numberData.doubleValue();
            return res;
        }

        private String serializeBoolean() {
            return element.getBooleanData() ? "true" : "false";
        }

        private String serializeDate() {
            @SuppressWarnings("SpellCheckingInspection")
            final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            return "\"" + dateFormat.format(element.getDateData()).replaceAll("\\\\", "\\\\\\\\") + "\"";
        }
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
