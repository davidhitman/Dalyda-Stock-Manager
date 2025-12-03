package com.example.stockmanager.dtos;

import com.example.stockmanager.entities.ContainerWeights;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

public class StockDto {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AddStockDto {

        private String item_code;

        @NotBlank(message = "Item name can not be blank")
        private String item_name;

        @NotNull(message="Quantity can not be black")
        private Integer quantity;

        @NotBlank(message="Container Name can not blank")
        private String container_name;

    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ViewStockDto {
        private UUID id;
        private String item_code;
        private String item_name;
        private Integer quantity;
        private String container_name;
        private ContainerWeights weight;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateStockDto {
        private String item_code;
        private String item_name;
        private Integer quantity;
        private String container_name;
        private ContainerWeights weight;
    }

}
