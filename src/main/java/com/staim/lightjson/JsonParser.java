package com.staim.lightjson;

/**
 * Json Parser
 * Created by alexeyshcherbinin on 03.12.14.
 */
public interface JsonParser {
    JsonElement parse(String json) throws JsonException;
}
