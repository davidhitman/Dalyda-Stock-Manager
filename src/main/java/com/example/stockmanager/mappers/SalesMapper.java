package com.example.stockmanager.mappers;

import com.example.stockmanager.dtos.SalesDto;
import com.example.stockmanager.entities.Sales;

public class SalesMapper {

    public static Sales map (SalesDto.AddSalesDto salesDto) {
        return new Sales(salesDto.getDate(), salesDto.getCode(), salesDto.getName(), salesDto.getContainerName(), salesDto.getQuantity(), salesDto.getPrice(), salesDto.getTotalPrice(), salesDto.getWeight());
    }

    public static SalesDto.ViewSalesDto map (Sales sales) {
        return new SalesDto.ViewSalesDto(sales.getId(), sales.getDate(), sales.getCode(), sales.getName(), sales.getQuantity(), sales.getPrice(), sales.getTotalPrice(), sales.getWeight(), sales.getContainerName());
    }

}
