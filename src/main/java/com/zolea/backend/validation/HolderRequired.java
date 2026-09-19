package com.zolea.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Una reserva tiene que tener titular: o una cuenta registrada, o un nombre de mostrador.
 *
 * <p>Es una restricción de <b>toda la clase</b> y no de un campo, y esa es justamente la razón de
 * que exista. La opción natural —un {@code @NotNull @Positive} sobre {@code clientId}— rompería la
 * reserva de mostrador, que llega con {@code clientId} en cero y el titular en {@code walkInName}.
 * Ninguno de los dos campos es obligatorio por separado; lo que es obligatorio es que haya uno.
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = HolderRequiredValidator.class)
public @interface HolderRequired {

    String message() default "La reserva necesita un cliente registrado o un nombre de mostrador.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
