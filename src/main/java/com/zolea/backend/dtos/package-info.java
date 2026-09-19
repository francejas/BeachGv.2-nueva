/**
 * Los objetos que viajan por HTTP, uno de entrada y uno de salida por agregado.
 *
 * <p>Son {@code record}: inmutables y sin lógica. Existen para tres cosas concretas, no por estilo:
 * que no se filtren campos que la entidad tiene y la API no debe publicar —{@code passwordHash},
 * {@code qrToken}—, que las relaciones bidireccionales no se serialicen en círculo, y que el JSON
 * sea un contrato estable que no cambie cada vez que se renombra un campo de una entidad.
 *
 * <p>Las restricciones de Bean Validation de los DTO de entrada cubren la <b>forma</b> del pedido
 * (qué es obligatorio, qué formato, qué signo). Las reglas del negocio están en las entidades: si se
 * escribieran también acá, la misma regla quedaría en dos lugares.
 */
package com.zolea.backend.dtos;
