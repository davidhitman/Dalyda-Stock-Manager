package com.example.stockmanager.mappers;

import com.example.stockmanager.dtos.StockDto;
import com.example.stockmanager.entities.Stock;

public class StockMapper {
    public static Stock map (StockDto.AddStockDto stockDto) {
        return new Stock(stockDto.getItem_code(), stockDto.getItem_name(), stockDto.getQuantity(), stockDto.getContainer_name());
    }

    public static StockDto.ViewStockDto map (Stock stock) {
        return new StockDto.ViewStockDto(stock.getId(), stock.getCode(), stock.getName(), stock.getQuantity(), stock.getContainer_name(), stock.getWeight());
    }
}
