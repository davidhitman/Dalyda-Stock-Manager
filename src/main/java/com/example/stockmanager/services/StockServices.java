package com.example.stockmanager.services;

import com.example.stockmanager.dtos.PageDto;
import com.example.stockmanager.dtos.StockDto;
import com.example.stockmanager.entities.ContainerWeights;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface StockServices {

    Integer getTotalStock ();
    Integer get75Stock ();
    Integer get45Stock();
    Integer getBagStock ();
    StockDto.ViewStockDto addStock (StockDto.AddStockDto stockDto, ContainerWeights weight);
    Page<StockDto.ViewStockDto> viewStockFilter (PageDto pageable, ContainerWeights weight, String containerName);
    List<String> findAllContainers();
    int uploadStockFile (MultipartFile file);
    StockDto.ViewStockDto updateStock(UUID id, StockDto.UpdateStockDto updateDto);
    void deleteStock(UUID id);
}
