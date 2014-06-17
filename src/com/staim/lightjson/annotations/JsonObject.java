package com.staim.lightjson.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JSON Object Annotation is used to mark Java Bean as json serializable for all it's getter and setter fields
 *
 * Created by a_scherbinin on 17.04.14.
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonObject {
    boolean AutomaticBinding() default false;
}
