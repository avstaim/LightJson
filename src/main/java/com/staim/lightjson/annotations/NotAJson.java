package com.staim.lightjson.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark Java Bean field never provide a JSON
 *
 * Created by a_scherbinin on 17.04.14.
 */


// TODO
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotAJson {}
