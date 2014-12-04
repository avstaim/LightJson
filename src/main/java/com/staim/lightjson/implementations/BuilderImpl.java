package com.staim.lightjson.implementations;

import com.staim.lightjson.JsonBuilder;
import com.staim.lightjson.JsonElement;
import com.staim.lightjson.implementations.elements.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Json Builder Implementation
 *
 * Created by alexeyshcherbinin on 04.12.14.
 */
public class BuilderImpl implements JsonBuilder {
    @Override
    public ObjectBuilder objectBuilder() {
        return new ObjectBuilder() {
            Map<String, JsonElement> data = new TreeMap<>(); // Using TreeMap to maintain order
            @Override
            public ObjectBuilder append(String key, JsonElement element) {
                data.put(key, element);
                return this;
            }
            @Override
            public ObjectBuilder append(String key, Builder builder) {
                return append(key, builder.build());
            }
            @Override public JsonElement build() { return new JsonObject(data); }
        };
    }

    @Override
    public ArrayBuilder arrayBuilder() {
        return new ArrayBuilder() {
            List<JsonElement> data = new ArrayList<>();
            @Override
            public ArrayBuilder append(JsonElement element) {
                data.add(element);
                return this;
            }
            @Override public ArrayBuilder append(Builder builder) { return append(builder.build()); }
            @Override public JsonElement build() { return new JsonArray(data); }
        };
    }

    @Override public JsonElement string(String string) { return new JsonString(string); }
    @Override public JsonElement number(Number number) { return new JsonNumber(number); }
    @Override public JsonElement bool(boolean bool) { return new JsonBoolean(bool); }
    @Override public JsonElement nil() { return new JsonNull(); }
}
