package com.staim.lightjson.implementations.elements;

import com.staim.lightjson.JsonElement;
import com.staim.lightjson.JsonType;

import java.util.Date;

/**
 * Json String Element
 *
 * Created by alexeyshcherbinin on 04.12.14.
 */
public class JsonDate extends JsonAbstractElement implements JsonElement {
    private static final JsonType type = JsonType.DATE;
    private Date data;

    public JsonDate(Date date) { data = date; }

    @Override public JsonType getType() { return type; }
    @Override public Date getDateData() { return data; }
    @Override public Object getData() { return data; }
}
