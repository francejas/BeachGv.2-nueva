package com.zolea.backend.models;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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
public class Resort {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long idResort;

    private String name;

    private String location;

    @Column(unique = true)
    private String adminEmail;

    private String coverPhotoUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Las dos colecciones van con BatchSize y no con @EntityGraph, y es a propósito: traer dos
    // listas en la misma consulta produce un producto cartesiano, y Hibernate directamente lo
    // rechaza con MultipleBagFetchException. Con BatchSize, listar cinco balnearios son tres
    // consultas —los balnearios, todas sus unidades, todas sus amenidades— en vez de once.
    @OneToMany(mappedBy = "resort")
    @BatchSize(size = 25)
    @ToString.Exclude
    private List<RentalUnit> rentalUnits;

    @ManyToMany
    @BatchSize(size = 25)
    @ToString.Exclude
    private List<Amenity> amenities;

    private boolean isActive;
}