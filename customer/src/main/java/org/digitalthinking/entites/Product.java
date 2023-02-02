package org.digitalthinking.entites;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class Product extends PanacheEntity {

    @Transient
    private Long Id;
    @ManyToOne
    @JoinColumn(name = "customer", referencedColumnName = "id")
    @JsonBackReference
    private Customer customer;
    @Column
    private Long product;
    @Transient
    private String name;
    @Transient
    private String code;
    @Transient
    private String description;
}
