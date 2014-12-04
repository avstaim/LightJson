package com.staim.lightjson.implementations.elements;

import com.staim.lightjson.JsonElement;
import com.staim.lightjson.JsonException;
import com.staim.lightjson.JsonType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Json Array Element
 *
 * Created by alexeyshcherbinin on 04.12.14.
 */
public class JsonArray extends JsonAbstractElement implements JsonElement {
    private static final JsonType type = JsonType.ARRAY;
    private List<JsonElement> data;

    //////////////////////////// CONSTRUCTORS //////////////////////////////////////////////////////////////////////////

    /**
     * Create empty JSON Object
     */
    public JsonArray() { data = new ArrayList<>(); }

    /**
     * Create JSON Element of given type with initializer
     * @param data  - Object Data
     */
    public JsonArray(Collection<JsonElement> data) { this.data = new ArrayList<>(data); }

    //////////////////////////// INTERFACE /////////////////////////////////////////////////////////////////////////////

    //// Getters ////

    @Override public JsonType getType() { return type; }
    @Override public Object getData() { return this.data; }

    @Override
    public JsonElement get(int index) throws JsonException {
        if (data == null) throw new JsonException("Data is missing");
        return data.get(index);
    }

    @Override
    public Iterator<JsonElement> iterator() throws JsonException {
        if (data == null) throw new JsonException("Data is missing");
        return data.iterator();
    }

    @Override
    public int size() throws JsonException {
        if (data == null) throw new JsonException("Data is missing");
        return data.size();
    }

    //// Adders ////

    @Override
    public void add(Object object) throws JsonException {
        if (data == null) throw new JsonException("Data is missing");
        data.add(getJsonElementFromObject(object));
    }
}
