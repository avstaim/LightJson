package com.staim.lightjson.implementations.elements;

import com.staim.lightjson.JsonElement;
import com.staim.lightjson.JsonType;

/**
 * Json String Element
 *
 * Created by alexeyshcherbinin on 04.12.14.
 */
public class JsonNumberElement extends JsonAbstractElement implements JsonElement {
    private static final JsonType type = JsonType.NUMBER;
    private Number data;

    public JsonNumberElement(Number number) { data = number; }

    @Override public JsonType getType() { return type; }
    @Override public Number getNumberData() { return data; }
    @Override public Object getObjectData() { return data; }
}
