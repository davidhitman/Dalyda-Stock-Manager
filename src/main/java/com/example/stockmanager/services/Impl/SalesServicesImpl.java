package com.example.stockmanager.services.Impl;

import com.example.stockmanager.dtos.PageDto;
import com.example.stockmanager.dtos.SalesDto.SalesDateDto;
import com.example.stockmanager.dtos.SalesDto;
import com.example.stockmanager.entities.Stock;
import com.example.stockmanager.exceptions.InsufficientStockException;
import com.example.stockmanager.mappers.SalesMapper;
import com.example.stockmanager.repositories.SalesRepository;
import com.example.stockmanager.repositories.StockRepository;
import com.example.stockmanager.services.SalesServices;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SalesServicesImpl implements SalesServices {

    private final StockRepository stockRepository;
    private final SalesRepository salesRepository;
    private final PageServiceImpl pageService;
    @Override
    public SalesDto.ViewSalesDto addSales(SalesDto.AddSalesDto salesDto) {

        var sales = SalesMapper.map(salesDto);
        var presentProduct = stockRepository.findByProductNameAndWeightAndContainerName(
                sales.getName(), sales.getWeight(), sales.getContainerName()
        );
        if (presentProduct.isEmpty()) {
            throw new ResourceNotFoundException("You don't have such product in stock");
        }
        var existingStock = presentProduct.get();
        if (existingStock.getQuantity() < sales.getQuantity()) {
            throw new InsufficientStockException("Not enough items in stock");
        }
        salesRepository.save(sales);

        // update stock
        var newStockQuantity = existingStock.getQuantity() - sales.getQuantity();
        if (newStockQuantity == 0) {
            stockRepository.delete(existingStock);
        } else {
            existingStock.setQuantity(newStockQuantity);
            stockRepository.save(existingStock);
        }
        return SalesMapper.map(sales);
    }

    @Override
    public List<SalesDto.ViewSalesDto> recentSales() {
        var sales = salesRepository.getRecentSales();
        if (sales.isEmpty()) throw new ResourceNotFoundException("No sales data available");
        return sales.stream()
                .map(SalesMapper::map)
                .toList();
    }

    @Override
    public Page<SalesDto.ViewSalesDto> viewSales(PageDto pageDto) {
        var pageable = pageService.getPageable(pageDto);
        var sales = salesRepository.getSales(pageable);
        if (sales.isEmpty()) throw new ResourceNotFoundException("No sales data available.");
        return sales.map(SalesMapper::map);
    }

    @Override
    public Page<SalesDto.ViewSalesDto> viewSalesFiltered(SalesDateDto dateDto) {
        if (dateDto == null) {
            throw new IllegalArgumentException("Filter payload cannot be null.");
        }
        if (dateDto.getStartDate() != null && dateDto.getEndDate() != null &&
                dateDto.getEndDate().isBefore(dateDto.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }

        var pageable = pageService.getPageable(dateDto.toPageDto());
        var sales = salesRepository.findByDateRange(pageable, dateDto.getStartDate(), dateDto.getEndDate());
        if (sales.isEmpty()) throw new ResourceNotFoundException("No sales data available for the provided filters.");
        return sales.map(SalesMapper::map);
    }

    @Override
    public String getItemName(String articleCode) {
        var name = stockRepository.getArticleName(articleCode);
        if (name.isEmpty()) throw new ResourceNotFoundException("You don't have such product in stock");
        if (name.size() > 1) throw new DuplicateKeyException("There's more than one product with this code in store, please enter the code manually");
        return name.getFirst();
    }

    @Override
    public void deleteSales(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Sale id cannot be null.");
        }
        var sale = salesRepository.findSaleById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found"));

        var stock = stockRepository.findByProductNameAndWeightAndContainerName(
                sale.getName(), sale.getWeight(), sale.getContainerName()
        ).orElseThrow(() -> new ResourceNotFoundException("Matching stock entry not found for sale restoration"));

        stock.setQuantity(stock.getQuantity() + sale.getQuantity());
        stockRepository.save(stock);
        salesRepository.delete(sale);
    }

    @Override
    public SalesDto.ViewSalesDto updateSale(UUID id, SalesDto.UpdateSalesDto updateDto) {
        if (id == null) {
            throw new IllegalArgumentException("Sale id cannot be null.");
        }
        if (updateDto == null || (updateDto.getQuantity() == null && updateDto.getPrice() == null)) {
            throw new IllegalArgumentException("Provide at least a quantity or price update.");
        }

        var sale = salesRepository.findSaleById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found"));

        int currentQuantity = sale.getQuantity();
        double currentPrice = sale.getPrice();

        int newQuantity = updateDto.getQuantity() != null ? updateDto.getQuantity() : currentQuantity;
        double newPrice = updateDto.getPrice() != null ? updateDto.getPrice() : currentPrice;

        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        if (newPrice < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }

        int quantityDiff = newQuantity - currentQuantity;
        if (quantityDiff != 0) {
            var optionalStock = stockRepository.findByProductNameAndWeightAndContainerName(
                    sale.getName(), sale.getWeight(), sale.getContainerName()
            );

            if (quantityDiff > 0) {
                if (optionalStock.isEmpty()) {
                    throw new InsufficientStockException("Not enough stock to increase the sale quantity. Item not found in stock.");
                }
                var stock = optionalStock.get();
                if (stock.getQuantity() < quantityDiff) {
                    throw new InsufficientStockException("Not enough stock to increase the sale quantity. Available: " + stock.getQuantity() + ", Required: " + quantityDiff);
                }
                stock.setQuantity(stock.getQuantity() - quantityDiff);

                if (stock.getQuantity() == 0) {
                    stockRepository.delete(stock);
                } else {
                    stockRepository.save(stock);
                }
            } else {
                int quantityToAdd = Math.abs(quantityDiff);
                var stock = optionalStock.orElseGet(() -> {
                    var newStock = new Stock();
                    newStock.setCode(sale.getCode());
                    newStock.setName(sale.getName());
                    newStock.setQuantity(0);
                    newStock.setContainer_name(sale.getContainerName());
                    newStock.setWeight(sale.getWeight());
                    return stockRepository.save(newStock);
                });
                stock.setQuantity(stock.getQuantity() + quantityToAdd);
                stockRepository.save(stock);
            }
        }

        sale.setQuantity(newQuantity);
        sale.setPrice(newPrice);
        sale.setTotalPrice(newQuantity * newPrice);
        var updatedSale = salesRepository.save(sale);

        return SalesMapper.map(updatedSale);
    }
}
