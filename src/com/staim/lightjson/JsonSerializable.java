package com.staim.lightjson;

/**
 * Interface for JSON-convertible data objects.
 *
 */
public interface JsonSerializable {
    /**
     * @return JSON representation of OBJECT
     * @throws JsonException
     */
    public JsonElement toJSON() throws JsonException;
}
