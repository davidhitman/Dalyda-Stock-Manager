package com.example.stockmanager.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name="stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Stock {
    @Id
    private UUID id = UUID.randomUUID();
    private String code;
    private String name;
    private Integer quantity;
    private String container_name;

    @Enumerated(EnumType.STRING)
    private ContainerWeights weight;

    public Stock(String code, String name, Integer quantity, String containerName) {
        this.code = code;
        this.name = name;
        this.quantity = quantity;
        this.container_name = containerName;
    }
}
