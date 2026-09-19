/**
 * La capa HTTP: traduce pedidos y respuestas, y no decide nada del negocio.
 *
 * <p>Cada controller recibe un DTO, llama a un servicio y devuelve otro DTO. Las reglas de negocio
 * viven en las entidades y en los servicios; los permisos finos, en las anotaciones
 * {@code @PreAuthorize} de cada método, que es donde se pueden expresar («¿esta reserva es tuya?»).
 *
 * <p>Los tres retornos de MercadoPago y el aviso automático son <b>públicos</b>, y tienen que serlo:
 * quien los llama no tiene el token de la aplicación. No le creen a lo que les llega — consultan el
 * pago contra la pasarela antes de tocar nada.
 */
package com.zolea.backend.controllers;
