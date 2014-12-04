package com.staim.lightjson.implementations.elements;

import com.staim.lightjson.JsonElement;
import com.staim.lightjson.JsonType;

/**
 * Json String Element
 *
 * Created by alexeyshcherbinin on 04.12.14.
 */
public class JsonBoolean extends JsonAbstractElement implements JsonElement {
    private static final JsonType type = JsonType.BOOLEAN;
    private Boolean data;

    public JsonBoolean(boolean bool) { data = bool; }

    @Override public JsonType getType() { return type; }
    @Override public Boolean getBooleanData() { return data; }
    @Override public Object getData() { return data; }
}
