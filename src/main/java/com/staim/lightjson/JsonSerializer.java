package com.staim.lightjson;

/**
 * Json Serializer Interface
 *
 * Created by alexeyshcherbinin on 05.12.14.
 */
public interface JsonSerializer {
    /**
     * Serialize Json Element to String
     * @param element Json Element
     * @return Json String
     */
    String serialize(JsonElement element);
}
