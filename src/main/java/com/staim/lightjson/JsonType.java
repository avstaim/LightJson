package com.staim.lightjson;

/**
 * Type of Json Element
 */
public enum JsonType {
    OBJECT,
    ARRAY,
    STRING,
    NUMBER,
    BOOLEAN,
    DATE, // serialization only
    NULL,
    ANY,  // serialization only
    RAW // parse only: return as JsonElement for special cases, which cannot be parsed to Java classes
}
