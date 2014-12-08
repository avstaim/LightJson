package com.staim.lightjson.implementations.elements;

import com.staim.lightjson.JsonElement;
import com.staim.lightjson.JsonType;

/**
 * Json Element for Plain Data Types: String, Number, Boolean, Date
 *
 * Created by alexeyshcherbinin on 08.12.14.
 */
public class JsonPlainElement<T> extends JsonAbstractElement implements JsonElement {
    private final JsonType _type;
    private T _data;

    public JsonPlainElement(T data, JsonType type) { _data = data; _type = type; }
    public JsonPlainElement(T data) { _data = data; _type = getType(data); }

    @Override public JsonType getType() { return _type; }
    @Override public Object getObjectData() { return _data; }
}
