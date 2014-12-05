package com.staim.lightjson.implementations.elements;

import com.staim.lightjson.JsonElement;
import com.staim.lightjson.JsonType;

/**
 * Json String Element
 *
 * Created by alexeyshcherbinin on 04.12.14.
 */
public class JsonNullElement extends JsonAbstractElement implements JsonElement {
    private static final JsonType type = JsonType.NULL;

    @Override public JsonType getType() { return type; }
    @Override public Object getObjectData() { return null; }
}
