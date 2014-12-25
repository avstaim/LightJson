package com.staim.lightjson;

/**
 * Json Unmarshaller Interface
 *
 * Created by alexeyshcherbinin on 03.12.14.
 */
public interface JsonUnmarshaller {
    /**
     * Unmarshall Json String into java class.
     *
     * @param unmarshalClass - class reference for unmarshalling type safety
     * @param <T> - type of unmarshalling object
     * @return Java class, unmarshalled from Json.
     * @throws JsonException on parsing or unmarshalling error
     */
    public <T> T unmarshal(Class<T> unmarshalClass) throws JsonException;

    /**
     * Get raw Json Element from parsed Json Structure.
     *
     * @return Resulting Json Element from parsed Json String, used for unmarshalling, or null when parsing was unsuccessful.
     */
    public JsonElement getElement();
}
