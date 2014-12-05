package com.staim.lightjson;

/**
 * Json Serializer Interface
 *
 * Created by alexeyshcherbinin on 05.12.14.
 */
public interface JsonSerializer {
    String serialize(JsonElement element);
}
