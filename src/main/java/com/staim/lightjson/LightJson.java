package com.staim.lightjson;

import com.staim.lightjson.implementations.BuilderImpl;
import com.staim.lightjson.implementations.MarshallerImpl;
import com.staim.lightjson.implementations.UnmarshallerImpl;
import com.staim.lightjson.implementations.parsers.ParserScalable;
import com.staim.lightjson.implementations.parsers.ParserSimple;
import com.staim.lightjson.implementations.serializers.SerializerForkJoin;
import com.staim.lightjson.implementations.serializers.SerializerRecursive;

/**
 * Light Json
 *
 * Created by a_shcherbinin on 18.04.14.
 */
@SuppressWarnings({"unchecked", "UnusedDeclaration", "deprecation"})
public class LightJson<T> implements Json<T> {
    @Deprecated private JsonElement element;
    private static JsonBuilder builder;

    private static Class<? extends JsonParser> parserClass = ParserScalable.class;
    private static Class<? extends JsonSerializer> serializerClass = SerializerRecursive.class;

    private JsonParser parser;
    private JsonSerializer serializer;

    {{
        try {
            parser = parserClass.newInstance();
            serializer = serializerClass.newInstance();
        } catch (Exception e) { throw new RuntimeException(e); }
    }}

    public enum ParserType {
        Simple, // Good for small simple JSONs, bad for large and complex ones
        Scalable // Worse a bit for simple JSONs, but in long and complex up to 10x faster (default)
    }

    public enum SerializerType {
        SimpleRecursive, // Good for small simple JSONs, a bit worse for large and complex ones (default)
        ForkJoin // Better for large and complex JSONs, Very Bad on tons of simple JSONs.
    }

    public static void setParserType(ParserType type) {
        switch (type) {
            case Simple:
                parserClass = ParserSimple.class;
                break;
            case Scalable:
                parserClass = ParserScalable.class;
                break;
        }
    }

    public static void setSerializerType(SerializerType type) {
        switch (type) {
            case SimpleRecursive:
                serializerClass = SerializerRecursive.class;
                break;
            case ForkJoin:
                serializerClass = SerializerForkJoin.class;
                break;
        }
    }

    @Deprecated
    public LightJson(T t) throws JsonException {
        element = OldMarshaller.marshal(t);
    }

    @Deprecated
    public LightJson(String jsonString) throws JsonException {
        element = parser.parse(jsonString);
    }

    private LightJson() {}

    /**
     * Get Instance
     * @param <T> Type Of Object to marshalling/unmarshalling
     * @return Instance of Json Interface
     */
    public static <T> Json<T> json() { return new LightJson<>(); }

    @Override
    @Deprecated
    public T unmarshal(Class<T> unmarshalClass) throws JsonException {
        return (new UnmarshallerImpl(element).unmarshal(unmarshalClass));
    }

    @Override
    @Deprecated
    public String marshal() {
        return element.serialize();
    }

    @Override
    @Deprecated
    public JsonElement getElement() {
        return element;
    }

    @Override
    public JsonBuilder builder() {
        if (builder == null) builder = new BuilderImpl();
        return builder;
    }

    @Override
    public JsonMarshaller marshaller(T t) {
        return new MarshallerImpl(t, serializer);
    }

    @Override
    public JsonUnmarshaller unmarshaller(String jsonString) {
        return new UnmarshallerImpl(parser, jsonString);
    }

    @Override
    public JsonParser parser() {
        return parser;
    }

    @Override
    public JsonSerializer serializer() {
        return serializer;
    }

    /** fast syntax */

    public static JsonElement from(String jsonString) {
        try {
            return json().parser().parse(jsonString);
        } catch (JsonException e) {
            return null;
        }
    }

    public static <T> T from(String jsonString, Class<T> entityClass) {
        try {
            return json().unmarshaller(jsonString).unmarshal(entityClass);
        } catch (JsonException e) {
            return null;
        }
    }

    public static <T> String to(T t) {
        try {
            return json().marshaller(t).marshal();
        } catch (JsonException e) {
            return "";
        }
    }

    public static String to(JsonElement jsonElement) {
        return json().serializer().serialize(jsonElement);
    }
}
