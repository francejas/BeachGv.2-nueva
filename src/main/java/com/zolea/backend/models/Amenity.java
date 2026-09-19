package com.zolea.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.NoArgsConstructor;

import java.util.List;

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
@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class Amenity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long idAmenity;

    private String name;

    @ManyToMany(mappedBy = "amenities")
    @ToString.Exclude
    private List<Resort> resorts;
}