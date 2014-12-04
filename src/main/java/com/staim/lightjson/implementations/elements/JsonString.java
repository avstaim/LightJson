package com.staim.lightjson.implementations.elements;

import com.staim.lightjson.JsonElement;
import com.staim.lightjson.JsonType;

/**
 * Json String Element
 *
 * Created by alexeyshcherbinin on 04.12.14.
 */
public class JsonString extends JsonAbstractElement implements JsonElement {
    private static final JsonType type = JsonType.STRING;
    private String data;

    public JsonString(String string) { data = string; }

    @Override public JsonType getType() { return type; }
    @Override public String getStringData() { return data; }
    @Override public Object getData() { return data; }
}
