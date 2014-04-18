package com.staim.lightjson;

/**
 * Main Json interface providing required functionality for marshalling and serialization as well as reverse operations
 * Created by a_scherbinin on 18.04.14.
 */
public interface Json<T> {
    public T unmarshal();
    public String marshal();
    public JsonElement getElement();
}
