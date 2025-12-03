package com.example.stockmanager.services.Impl;

import com.example.stockmanager.dtos.PageDto;
import com.example.stockmanager.dtos.StockDto;
import com.example.stockmanager.entities.Stock;
import com.example.stockmanager.entities.ContainerWeights;
import com.example.stockmanager.mappers.StockMapper;
import com.example.stockmanager.repositories.StockRepository;
import com.example.stockmanager.services.StockServices;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockServices {

    private final StockRepository stockRepository;
    private final PageServiceImpl pageService;
    @Override
    public Integer getTotalStock() {
        var stock = stockRepository.getTotalStock();
        if (stock == null) return 0;
        return stock;
    }

    @Override
    public Integer get75Stock() {
        var stock = stockRepository.get75KGTotalStock();
        if (stock == null) return 0;
        return stock;
    }

    @Override
    public Integer get45Stock() {
        var stock = stockRepository.get45KGTotalStock();
        if (stock == null) return 0;
        return stock;
    }

    @Override
    public Integer getBagStock() {
        var stock = stockRepository.getBagTotalStock();
        if (stock == null) return 0;
        return stock;
    }

    @Override
    public StockDto.ViewStockDto addStock(StockDto.AddStockDto stockDto, ContainerWeights weight) {
        var stock = StockMapper.map(stockDto);
        var presentProduct = stockRepository.findByProductNameAndWeightAndContainerName(
                stock.getName(), weight, stock.getContainer_name()
        );

        if (presentProduct.isPresent()) {
            Stock existing = presentProduct.get();
            existing.setQuantity(existing.getQuantity() + stock.getQuantity());
            Stock updated = stockRepository.save(existing);
            return StockMapper.map(updated);
        } else {
            stock.setWeight(weight);
            Stock saved = stockRepository.save(stock);
            return StockMapper.map(saved);
        }
    }

    @Override
    public Page<StockDto.ViewStockDto> viewStockFilter(PageDto pageDto, ContainerWeights weight, String containerName) {

        Pageable pageable = pageService.getPageable(pageDto);
        boolean hasWeight = (weight != null);
        boolean hasContainer = (containerName != null);

        Page<Stock> stock;

        if (hasWeight && hasContainer) {
            stock = stockRepository.findByWeightAndContainerName(pageable, weight, containerName);
        } else if (hasWeight) {
            stock = stockRepository.findByWeight(pageable, weight);
        } else if (hasContainer) {
            stock = stockRepository.findByContainerName(pageable, containerName);
        } else {
            stock = stockRepository.getAllStock(pageable);
        }
        if (stock.isEmpty()) throw new ResourceNotFoundException("Stock with selected criteria not found");
        return stock.map(StockMapper::map);
    }

    @Override
    public List<String> findAllContainers() {
        return stockRepository.getDistinctContainerName();
    }

    @Override
    public int uploadStockFile(MultipartFile file) {
        if (!ExcelUploadService.isValidExcelFile(file)) {
            throw new IllegalArgumentException("Please upload a .xlsx file that matches the Stock template.");
        }

        final List<Stock> parsedRows;
        try {
            parsedRows = ExcelUploadService.getStockDataFromExcel(file.getInputStream());
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read the uploaded file. Please try again.", e);
        }

        if (parsedRows.isEmpty()) {
            throw new IllegalArgumentException("The uploaded file does not contain any stock rows.");
        }

        Map<String, Stock> aggregated = new LinkedHashMap<>();
        for (Stock stock : parsedRows) {
            String key = stock.getName().toLowerCase(Locale.ROOT) + "|" +
                    stock.getWeight().name() + "|" +
                    stock.getContainer_name().toLowerCase(Locale.ROOT);

            aggregated.merge(key, stock, (existing, incoming) -> {
                existing.setQuantity(existing.getQuantity() + incoming.getQuantity());
                if ((existing.getCode() == null || existing.getCode().isBlank()) && incoming.getCode() != null) {
                    existing.setCode(incoming.getCode());
                }
                return existing;
            });
        }

        aggregated.values().forEach(newStock -> {
            var existingStock = stockRepository.findByProductNameAndWeightAndContainerName(
                    newStock.getName(), newStock.getWeight(), newStock.getContainer_name()
            );

            if (existingStock.isPresent()) {
                Stock existing = existingStock.get();
                existing.setQuantity(existing.getQuantity() + newStock.getQuantity());
                if ((existing.getCode() == null || existing.getCode().isBlank()) && newStock.getCode() != null) {
                    existing.setCode(newStock.getCode());
                }
                stockRepository.save(existing);
            } else {
                stockRepository.save(newStock);
            }
        });

        return aggregated.size();
    }

    @Override
    public StockDto.ViewStockDto updateStock(UUID id, StockDto.UpdateStockDto updateDto) {
        if (id == null) {
            throw new IllegalArgumentException("Stock id cannot be null.");
        }
        if (updateDto == null) {
            throw new IllegalArgumentException("Update payload cannot be null.");
        }

        var stock = stockRepository.findItemById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock item not found with id: " + id));

        // Update only fields that are provided (not null)
        if (updateDto.getItem_code() != null && !updateDto.getItem_code().isBlank()) {
            stock.setCode(updateDto.getItem_code());
        }
        if (updateDto.getItem_name() != null && !updateDto.getItem_name().isBlank()) {
            stock.setName(updateDto.getItem_name());
        }
        if (updateDto.getQuantity() != null) {
            if (updateDto.getQuantity() < 0) {
                throw new IllegalArgumentException("Quantity cannot be negative.");
            }
            stock.setQuantity(updateDto.getQuantity());
        }
        if (updateDto.getContainer_name() != null && !updateDto.getContainer_name().isBlank()) {
            stock.setContainer_name(updateDto.getContainer_name());
        }
        if (updateDto.getWeight() != null) {
            stock.setWeight(updateDto.getWeight());
        }

        stockRepository.save(stock);
        return StockMapper.map(stock);
    }

    @Override
    public void deleteStock(UUID id) {
        var item = stockRepository.findItemById(id);
        if (item.isEmpty()) throw new ResourceNotFoundException("Item not found");
        stockRepository.deleteById(id);
    }
}
