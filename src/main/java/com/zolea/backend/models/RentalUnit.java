package com.zolea.backend.models;

import jakarta.persistence.*;

import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * <p><b>Sobre {@code equals}, {@code hashCode} y {@code toString}:</b> los tres se limitan al
 * identificador. {@code @Data} de Lombok los genera con <b>todos</b> los campos, y sobre relaciones
 * que apuntan en los dos sentidos eso es una recursión infinita — imprimir una entidad tiraba la
 * aplicación con {@code StackOverflowError}. La identidad de una entidad es su fila, no el
 * contenido de sus campos.
 *
 * <p>Queda una limitación conocida: el identificador lo asigna la base al guardar, así que el
 * {@code hashCode} de una entidad cambia entre antes y después de persistirla. Por eso una entidad
 * sin guardar no debe meterse en un {@code HashSet} ni usarse como clave de un {@code HashMap}.
 */
// Dos unidades del mismo balneario no pueden compartir identificador. Entre balnearios sí: la
// carpa "A-1" de Mar del Plata y la "A-1" de Pinamar son unidades distintas.
@Table(name = "rental_unit", uniqueConstraints =
        @UniqueConstraint(name = "uk_unidad_por_balneario", columnNames = {"id_resort", "identifier"}))
@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RentalUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long idRentalUnit;

    @Enumerated(EnumType.STRING)
    private UnitType type;

    private String identifier;

    @Column(precision = 12, scale = 2)
    private BigDecimal dailyPrice;

    private Boolean isBlocked;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_resort")
    @ToString.Exclude
    private Resort resort;
}