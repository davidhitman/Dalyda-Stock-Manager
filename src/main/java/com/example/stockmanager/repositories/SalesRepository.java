package com.example.stockmanager.repositories;

import com.example.stockmanager.entities.Sales;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SalesRepository extends JpaRepository<Sales, UUID> {
    @Query("SELECT s FROM Sales s ORDER BY s.date DESC LIMIT 10")
    List<Sales> getRecentSales();

    @Query("SELECT s FROM Sales s ORDER BY s.date DESC")
    Page<Sales> getSales(Pageable pageable);

    @Query("""
            SELECT s FROM Sales s
            WHERE (:startDate IS NULL OR s.date >= :startDate)
              AND (:endDate IS NULL OR s.date <= :endDate)
            ORDER BY s.date DESC
            """)
    Page<Sales> findByDateRange(Pageable pageable,
                                @Param("startDate") java.time.LocalDate startDate,
                                @Param("endDate") java.time.LocalDate endDate);

    @Query("SELECT s FROM Sales s WHERE s.id = :id")
    Optional<Sales> findSaleById(@Param("id") UUID id);
}
