package com.staim.lightjson;

import com.staim.lightjson.implementations.ElementImpl;

/**
 * Old Deprecated Json Element, renamed because of name conflict with JsonElement interface
 *
 * Created by alexeyshcherbinin on 05.12.14.
 */
@SuppressWarnings("UnusedDeclaration")
@Deprecated
public class OldJsonElement extends ElementImpl {
    public OldJsonElement(String jsonString) throws JsonException { super(jsonString); }
    public OldJsonElement(JsonType type) { super((type)); }
    public OldJsonElement(JsonType type, Object value) throws JsonException { super(type, value); }
}
