package com.example.stockmanager.controllers;

import com.example.stockmanager.dtos.PageDto;
import com.example.stockmanager.dtos.SalesDto.SalesDateDto;
import com.example.stockmanager.dtos.SalesDto;
import com.example.stockmanager.responses.GenericResponse;
import com.example.stockmanager.services.SalesServices;
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

import java.util.List;
import java.util.UUID;

@Tag(name = "Sales Controller", description = "Handles Sales")
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/sales")
@PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
public class SalesController {

    private SalesServices salesService;

    @Operation(summary = "Add Sales", description = "Endpoint to Add Sales")
    @PostMapping("/add")
    public ResponseEntity<GenericResponse<SalesDto.ViewSalesDto>> addSales(@Valid @RequestBody SalesDto.AddSalesDto salesDto) {
        var sales = salesService.addSales(salesDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new GenericResponse<>("Sales Added successfully!", sales));
    }

    @Operation(summary = "View Recent Sales", description = "View Recent Sales for the front page")
    @GetMapping("/recent") // returns a list of 10 recent sales
    public ResponseEntity<GenericResponse<List<SalesDto.ViewSalesDto>>> recentSales() {
        var sales = salesService.recentSales();
        return ResponseEntity.status(HttpStatus.OK).body(new GenericResponse<>(" 10 Recent Sales", sales));
    }

    @Operation(summary = "View All Sales", description = "View All Sales in the database")
    @GetMapping("/all")
    public ResponseEntity<GenericResponse<Page<SalesDto.ViewSalesDto>>> viewAllSales(PageDto pageDto) {
        var sales = salesService.viewSales(pageDto);
        return ResponseEntity.status(HttpStatus.OK).body(new GenericResponse<>("All Sales in the Database", sales));
    }

    @Operation(
            summary = "Filter Sales by Date",
            description = "Provide optional startDate/endDate and pagination to retrieve filtered sales."
    )
    @PostMapping(value = "/filter", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GenericResponse<Page<SalesDto.ViewSalesDto>>> salesFilter(
            @Valid @RequestBody SalesDateDto dateDto
    ) {
        var sales = salesService.viewSalesFiltered(dateDto);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GenericResponse<>("Filtered sales", sales));
    }

    @Operation(summary = "Get the Item Name", description = "Get the Item name with the Item Code")
    @GetMapping(value = "/article/name")
    public ResponseEntity<GenericResponse<String>> getArticleName(@RequestParam String articleCode) {
        var name = salesService.getItemName(articleCode);
        return ResponseEntity.status(HttpStatus.OK).body(new GenericResponse<>("Item Name", name));

    }

    @Operation(summary = "Delete sales", description = "Delete Sales and Add back to stock")
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<GenericResponse<String>> deleteSales(@PathVariable UUID id) {
        salesService.deleteSales(id);
        return ResponseEntity.status(HttpStatus.OK).body(new GenericResponse<>("Sale deleted successfully!", "Deleted ID: " + id));
    }

    @Operation(summary = "Update sale", description = "Update sale quantity/price and sync stock")
    @PreAuthorize("hasAuthority('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<GenericResponse<SalesDto.ViewSalesDto>> updateSale(
            @PathVariable UUID id,
            @Valid @RequestBody SalesDto.UpdateSalesDto updateDto
    ) {
        var updated = salesService.updateSale(id, updateDto);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GenericResponse<>("Sale updated successfully!", updated));
    }
}
