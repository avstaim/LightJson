package com.staim.lightjson.implementations.elements;

import com.staim.lightjson.JsonElement;
import com.staim.lightjson.JsonException;
import com.staim.lightjson.JsonType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Json Object Element
 *
 */
@SuppressWarnings("UnusedDeclaration")
public class JsonObjectElement extends JsonAbstractElement implements JsonElement {
    private static final JsonType type = JsonType.OBJECT;
    private Map<String, JsonElement> data;

    //////////////////////////// CONSTRUCTORS //////////////////////////////////////////////////////////////////////////

    /**
     * Create empty JSON Object
     */
    public JsonObjectElement() { data = new HashMap<>(); }

    /**
     * Create JSON Element of given type with initializer
     * @param data  - Object Data
     */
    public JsonObjectElement(Map<String, JsonElement> data) { this.data = data; }

    //////////////////////////// INTERFACE /////////////////////////////////////////////////////////////////////////////

    //// Getters ////

    @Override public JsonType getType() { return type; }
    @Override public Object getObjectData() { return this.data; }

    @Override
    public JsonElement get(String name) throws JsonException {
        if (data == null) throw new JsonException("Data is missing");
        return data.get(name);
    }

    @Override
    public Iterator<JsonElement> iterator() throws JsonException {
        if (data == null) throw new JsonException("Data is missing");
        return data.values().iterator();
    }

    @Override
    public int size() throws JsonException {
        if (data == null) throw new JsonException("Data is missing");
        return data.size();
    }

    //// Adders ////

    @Override
    public void add(String name, Object object) throws JsonException {
        if (data == null) throw new JsonException("Data is missing");
        data.put(name, getJsonElementFromObject(object));
    }
}
