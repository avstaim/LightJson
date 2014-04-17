package com.staim.lightjson;

/**
 * Interface for JSON-convertible data objects.
 *
 */
public interface ConvertibleToJson {
    /**
     * @return JSON representation of object
     * @throws JsonException
     */
    public JsonElement toJSON() throws JsonException;
}
