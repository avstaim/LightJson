package com.staim.lightjson;

/**
 * Main Json interface providing required functionality for marshalling and serialization as well as reverse operations
 * Created by a_shcherbinin on 18.04.14.
 */
@SuppressWarnings("UnusedDeclaration")
public interface Json<T> {
    @Deprecated
    public T unmarshal(Class<T> unmarshalClass) throws JsonException;
    @Deprecated
    public String marshal();
    @Deprecated
    public JsonElement getElement();

    public JsonBuilder builder();
    public JsonMarshaller marshaller(T t);
    public JsonUnmarshaller unmarshaller(String jsonString);

    public JsonParser parser();
    public JsonSerializer serializer();
}
