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
    ANY  // serialization only
}
