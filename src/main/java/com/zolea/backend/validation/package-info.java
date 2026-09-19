/**
 * Restricciones de Bean Validation propias del proyecto.
 *
 * <p>Solo hay una, y existe por un motivo que vale entender: la regla «una reserva tiene que tener
 * titular» no se puede expresar campo por campo. Un {@code @NotNull @Positive} sobre el id del
 * cliente es lo natural y <b>rompe la reserva de mostrador</b>, que llega sin cliente y con el
 * titular cargado a mano. Por eso {@code @HolderRequired} es una restricción de <b>la clase
 * entera</b>: ninguno de los dos campos es obligatorio por separado; lo obligatorio es que haya uno.
 */
package com.zolea.backend.validation;
