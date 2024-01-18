package org.flyboy.belltower.panel.scene.navigation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author John J. Franey
 */
@Qualifier
@Retention(RUNTIME)
@Target(value = {METHOD, FIELD, PARAMETER, TYPE})
public @interface Belfry {

}