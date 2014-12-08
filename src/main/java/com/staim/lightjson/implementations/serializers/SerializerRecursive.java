package com.staim.lightjson.implementations.serializers;

import com.staim.lightjson.JsonElement;
import com.staim.lightjson.JsonSerializer;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import static com.staim.lightjson.implementations.serializers.SerializerUtil.*;

/**
 * Simple Recursive Serializer
 *
 * Created by alexeyshcherbinin on 05.12.14.
 */
public class SerializerRecursive implements JsonSerializer {
    public String serialize(JsonElement element) {
        return (new SerializerWorker(element)).compute();
    }

    @SuppressWarnings("unchecked")
    private class SerializerWorker {
        private final JsonElement element;

        public SerializerWorker(JsonElement element) { this.element = element; }

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

            for (Map.Entry<String, JsonElement> childEntry : objectData.entrySet()) {
                SerializerWorker task = new SerializerWorker(childEntry.getValue());
                final String value = task.compute();
                if (!resObject.isEmpty()) resObject += ",";
                final String key = childEntry.getKey();
                resObject += serializeObjectEntry(key, value);
            }

            return "{" + resObject + "}";
        }

        private String serializeArray() {
            String resArray = "";
            Collection<JsonElement> arrayData = element.getData();

            for (JsonElement child : arrayData) {
                SerializerWorker task = new SerializerWorker(child);
                final String value = task.compute();
                if (!resArray.isEmpty()) resArray += ",";
                resArray += value;
            }

            return "[" + resArray + "]";
        }
    }
}
