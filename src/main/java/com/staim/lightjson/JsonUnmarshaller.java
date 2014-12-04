package com.staim.lightjson;

/**
 * Created by alexeyshcherbinin on 03.12.14.
 */
public interface JsonUnmarshaller {
    public <T> T unmarshal(Class<T> unmarshalClass) throws JsonException;
    public JsonElement getElement();
}
