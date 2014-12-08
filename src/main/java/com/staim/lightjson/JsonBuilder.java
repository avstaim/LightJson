package com.staim.lightjson;

/**
 * Json Builder
 *
 * Used to create JsonElements
 *
 * Created by alexeyshcherbinin on 03.12.14.
 */
@SuppressWarnings("UnusedDeclaration")
public interface JsonBuilder {
    /**
     * Abstract Builder
     */
    interface Builder {
        JsonElement build();
    }

    /**
     * Object {} builder
     */
    interface ObjectBuilder extends Builder {
        ObjectBuilder append(String key, JsonElement element);
        ObjectBuilder append(String key, Builder builder);
    }

    /**
     * Array [] Builder
     */
    interface ArrayBuilder extends Builder {
        ArrayBuilder append(JsonElement element);
        ArrayBuilder append(Builder builder);
    }

    /**
     * Get Object Builder to build Object {} Json Element
     * @return ObjectBuilder instance
     */
    ObjectBuilder objectBuilder();

    /**
     * Get Array Builder to build Array [] Json Element
     * @return ArrayBuilder instance
     */
    ArrayBuilder arrayBuilder();

    /**
     * Create String Json Element
     * @param string - string to wrap into Json Element
     * @return JsonElement
     */
    JsonElement string(String string);

    /**
     * Create Number Json Element
     * @param number - number to wrap into Json Element
     * @return JsonElement
     */
    JsonElement number(Number number);

    /**
     * Create Boolean Json Element
     * @param bool - boolean to wrap into Json Element
     * @return JsonElement
     */
    JsonElement bool(boolean bool);

    /**
     * Create Plain Json Element with automatic type detection
     * @param object - object to wrap into Json Element. Supported types: String, Number, Boolean or Date.
     *                  Other objects will be added as null.
     * @return JsonElement
     */
    <T> JsonElement auto(T object);

    /**
     * Create Null Json Element
     * @return Null Json Element
     */
    JsonElement nil();
}
