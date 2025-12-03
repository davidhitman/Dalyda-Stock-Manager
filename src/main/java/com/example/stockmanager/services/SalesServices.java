package com.example.stockmanager.services;

import com.example.stockmanager.dtos.PageDto;
import com.example.stockmanager.dtos.SalesDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface SalesServices {

    SalesDto.ViewSalesDto addSales(SalesDto.AddSalesDto salesDto);
    List<SalesDto.ViewSalesDto> recentSales();
    Page<SalesDto.ViewSalesDto> viewSales(PageDto pageDto);
    Page<SalesDto.ViewSalesDto> viewSalesFiltered(SalesDto.SalesDateDto dateDto);
    String getItemName (String articleCode);
    void deleteSales(UUID id);
    SalesDto.ViewSalesDto updateSale(UUID id, SalesDto.UpdateSalesDto updateDto);
}
