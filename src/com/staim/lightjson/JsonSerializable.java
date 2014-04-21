package com.staim.lightjson;

/**
 * Interface for JSON-serializable data objects.
 *
 */
public interface JsonSerializable {
    /**
     * @return JSON representation of Object
     * @throws JsonException
     */
    public JsonElement toJSON() throws JsonException;
}
