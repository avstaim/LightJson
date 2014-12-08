package com.staim.lightjson.implementations.parsers;

import com.staim.lightjson.JsonElement;
import com.staim.lightjson.JsonException;
import com.staim.lightjson.JsonParser;
import com.staim.lightjson.implementations.ElementImpl;

/**
 * Old good simple parser
 *
 * Created by alexeyshcherbinin on 03.12.14.
 */
public class ParserSimple implements JsonParser {
    @Override
    public JsonElement parse(String json) throws JsonException {
        return new ElementImpl(json);
    }
}
