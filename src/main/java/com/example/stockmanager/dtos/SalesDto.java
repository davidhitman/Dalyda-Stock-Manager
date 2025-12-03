package com.example.stockmanager.dtos;

import com.example.stockmanager.entities.ContainerWeights;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

public class SalesDto {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    // DTO used to add Sales
    public static class AddSalesDto {
        @JsonFormat(pattern = "yyyy-MM-dd")
        @NotNull(message = "Date Cannot be blank")
        private LocalDate date;
        private String code;
        @NotBlank(message="Name cannot be blank")
        private String name;
        @NotNull(message="Quantity cannot be blank")
        private Integer quantity;
        @NotNull(message="Price cannot be blank")
        private double price;
        @NotNull(message="Total Price cannot be blank")
        private double totalPrice;
        @NotNull(message="Weight cannot be blank")
        private ContainerWeights weight;
        @NotBlank(message="containerName cannot be blank")
        private String containerName;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ViewSalesDto{
        private UUID id;
        private LocalDate date;
        private String code;
        private String name;
        private Integer quantity;
        private double price;
        private double total;
        private ContainerWeights weight;
        private String containerName;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateSalesDto {
        @Positive(message = "Quantity must be greater than zero")
        private Integer quantity;
        @PositiveOrZero(message = "Price must be zero or greater")
        private Double price;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesDateDto {

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;

        @Min(value = 0, message = "Page index must be zero or greater")
        private Integer page;

        @Min(value = 1, message = "Page size must be at least 1")
        private Integer size;

        public PageDto toPageDto() {
            return new PageDto(page, size);
        }
    }
}
