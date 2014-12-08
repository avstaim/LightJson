package com.staim.lightjson.implementations;

import com.staim.lightjson.JsonBuilder;
import com.staim.lightjson.JsonElement;
import com.staim.lightjson.JsonType;
import com.staim.lightjson.implementations.elements.JsonArrayElement;
import com.staim.lightjson.implementations.elements.JsonNullElement;
import com.staim.lightjson.implementations.elements.JsonObjectElement;
import com.staim.lightjson.implementations.elements.JsonPlainElement;

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
            @Override public JsonElement build() { return new JsonObjectElement(data); }
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
            @Override public JsonElement build() { return new JsonArrayElement(data); }
        };
    }

    @Override public JsonElement string(String string) { return new JsonPlainElement<>(string, JsonType.STRING); }
    @Override public JsonElement number(Number number) { return new JsonPlainElement<>(number, JsonType.NUMBER); }
    @Override public JsonElement bool(boolean bool) { return new JsonPlainElement<>(bool, JsonType.BOOLEAN); }
    @Override public <T> JsonElement auto(T object) { return new JsonPlainElement<>(object); }
    @Override public JsonElement nil() { return new JsonNullElement(); }
}
