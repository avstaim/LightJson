package com.staim.lightjson.annotations;

import com.staim.lightjson.JsonType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark Java Bean method as setter for Json Object
 *
 * Created by a_scherbinin on 17.04.14.
 */

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonSetter {
    JsonType type() default JsonType.ANY;
    String name() default "";
}
