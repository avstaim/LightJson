package com.staim.lightjson;

/**
 * Json Unmarshaller Interface
 *
 * Created by alexeyshcherbinin on 03.12.14.
 */
public interface JsonUnmarshaller {
    public <T> T unmarshal(Class<T> unmarshalClass) throws JsonException;
    public JsonElement getElement();
}
