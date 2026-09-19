/**
 * Las entidades JPA, que son también donde viven las reglas del negocio.
 *
 * <p>No son bolsas de campos: {@link com.zolea.backend.models.Booking} sabe cuántos días ocupa,
 * cuánto sale, si su rango de fechas es válido y a qué estados puede pasar. Una regla escrita en un
 * solo lugar no se puede contradecir, que es exactamente lo que pasaba cuando el precio y la
 * disponibilidad contaban los días con criterios distintos.
 *
 * <p>Dos convenciones que conviene conocer antes de tocar nada:
 * <ul>
 *   <li><b>Fechas con fin inclusivo.</b> Del 10 al 12 son tres días, y son los mismos tres que la
 *       consulta de disponibilidad bloquea.</li>
 *   <li><b>El dinero es {@code BigDecimal}</b> con dos decimales. Nunca {@code double}.</li>
 * </ul>
 *
 * <p>{@code equals}, {@code hashCode} y {@code toString} se limitan al identificador: generados sobre
 * todos los campos, las relaciones bidireccionales los vuelven mutuamente recursivos.
 */
package com.zolea.backend.models;
