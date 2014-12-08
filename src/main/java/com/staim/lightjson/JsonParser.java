package com.staim.lightjson;

/**
 * Json Parser
 *
 * Created by alexeyshcherbinin on 03.12.14.
 */
public interface JsonParser {
    /**
     * Parse Json string to Json Element
     * @param json - json String
     * @return Json Element
     * @throws JsonException
     */
    JsonElement parse(String json) throws JsonException;
}
