/**
 * El acceso a datos, con Spring Data JPA.
 *
 * <p>Tres cosas de acá no son decorativas y conviene no quitarlas sin entenderlas:
 * <ul>
 *   <li>{@code findByIdForUpdate} toma un candado de escritura sobre la unidad y es lo que impide
 *       que dos personas reserven la misma carpa al mismo tiempo.</li>
 *   <li>Las anotaciones {@code @EntityGraph} de las consultas de lectura son lo que bajó el listado
 *       de reservas de 33 consultas a 3. Hay una prueba que cuenta las consultas reales y falla si
 *       vuelven a subir.</li>
 *   <li>{@code isUnitAvailable} usa el mismo criterio de fechas inclusivo que el cálculo del precio.
 *       Cambiar uno sin el otro es el error que hacía que una reserva cobrara un día y bloqueara
 *       dos.</li>
 * </ul>
 */
package com.zolea.backend.repositories;
