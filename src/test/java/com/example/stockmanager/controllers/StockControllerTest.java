package com.example.stockmanager.controllers;

import com.example.stockmanager.dtos.StockDto;
import com.example.stockmanager.entities.ContainerWeights;
import com.example.stockmanager.entities.Role;
import com.example.stockmanager.entities.Stock;
import com.example.stockmanager.entities.Users;
import com.example.stockmanager.repositories.StockRepository;
import com.example.stockmanager.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import java.io.ByteArrayOutputStream;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private Stock testStock75;
    private StockDto.AddStockDto addStockDto;
    private StockDto.UpdateStockDto updateStockDto;

    @BeforeEach
    void setUp() {
        // Clean up repositories
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

        // Create test stock items
        testStock75 = new Stock("ST001", "Test Item 75KG", 100, "Container1");
        testStock75.setWeight(ContainerWeights.KG_75);
        testStock75 = stockRepository.save(testStock75);

        Stock testStock45 = new Stock("ST002", "Test Item 45KG", 50, "Container2");
        testStock45.setWeight(ContainerWeights.KG_45);
        stockRepository.save(testStock45);

        Stock testStockBags = new Stock("ST003", "Test Item Bags", 30, "Container3");
        testStockBags.setWeight(ContainerWeights.BAGS);
        stockRepository.save(testStockBags);

        // Setup stock DTOs
        addStockDto = new StockDto.AddStockDto();
        addStockDto.setItem_code("ST004");
        addStockDto.setItem_name("New Item");
        addStockDto.setQuantity(25);
        addStockDto.setContainer_name("Container4");

        updateStockDto = new StockDto.UpdateStockDto();
        updateStockDto.setItem_code("ST001-UPDATED");
        updateStockDto.setItem_name("Updated Item");
        updateStockDto.setQuantity(150);
        updateStockDto.setContainer_name("Updated Container");
        updateStockDto.setWeight(ContainerWeights.KG_75);
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testViewTotalStock_Success() throws Exception {
        mockMvc.perform(get("/api/v1/stock/total"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("This is the total Stock Number"))
                .andExpect(jsonPath("$.data").value(180));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testView75Stock_Success() throws Exception {
        mockMvc.perform(get("/api/v1/stock/75KG"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("This is the total Stock Number for 75KG"))
                .andExpect(jsonPath("$.data").value(100));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testView45Stock_Success() throws Exception {
        mockMvc.perform(get("/api/v1/stock/45KG"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("This is the total Stock Number for 45KG"))
                .andExpect(jsonPath("$.data").value(50));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testViewBagStock_Success() throws Exception {
        mockMvc.perform(get("/api/v1/stock/bags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("This is the total Stock Number for Bags"))
                .andExpect(jsonPath("$.data").value(30));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testViewStock_Success() throws Exception {
        mockMvc.perform(get("/api/v1/stock/view/stock")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("This is the current stock"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(3));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testViewStock_WithWeightFilter() throws Exception {
        mockMvc.perform(get("/api/v1/stock/view/stock")
                        .param("page", "0")
                        .param("size", "10")
                        .param("weight", "KG_75"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].weight").value("KG_75"));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testViewStock_WithContainerNameFilter() throws Exception {
        mockMvc.perform(get("/api/v1/stock/view/stock")
                        .param("page", "0")
                        .param("size", "10")
                        .param("containerName", "Container1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].container_name").value("Container1"));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testViewStock_WithBothFilters() throws Exception {
        mockMvc.perform(get("/api/v1/stock/view/stock")
                        .param("page", "0")
                        .param("size", "10")
                        .param("weight", "KG_75")
                        .param("containerName", "Container1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testAddStock_Success() throws Exception {
        mockMvc.perform(post("/api/v1/stock/add/stock")
                        .with(csrf())
                        .param("weight", "KG_75")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addStockDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Stock Added Successfully"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.item_code").value("ST004"))
                .andExpect(jsonPath("$.data.item_name").value("New Item"))
                .andExpect(jsonPath("$.data.quantity").value(25));

        // Verify stock was added to database
        Stock addedStock = stockRepository.findByCode("ST004").orElseThrow();
        assertEquals("New Item", addedStock.getName());
        assertEquals(25, addedStock.getQuantity());
        assertEquals(ContainerWeights.KG_75, addedStock.getWeight());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testAddStock_AddToExistingStock() throws Exception {
        // Add stock with same name, weight, and container as existing stock
        StockDto.AddStockDto addToExisting = new StockDto.AddStockDto();
        addToExisting.setItem_code("ST001");
        addToExisting.setItem_name("Test Item 75KG");
        addToExisting.setQuantity(50);
        addToExisting.setContainer_name("Container1");

        int initialQuantity = testStock75.getQuantity();

        mockMvc.perform(post("/api/v1/stock/add/stock")
                        .with(csrf())
                        .param("weight", "KG_75")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addToExisting)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.quantity").value(150));

        // Verify quantity was increased
        stockRepository.flush();
        Stock updatedStock = stockRepository.findByCode("ST001").orElseThrow();
        assertEquals(initialQuantity + 50, updatedStock.getQuantity());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testAddStock_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/stock/add/stock")
                        .with(csrf())
                        .param("weight", "KG_75")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addStockDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testAddStock_InvalidData() throws Exception {
        addStockDto.setItem_name("");

        mockMvc.perform(post("/api/v1/stock/add/stock")
                        .with(csrf())
                        .param("weight", "KG_75")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addStockDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testDistinctContainers_Success() throws Exception {
        mockMvc.perform(get("/api/v1/stock/distinct/containers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("These are the Stored Containers"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0]").exists());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testDistinctContainers_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/stock/distinct/containers"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testUploadStockFile_Success() throws Exception {
        // Create a real Excel file using Apache POI
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Stock");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"code", "name", "quantity", "container_name", "weight"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Create data rows
            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("EX001");
            row1.createCell(1).setCellValue("Excel Item 1");
            row1.createCell(2).setCellValue(10);
            row1.createCell(3).setCellValue("Excel Container");
            row1.createCell(4).setCellValue("KG_75");

            Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("EX002");
            row2.createCell(1).setCellValue("Excel Item 2");
            row2.createCell(2).setCellValue(20);
            row2.createCell(3).setCellValue("Excel Container");
            row2.createCell(4).setCellValue("KG_45");

            workbook.write(outputStream);
            byte[] excelBytes = outputStream.toByteArray();

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    excelBytes
            );

            mockMvc.perform(multipart("/api/v1/stock/upload/stock/file")
                            .file(file)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Stock upload completed"))
                    .andExpect(jsonPath("$.data").value(2)); // 2 rows processed

            // Verify stock was added to database
            Stock excelStock1 = stockRepository.findByCode("EX001").orElseThrow();
            assertEquals("Excel Item 1", excelStock1.getName());
            assertEquals(10, excelStock1.getQuantity());
            assertEquals(ContainerWeights.KG_75, excelStock1.getWeight());
        }
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testUploadStockFile_Unauthorized() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "test content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/stock/upload/stock/file")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testUpdateStock_Success() throws Exception {
        UUID stockId = testStock75.getId();

        mockMvc.perform(patch("/api/v1/stock/{id}", stockId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateStockDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Stock updated successfully"))
                .andExpect(jsonPath("$.data.id").value(stockId.toString()))
                .andExpect(jsonPath("$.data.item_code").value("ST001-UPDATED"))
                .andExpect(jsonPath("$.data.item_name").value("Updated Item"))
                .andExpect(jsonPath("$.data.quantity").value(150));

        // Verify stock was updated in database
        stockRepository.flush();
        Stock updatedStock = stockRepository.findItemById(stockId).orElseThrow();
        assertEquals("ST001-UPDATED", updatedStock.getCode());
        assertEquals("Updated Item", updatedStock.getName());
        assertEquals(150, updatedStock.getQuantity());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testUpdateStock_PartialUpdate() throws Exception {
        UUID stockId = testStock75.getId();
        String originalName = testStock75.getName();

        StockDto.UpdateStockDto partialUpdate = new StockDto.UpdateStockDto();
        partialUpdate.setItem_code(null);
        partialUpdate.setItem_name(null);
        partialUpdate.setQuantity(200);
        partialUpdate.setContainer_name(null);
        partialUpdate.setWeight(null);

        mockMvc.perform(patch("/api/v1/stock/{id}", stockId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partialUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(200));

        // Verify only quantity changed
        stockRepository.flush();
        Stock updatedStock = stockRepository.findItemById(stockId).orElseThrow();
        assertEquals(originalName, updatedStock.getName()); // Name unchanged
        assertEquals(200, updatedStock.getQuantity()); // Quantity changed
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testUpdateStock_InvalidQuantity() throws Exception {
        UUID stockId = testStock75.getId();

        updateStockDto.setQuantity(-1);

        mockMvc.perform(patch("/api/v1/stock/{id}", stockId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateStockDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testUpdateStock_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/stock/{id}", nonExistentId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateStockDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testUpdateStock_Unauthorized() throws Exception {
        UUID stockId = testStock75.getId();

        mockMvc.perform(patch("/api/v1/stock/{id}", stockId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateStockDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testDeleteStock_Success() throws Exception {
        UUID stockId = testStock75.getId();

        mockMvc.perform(delete("/api/v1/stock/{id}", stockId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Stock content deleted successfully!"))
                .andExpect(jsonPath("$.data").value("Deleted ID: " + stockId));

        // Verify stock was deleted
        assertFalse(stockRepository.findById(stockId).isPresent());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testDeleteStock_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/stock/{id}", nonExistentId)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testDeleteStock_Unauthorized() throws Exception {
        UUID stockId = testStock75.getId();

        mockMvc.perform(delete("/api/v1/stock/{id}", stockId)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
