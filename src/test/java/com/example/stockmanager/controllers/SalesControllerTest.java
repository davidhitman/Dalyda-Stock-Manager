package com.example.stockmanager.controllers;

import com.example.stockmanager.dtos.SalesDto;
import com.example.stockmanager.entities.ContainerWeights;
import com.example.stockmanager.entities.Role;
import com.example.stockmanager.entities.Stock;
import com.example.stockmanager.entities.Users;
import com.example.stockmanager.repositories.SalesRepository;
import com.example.stockmanager.repositories.StockRepository;
import com.example.stockmanager.repositories.UserRepository;
import com.example.stockmanager.services.SalesServices;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SalesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SalesServices salesService;

    @Autowired
    private SalesRepository salesRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private Stock testStock;
    private SalesDto.AddSalesDto addSalesDto;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());

        // Clean up repositories
        salesRepository.deleteAll();
        stockRepository.deleteAll();
        userRepository.deleteAll();

        // Create test admin user
        Users testAdmin = new Users("Admin", "User", "admin@test.com", "1234567890");
        testAdmin.setPassword(passwordEncoder.encode("password123"));
        testAdmin.setRole(Role.ADMIN);
        userRepository.save(testAdmin);

        // Create test regular user
        Users testUser = new Users("Test", "User", "user@test.com", "0987654321");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole(Role.USER);
        userRepository.save(testUser);

        // Create test stock
        testStock = new Stock("ST001", "Test Item", 100, "Container1");
        testStock.setWeight(ContainerWeights.KG_75);
        testStock = stockRepository.save(testStock);

        // Setup sales DTO
        addSalesDto = new SalesDto.AddSalesDto();
        addSalesDto.setDate(LocalDate.now());
        addSalesDto.setCode("ST001");
        addSalesDto.setName("Test Item");
        addSalesDto.setQuantity(10);
        addSalesDto.setPrice(100.0);
        addSalesDto.setTotalPrice(1000.0);
        addSalesDto.setWeight(ContainerWeights.KG_75);
        addSalesDto.setContainerName("Container1");
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testAddSales_Success() throws Exception {
        mockMvc.perform(post("/api/v1/sales/add")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addSalesDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Sales Added successfully!"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.code").value("ST001"))
                .andExpect(jsonPath("$.data.quantity").value(10));

        // Verify stock was reduced
        Stock updatedStock = stockRepository.findByCode("ST001").orElseThrow();
        assertEquals(90, updatedStock.getQuantity());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testAddSales_InvalidData() throws Exception {
        addSalesDto.setName("");

        mockMvc.perform(post("/api/v1/sales/add")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addSalesDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testAddSales_InsufficientStock() throws Exception {
        addSalesDto.setQuantity(200);

        mockMvc.perform(post("/api/v1/sales/add")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addSalesDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testRecentSales_Success() throws Exception {
        // Add some sales first
        salesService.addSales(addSalesDto);
        SalesDto.AddSalesDto secondSale = new SalesDto.AddSalesDto();
        secondSale.setDate(LocalDate.now().minusDays(1));
        secondSale.setCode("ST001");
        secondSale.setName("Test Item");
        secondSale.setQuantity(5);
        secondSale.setPrice(50.0);
        secondSale.setTotalPrice(250.0);
        secondSale.setWeight(ContainerWeights.KG_75);
        secondSale.setContainerName("Container1");
        salesService.addSales(secondSale);

        mockMvc.perform(get("/api/v1/sales/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(" 10 Recent Sales"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testViewAllSales_Success() throws Exception {
        salesService.addSales(addSalesDto);

        mockMvc.perform(get("/api/v1/sales/all")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All Sales in the Database"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testSalesFilter_Success() throws Exception {
        salesService.addSales(addSalesDto);

        SalesDto.SalesDateDto salesDateDto = new SalesDto.SalesDateDto();
        salesDateDto.setStartDate(LocalDate.now().minusDays(1));
        salesDateDto.setEndDate(LocalDate.now().plusDays(1));
        salesDateDto.setPage(0);
        salesDateDto.setSize(10);

        mockMvc.perform(post("/api/v1/sales/filter")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(salesDateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Filtered sales"))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testGetArticleName_Success() throws Exception {
        mockMvc.perform(get("/api/v1/sales/article/name")
                        .param("articleCode", "ST001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Item Name"))
                .andExpect(jsonPath("$.data").value("Test Item"));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testGetArticleName_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/sales/article/name")
                        .param("articleCode", "NONEXISTENT"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testDeleteSales_Success() throws Exception {

        int initialStock = testStock.getQuantity();

        var createdSale = salesService.addSales(addSalesDto);
        UUID salesId = createdSale.getId();

        mockMvc.perform(delete("/api/v1/sales/{id}", salesId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Sale deleted successfully!"))
                .andExpect(jsonPath("$.data").value("Deleted ID: " + salesId));

        assertFalse(salesRepository.findById(salesId).isPresent());

        stockRepository.flush();
        Stock updatedStock = stockRepository.findByCode("ST001").orElseThrow();
        assertEquals(initialStock, updatedStock.getQuantity());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testDeleteSales_Unauthorized() throws Exception {
        var createdSale = salesService.addSales(addSalesDto);
        UUID salesId = createdSale.getId();

        mockMvc.perform(delete("/api/v1/sales/{id}", salesId)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testDeleteSales_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/sales/{id}", nonExistentId)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testUpdateSale_Success() throws Exception {
        var createdSale = salesService.addSales(addSalesDto);
        UUID salesId = createdSale.getId();

        SalesDto.UpdateSalesDto updateSalesDto = new SalesDto.UpdateSalesDto();
        updateSalesDto.setQuantity(15);
        updateSalesDto.setPrice(120.0);

        mockMvc.perform(patch("/api/v1/sales/{id}", salesId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateSalesDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Sale updated successfully!"))
                .andExpect(jsonPath("$.data.id").value(salesId.toString()))
                .andExpect(jsonPath("$.data.quantity").value(15))
                .andExpect(jsonPath("$.data.price").value(120.0));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testUpdateSale_InvalidQuantity() throws Exception {
        var createdSale = salesService.addSales(addSalesDto);
        UUID salesId = createdSale.getId();

        SalesDto.UpdateSalesDto updateSalesDto = new SalesDto.UpdateSalesDto();
        updateSalesDto.setQuantity(-1);
        updateSalesDto.setPrice(120.0);

        mockMvc.perform(patch("/api/v1/sales/{id}", salesId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateSalesDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testUpdateSale_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        SalesDto.UpdateSalesDto updateSalesDto = new SalesDto.UpdateSalesDto();
        updateSalesDto.setQuantity(15);
        updateSalesDto.setPrice(120.0);

        mockMvc.perform(patch("/api/v1/sales/{id}", nonExistentId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateSalesDto)))
                .andExpect(status().isNotFound());
    }
}
