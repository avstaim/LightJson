package com.staim.lightjson;

/**
 * Marshaller interface
 *
 * Created by alexeyshcherbinin on 03.12.14.
 */
public interface JsonMarshaller {
    /**
     * Marshal Java Object to Json
     * @return Json String
     * @throws JsonException when marshalling was not successful
     */
    public String marshal() throws JsonException;
}
