package com.example.stockmanager.repositories;

import com.example.stockmanager.entities.Stock;
import com.example.stockmanager.entities.ContainerWeights;
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
public interface StockRepository extends JpaRepository<Stock, UUID> {
    @Query("SELECT SUM(s.quantity) FROM Stock s")
    Integer getTotalStock();

    @Query("SELECT SUM(s.quantity) FROM Stock s WHERE s.weight = 'KG_75' ")
    Integer get75KGTotalStock();
    @Query("SELECT SUM(s.quantity) FROM Stock s WHERE s.weight = 'KG_45' ")
    Integer get45KGTotalStock();

    @Query("SELECT SUM(s.quantity) FROM Stock s WHERE s.weight = 'BAGS' ")
    Integer getBagTotalStock();

    @Query("SELECT s FROM Stock s WHERE LOWER(s.name) = LOWER(:productName) AND s.weight = :weight AND LOWER(s.container_name) = LOWER(:containerName)")
    Optional<Stock> findByProductNameAndWeightAndContainerName(
            @Param("productName") String productName,
            @Param("weight") ContainerWeights weight,
            @Param("containerName") String containerName
    );
    @Query("SELECT s FROM Stock s WHERE s.weight = :weight and lower(s.container_name) = lower(:containerName)")
    Page<Stock> findByWeightAndContainerName(Pageable pageable, @Param("weight") ContainerWeights weight, @Param("containerName") String containerName);
    @Query("SELECT s FROM Stock s WHERE s.weight = :weight")
    Page<Stock> findByWeight(Pageable pageable, @Param("weight") ContainerWeights weight);
    @Query("SELECT s FROM Stock s WHERE lower(s.container_name) = lower(:containerName)")
    Page<Stock> findByContainerName(Pageable pageable, @Param("containerName") String containerName);
    @Query("SELECT s FROM Stock s")
    Page<Stock> getAllStock(Pageable pageable);
    @Query("SELECT DISTINCT UPPER(TRIM(s.container_name)) FROM Stock s")
    List<String> getDistinctContainerName();

    @Query("SELECT s FROM Stock s WHERE s.id = :id")
    Optional<Stock> findItemById(@Param("id") UUID id);

    @Query("SELECT s.name FROM Stock s WHERE LOWER(s.code) = LOWER(:articleCode)")
    List<String> getArticleName (@Param("articleCode") String articleCode);

    @Query("SELECT s FROM Stock s WHERE LOWER(s.code) = LOWER(:articleCode)")
    Optional<Stock> findByCode(@Param("articleCode") String articleCode);
}
