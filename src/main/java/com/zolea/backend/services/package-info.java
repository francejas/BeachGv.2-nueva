/**
 * Los casos de uso: orquestan entidades y repositorios, y declaran las transacciones.
 *
 * <p>Cada servicio es {@code @Transactional(readOnly = true)} a nivel de clase, con
 * {@code @Transactional} explícito en los métodos que escriben. Eso no es ceremonia: en modo lectura
 * Hibernate no vuelca los cambios, pero la entidad en memoria sí queda modificada, así que un método
 * de escritura mal marcado devuelve el valor nuevo en la respuesta y deja el viejo en la base. El
 * error recién aparecería al recargar la página, y por eso las pruebas de escritura releen el dato
 * de la base en lugar de mirar la respuesta.
 *
 * <p>{@link com.zolea.backend.services.BookingCheckoutService} es el caso de uso del cobro. Antes
 * vivía dentro del controller, es decir: no existía fuera de HTTP y no se podía probar sin él.
 */
package com.zolea.backend.services;
