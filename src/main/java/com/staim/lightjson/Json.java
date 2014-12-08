package com.staim.lightjson;

/**
 * Main Json interface providing required functionality for marshalling and serialization as well as reverse operations
 *
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

    /**
     * Get Builder
     * @return JsonBuilder instance
     */
    public JsonBuilder builder();

    /**
     * Get Marshaller
     * @param t - Object to marshal
     * @return JsonMarshaller instance
     */
    public JsonMarshaller marshaller(T t);

    /**
     * Get Unmarshaller
     * @param jsonString - Json String to unmarshal
     * @return JsonUnmarshaller instance
     */
    public JsonUnmarshaller unmarshaller(String jsonString);

    /**
     * Get Parser
     * @return JsonParser instance
     */
    public JsonParser parser();

    /**
     * Get Serializer
     * @return JsonSerializer instance
     */
    public JsonSerializer serializer();
}
