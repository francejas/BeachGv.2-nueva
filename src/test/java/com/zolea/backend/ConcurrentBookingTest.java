package com.zolea.backend;

import com.zolea.backend.dtos.booking.BookingRequest;
import com.zolea.backend.models.Client;
import com.zolea.backend.models.RentalUnit;
import com.zolea.backend.models.Resort;
import com.zolea.backend.models.UnitType;
import com.zolea.backend.repositories.BookingRepository;
import com.zolea.backend.repositories.ClientRepository;
import com.zolea.backend.repositories.GuestRepository;
import com.zolea.backend.repositories.RentalUnitRepository;
import com.zolea.backend.repositories.ResortRepository;
import com.zolea.backend.services.BookingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Dos personas reservando la misma unidad, al mismo tiempo.
 *
 * <p>El sistema preguntaba «¿está libre?» y después guardaba, sin nada que impidiera que otro se
 * metiera entre las dos operaciones. Con dos peticiones simultáneas las dos leían «libre» y las dos
 * guardaban: la unidad quedaba reservada dos veces para las mismas fechas.
 *
 * <p><b>Esta prueba tiene que ser concurrente de verdad.</b> Una que llame a {@code createBooking}
 * dos veces seguidas pasa incluso con el error presente, porque la segunda llamada ya ve la reserva
 * de la primera: no probaría nada. Por eso hay dos hilos y un {@link CountDownLatch} que los larga
 * a la vez.
 *
 * <p>No hereda de {@code IntegrationTest} porque esa clase base envuelve cada prueba en una
 * transacción que se revierte al terminar. Acá hace falta lo contrario: cada hilo necesita su
 * propia transacción y sus escrituras tienen que ser visibles para el otro, así que los datos se
 * limpian a mano.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Concurrencia — una unidad, dos personas a la vez")
class ConcurrentBookingTest {

    @Autowired private BookingService bookingService;
    @Autowired private ClientRepository clientRepository;
    @Autowired private ResortRepository resortRepository;
    @Autowired private RentalUnitRepository rentalUnitRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private GuestRepository guestRepository;
    @Autowired private Clock clock;

    private static final AtomicInteger SEQ = new AtomicInteger(9000);

    @AfterEach
    void cleanUp() {
        guestRepository.deleteAll();
        bookingRepository.deleteAll();
        rentalUnitRepository.deleteAll();
        clientRepository.deleteAll();
        resortRepository.deleteAll();
    }

    @Test
    @DisplayName("dos reservas simultáneas sobre la misma unidad dejan una sola")
    void twoSimultaneousBookings_onlyOneSurvives() throws Exception {
        Client uno = aClient();
        Client dos = aClient();
        RentalUnit unidad = aUnit();
        LocalDate desde = LocalDate.now(clock).plusDays(30);
        LocalDate hasta = desde.plusDays(3);

        CountDownLatch largada = new CountDownLatch(1);
        CountDownLatch terminaron = new CountDownLatch(2);
        AtomicInteger exitos = new AtomicInteger();
        AtomicInteger rechazos = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(2);
        for (Client quien : List.of(uno, dos)) {
            pool.submit(() -> {
                try {
                    largada.await(); // los dos hilos salen en el mismo instante
                    bookingService.createBooking(new BookingRequest(
                            desde, hasta, quien.getIdClient(), unidad.getIdRentalUnit(),
                            List.of(), null, null));
                    exitos.incrementAndGet();
                } catch (Exception e) {
                    rechazos.incrementAndGet();
                } finally {
                    terminaron.countDown();
                }
            });
        }

        largada.countDown();
        assertThat(terminaron.await(30, TimeUnit.SECONDS))
                .withFailMessage("los hilos no terminaron: probable interbloqueo")
                .isTrue();
        pool.shutdown();

        assertThat(bookingRepository.count())
                .withFailMessage("la unidad quedó reservada dos veces para las mismas fechas")
                .isEqualTo(1);
        assertThat(exitos.get()).isEqualTo(1);
        assertThat(rechazos.get()).isEqualTo(1);
    }

    // ---------------------------------------------------------------------

    private Client aClient() {
        int n = SEQ.incrementAndGet();
        Client client = new Client();
        client.setFirstName("Cliente");
        client.setLastName("Concurrente" + n);
        client.setEmail("concurrente" + n + "@test.zolea");
        client.setPasswordHash("$2b$10$hash.de.prueba.no.se.verifica.nunca.aaaaaaaaaaaaaaaaaaaaaa");
        client.setPhone("11" + n + "0000");
        client.setDni("50" + n + "00");
        client.setRole("USER");
        return clientRepository.save(client);
    }

    private RentalUnit aUnit() {
        int n = SEQ.incrementAndGet();
        Resort resort = new Resort();
        resort.setName("Balneario concurrente " + n);
        resort.setLocation("Mar del Plata");
        resort.setAdminEmail("admin.concurrente" + n + "@test.zolea");
        resort.setActive(true);
        resort = resortRepository.save(resort);

        RentalUnit unit = new RentalUnit();
        unit.setType(UnitType.UMBRELLA);
        unit.setIdentifier("C-" + n);
        unit.setDailyPrice(new BigDecimal("10000.00"));
        unit.setIsBlocked(false);
        unit.setResort(resort);
        return rentalUnitRepository.save(unit);
    }
}
