package com.staim.lightjson;

/**
 * Type of Json Element
 */
public enum JsonType {
    OBJECT, // Json Object {}
    ARRAY, // Json Array []
    STRING, // Json String ""
    NUMBER, // Json Number 0, 1, 256, -34556, 43.6753, ...
    BOOLEAN, // Json Boolean true or false
    DATE, // Date
    NULL, // null
    ANY,  // serialization only
    RAW // parse only: return as JsonElement for special cases, which cannot be parsed to Java classes
}
