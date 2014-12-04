package com.staim.lightjson;

/**
 * Json Builder
 * Created by alexeyshcherbinin on 03.12.14.
 */
public interface JsonBuilder {
    interface Builder {
        JsonElement build();
    }

    interface ObjectBuilder extends Builder {
        ObjectBuilder append(String key, JsonElement element);
        ObjectBuilder append(String key, Builder builder);
    }

    interface ArrayBuilder extends Builder {
        ArrayBuilder append(JsonElement element);
        ArrayBuilder append(Builder builder);
    }

    ObjectBuilder objectBuilder();
    ArrayBuilder arrayBuilder();

    JsonElement string(String string);
    JsonElement number(Number number);
    JsonElement bool(boolean bool);
    JsonElement nil();
}
