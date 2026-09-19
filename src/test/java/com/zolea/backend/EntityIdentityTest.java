package com.zolea.backend;

import com.zolea.backend.models.Amenity;
import com.zolea.backend.models.Booking;
import com.zolea.backend.models.Client;
import com.zolea.backend.models.Guest;
import com.zolea.backend.models.Resort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Las entidades no se llaman en círculo entre ellas.
 *
 * <p>Las seis usan {@code @Data} de Lombok, que genera {@code equals}, {@code hashCode} y
 * {@code toString} incluyendo <b>todos</b> los campos. Sobre relaciones que apuntan en los dos
 * sentidos —{@code Booking}↔{@code Client}, {@code Booking}↔{@code Guest},
 * {@code Resort}↔{@code Amenity}— eso es una recursión infinita: el {@code toString} de la reserva
 * imprime el cliente, el del cliente imprime sus reservas, y así hasta que la pila se acaba.
 *
 * <p>No explotaba porque nada del sistema imprime una entidad. Basta agregar un
 * {@code log.debug("{}", booking)} para tirar la aplicación abajo, y eso es exactamente lo que se
 * suele agregar cuando algo falla en producción.
 *
 * <p>Es una prueba de unidad: son objetos comunes, no hace falta base ni Spring.
 */
@DisplayName("Entidades — sin recursión entre relaciones")
class EntityIdentityTest {

    /** Una reserva y su cliente, apuntándose mutuamente, como quedan al cargarse de la base. */
    private Booking bookingWithClient() {
        Client client = new Client();
        client.setIdClient(1L);
        client.setFirstName("Ana");
        client.setLastName("Pérez");

        Booking booking = new Booking();
        booking.setId(10L);
        booking.setClient(client);
        client.setBookings(new ArrayList<>(List.of(booking)));

        Guest guest = new Guest();
        guest.setIdGuest(100L);
        guest.setBooking(booking);
        booking.setGuests(new ArrayList<>(List.of(guest)));

        return booking;
    }

    @Test
    @DisplayName("imprimir una reserva no desborda la pila")
    void bookingToString_isNotRecursive() {
        Booking booking = bookingWithClient();

        assertThatCode(booking::toString).doesNotThrowAnyException();
        assertThatCode(() -> booking.getClient().toString()).doesNotThrowAnyException();
        assertThatCode(() -> booking.getGuests().getFirst().toString()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("imprimir un balneario y sus amenidades no desborda la pila")
    void resortToString_isNotRecursive() {
        Resort resort = new Resort();
        resort.setIdResort(1L);
        resort.setName("Zolea");

        Amenity amenity = new Amenity();
        amenity.setIdAmenity(5L);
        amenity.setName("Pileta");

        resort.setAmenities(new ArrayList<>(List.of(amenity)));
        amenity.setResorts(new ArrayList<>(List.of(resort)));

        assertThatCode(resort::toString).doesNotThrowAnyException();
        assertThatCode(amenity::toString).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("comparar dos reservas no desborda la pila")
    void bookingEquals_isNotRecursive() {
        Booking una = bookingWithClient();
        Booking otra = bookingWithClient();

        assertThatCode(() -> una.equals(otra)).doesNotThrowAnyException();
        assertThatCode(una::hashCode).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("dos instancias con el mismo id son la misma entidad")
    void sameIdentity_areEqual() {
        Booking una = new Booking();
        una.setId(7L);
        una.setTotalPrice(new java.math.BigDecimal("100.00"));

        Booking otra = new Booking();
        otra.setId(7L);
        otra.setTotalPrice(new java.math.BigDecimal("999.00"));

        // La identidad de una entidad es su id, no el contenido de sus campos: la misma fila leída
        // dos veces, o leída antes y después de un cambio, sigue siendo la misma reserva.
        assertThat(una).isEqualTo(otra);
        assertThat(una.hashCode()).isEqualTo(otra.hashCode());
    }
}
