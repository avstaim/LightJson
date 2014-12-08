package com.staim.lightjson.implementations.serializers;

import com.staim.lightjson.JsonElement;
import com.staim.lightjson.JsonSerializer;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import static com.staim.lightjson.implementations.serializers.SerializerUtil.*;

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
                case STRING: return serializeString((String)element.getData());
                case NUMBER: return serializeNumber((Number)element.getData());
                case BOOLEAN: return serializeBoolean((Boolean)element.getData());
                case DATE: return serializeDate((Date)element.getData());
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
                if (!resObject.isEmpty()) resObject += ",";
                final String key = taskEntry.getKey();
                resObject += serializeObjectEntry(key, value);
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
                if (!resArray.isEmpty()) resArray += ",";
                resArray += value;
            }

            return "[" + resArray + "]";
        }
    }
}
