package com.example.stockmanager.controllers;

import com.example.stockmanager.dtos.PageDto;
import com.example.stockmanager.dtos.StockDto;
import com.example.stockmanager.entities.ContainerWeights;
import com.example.stockmanager.responses.GenericResponse;
import com.example.stockmanager.services.StockServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(name = "Stock Controller", description = "Handles Stock")
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/stock")
@PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
public class StockController {

    private final StockServices stockService;

    // total stock of all the bales
    @Operation(summary = "Total of Stock", description = "View the total number of Stock")
    @GetMapping("/total")
    public ResponseEntity<GenericResponse<Integer>> viewTotalStock() {
        var totalStock = stockService.getTotalStock();
        return ResponseEntity.status(HttpStatus.OK).body(new GenericResponse<>("This is the total Stock Number", totalStock));
    }

    // total stock of the 75KG Bales
    @Operation(summary = "75KG total stock", description = "View the total stock number of the 75KGs")
    @GetMapping("/75KG")
    public ResponseEntity<GenericResponse<Integer>> view75Stock() {
        var stock = stockService.get75Stock();
        return ResponseEntity.status(HttpStatus.OK).body(new GenericResponse<>("This is the total Stock Number for 75KG", stock));
    }

    // total Stock of the 45KG Bales
    @Operation(summary = "45KG total stock", description = "View the total stock number of the 45KGs")
    @GetMapping("/45KG")
    public ResponseEntity<GenericResponse<Integer>> view45Stock() {
        var stock = stockService.get45Stock();
        return ResponseEntity.status(HttpStatus.OK).body(new GenericResponse<>("This is the total Stock Number for 45KG", stock));
    }

    // total Bags of the Bales
    @Operation(summary = "Bags Stock", description = "View the total stock number of the bags")
    @GetMapping("/bags")
    public ResponseEntity<GenericResponse<Integer>> viewBagStock() {
        var stock = stockService.getBagStock();
        return ResponseEntity.status(HttpStatus.OK).body(new GenericResponse<>("This is the total Stock Number for Bags", stock));
    }

    @Operation(summary = "View Stock", description = "View all the stock, Item by Item")
    @GetMapping("/view/stock")
    public ResponseEntity<GenericResponse<Page<StockDto.ViewStockDto>>> viewStock(PageDto pageable, @RequestParam(required = false) ContainerWeights weight, @RequestParam(required = false) String containerName) {
        var stock = stockService.viewStockFilter(pageable, weight, containerName);
        return ResponseEntity.status(HttpStatus.OK).body(new GenericResponse<>("This is the current stock", stock));
    }

    // Endpoint for Adding Stock
    @Operation(summary = "Add Stock Item", description = "Endpoint for Adding Stock Item By Item")
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/add/stock")
    public ResponseEntity<GenericResponse<StockDto.ViewStockDto>> addStock(@Valid @RequestBody StockDto.AddStockDto stockDto, @RequestParam ContainerWeights weight) {
        var stock =  stockService.addStock(stockDto, weight);
        return ResponseEntity.status(HttpStatus.CREATED).body(new GenericResponse<>("Stock Added Successfully", stock));
    }

    // Endpoint for Getting the List of Containers
    @Operation(summary = "List of Containers", description = "Get List Of Containers registered")
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/distinct/containers")
    public ResponseEntity<GenericResponse<List<String>>> distinctContainers() {
        var distinctContainer = stockService.findAllContainers();
        return ResponseEntity.status(HttpStatus.OK).body(new GenericResponse<>("These are the Stored Containers", distinctContainer));
    }

    @Operation(
            summary = "Upload Stock via Excel",
            description = "Upload an .xlsx file with the columns code, name, quantity, container_name, weight. " +
                    "All columns except code are mandatory per row. Rows with missing required data are rejected."
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(value = "/upload/stock/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GenericResponse<Integer>> uploadStockFile(@RequestParam("file") MultipartFile file) {
        int processedRows = stockService.uploadStockFile(file);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GenericResponse<>("Stock upload completed", processedRows));
    }

    @Operation(
            summary = "Update Stock Item",
            description = "Update any combination of stock fields: code, name, quantity, container_name, weight. " +
                    "Only provided fields will be updated."
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GenericResponse<StockDto.ViewStockDto>> updateStock(
            @PathVariable UUID id,
            @Valid @RequestBody StockDto.UpdateStockDto updateDto) {
        var updatedStock = stockService.updateStock(id, updateDto);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GenericResponse<>("Stock updated successfully", updatedStock));
    }

    @Operation(summary = "Delete Stock", description = "Allow Admins to delete contents of Stock")
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<GenericResponse<String>> deleteStock(@PathVariable UUID id) {
        stockService.deleteStock(id);
        return ResponseEntity.status(HttpStatus.OK).body(new GenericResponse<>("Stock content deleted successfully!", "Deleted ID: " + id));
    }
}
