package com.certification.controller.common;

import com.certification.entity.common.Product;
import com.certification.service.common.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 竞品信息管理控制器
 * 提供竞品信息的增删改查接口
 */
@Slf4j
@Tag(name = "竞品信息管理", description = "竞品信息的增删改查和生成接口")
@RestController
@RequestMapping("/products")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    /**
     * 分页获取竞品信息列表
     */
    @GetMapping
    @Operation(summary = "获取竞品信息列表", description = "分页获取竞品信息列表，支持关键词搜索和风险等级筛选")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ResponseEntity<Map<String, Object>> getProducts(
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword,
            @Parameter(description = "设备等级") @RequestParam(required = false) String deviceClass,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Product> productPage;
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                // 关键词搜索
                productPage = productService.searchProducts(keyword.trim(), pageable);
            } else if (deviceClass != null && !deviceClass.trim().isEmpty()) {
                // 按设备等级筛选
                productPage = productService.findByDeviceClassPaged(deviceClass.trim(), pageable);
            } else {
                // 获取所有竞品信息
                productPage = productService.findAllActiveProductsPaged(pageable);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of(
                "list", productPage.getContent(),
                "total", productPage.getTotalElements(),
                "totalPages", productPage.getTotalPages(),
                "currentPage", productPage.getNumber(),
                "pageSize", productPage.getSize()
            ));
            response.put("message", "获取竞品信息列表成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取竞品信息列表失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取竞品信息列表失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * 根据ID获取竞品信息详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取竞品信息详情", description = "根据ID获取单个竞品信息的详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "竞品信息不存在"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ResponseEntity<Map<String, Object>> getProductById(
            @Parameter(description = "竞品信息ID") @PathVariable Long id) {
        
        try {
            Optional<Product> optionalProduct = productService.findById(id);
            
            Map<String, Object> response = new HashMap<>();
            if (optionalProduct.isPresent()) {
                response.put("success", true);
                response.put("data", optionalProduct.get());
                response.put("message", "获取竞品信息详情成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "竞品信息不存在");
                return ResponseEntity.status(404).body(response);
            }
            
        } catch (Exception e) {
            log.error("获取竞品信息详情失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取竞品信息详情失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * 创建竞品信息
     */
    @PostMapping
    @Operation(summary = "创建竞品信息", description = "创建新的竞品信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "创建成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ResponseEntity<Map<String, Object>> createProduct(@RequestBody Product product) {
        
        try {
            if (product.getProductName() == null || product.getProductName().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "产品名称不能为空");
                return ResponseEntity.status(400).body(errorResponse);
            }
            
            Product savedProduct = productService.saveProduct(product);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", savedProduct);
            response.put("message", "创建竞品信息成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("创建竞品信息失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "创建竞品信息失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * 更新竞品信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新竞品信息", description = "更新指定ID的竞品信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "404", description = "竞品信息不存在"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ResponseEntity<Map<String, Object>> updateProduct(
            @Parameter(description = "竞品信息ID") @PathVariable Long id,
            @RequestBody Product product) {
        
        try {
            Optional<Product> existingProduct = productService.findById(id);
            if (existingProduct.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "竞品信息不存在");
                return ResponseEntity.status(404).body(errorResponse);
            }
            
            product.setId(id);
            Product updatedProduct = productService.updateProduct(product);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedProduct);
            response.put("message", "更新竞品信息成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("更新竞品信息失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "更新竞品信息失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * 删除竞品信息
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除竞品信息", description = "删除指定ID的竞品信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功"),
        @ApiResponse(responseCode = "404", description = "竞品信息不存在"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ResponseEntity<Map<String, Object>> deleteProduct(
            @Parameter(description = "竞品信息ID") @PathVariable Long id) {
        
        try {
            Optional<Product> existingProduct = productService.findById(id);
            if (existingProduct.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "竞品信息不存在");
                return ResponseEntity.status(404).body(errorResponse);
            }
            
            productService.deleteProduct(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "删除竞品信息成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("删除竞品信息失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "删除竞品信息失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * 从高风险数据生成竞品信息
     */
    @PostMapping("/generate")
    @Operation(summary = "从高风险数据生成竞品信息", description = "根据高风险数据生成竞品信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "生成成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误或数据已存在"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ResponseEntity<Map<String, Object>> generateFromHighRiskData(@RequestBody Map<String, Object> request) {
        
        try {
            String dataSource = (String) request.get("dataSource");
            Long sourceDataId = Long.valueOf(request.get("sourceDataId").toString());
            String productName = (String) request.get("productName");
            String applicantName = (String) request.get("applicantName");
            String brandName = (String) request.get("brandName");
            String deviceCode = (String) request.get("deviceCode");
            String deviceClass = (String) request.get("deviceClass");
            String deviceDescription = (String) request.get("deviceDescription");
            
            if (dataSource == null || sourceDataId == null || productName == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "数据来源、原始数据ID和产品名称不能为空");
                return ResponseEntity.status(400).body(errorResponse);
            }
            
            Product generatedProduct = productService.generateFromHighRiskData(
                    dataSource, sourceDataId, productName, applicantName, brandName,
                    deviceCode, deviceClass, deviceDescription);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", generatedProduct);
            response.put("message", "从高风险数据生成竞品信息成功");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("生成竞品信息失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(400).body(errorResponse);
            
        } catch (Exception e) {
            log.error("生成竞品信息失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "生成竞品信息失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * 批量从高风险数据生成竞品信息
     */
    @PostMapping("/batch-generate")
    @Operation(summary = "批量生成竞品信息", description = "批量从高风险数据生成竞品信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "批量生成成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ResponseEntity<Map<String, Object>> batchGenerateFromHighRiskData(@RequestBody List<Product> products) {
        
        try {
            if (products == null || products.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "竞品信息列表不能为空");
                return ResponseEntity.status(400).body(errorResponse);
            }
            
            List<Product> generatedProducts = productService.batchGenerateFromHighRiskData(products);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", generatedProducts);
            response.put("message", String.format("批量生成竞品信息成功，共生成 %d 条", generatedProducts.size()));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("批量生成竞品信息失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "批量生成竞品信息失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * 获取竞品信息统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取竞品信息统计", description = "获取竞品信息的统计数据")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ResponseEntity<Map<String, Object>> getStatistics() {
        
        try {
            long totalCount = productService.count();
            long activeCount = productService.countActiveProducts();
            
            // 按设备等级统计
            List<Product> class1Products = productService.findByDeviceClass("Class I");
            List<Product> class2Products = productService.findByDeviceClass("Class II");
            List<Product> class3Products = productService.findByDeviceClass("Class III");
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalRecords", totalCount);
            statistics.put("activeCompetitors", activeCount);
            statistics.put("class1Count", class1Products.size());
            statistics.put("class2Count", class2Products.size());
            statistics.put("class3Count", class3Products.size());
            statistics.put("monthlyNew", 0); // TODO: 实现本月新增统计
            statistics.put("riskAlerts", class3Products.size()); // Class III设备数量作为风险提醒
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", statistics);
            response.put("message", "获取竞品信息统计成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取竞品信息统计失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取竞品信息统计失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * 检查是否已从指定高风险数据生成过竞品信息
     */
    @GetMapping("/check-exists")
    @Operation(summary = "检查竞品信息是否存在", description = "检查是否已从指定高风险数据生成过竞品信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "检查成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ResponseEntity<Map<String, Object>> checkExists(
            @Parameter(description = "数据来源") @RequestParam String dataSource,
            @Parameter(description = "原始数据ID") @RequestParam Long sourceDataId) {
        
        try {
            if (dataSource == null || dataSource.trim().isEmpty() || sourceDataId == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "数据来源和原始数据ID不能为空");
                return ResponseEntity.status(400).body(errorResponse);
            }
            
            boolean exists = productService.existsByDataSourceAndSourceDataId(dataSource, sourceDataId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of("exists", exists));
            response.put("message", exists ? "竞品信息已存在" : "竞品信息不存在");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("检查竞品信息是否存在失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "检查竞品信息是否存在失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
