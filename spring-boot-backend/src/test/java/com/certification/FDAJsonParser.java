package com.certification;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * FDA JSON数据解析器
 * 用于解析FDA API返回的各种设备数据
 */
public class FDAJsonParser {
    private final ObjectMapper objectMapper;

    public FDAJsonParser() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 设备注册信息数据模型
     */
    public static class DeviceRegistration {
        @JsonProperty("proprietary_name")
        private List<String> proprietaryName;
        
        @JsonProperty("establishment_type")
        private List<String> establishmentType;
        
        @JsonProperty("registration")
        private Registration registration;
        
        @JsonProperty("pma_number")
        private String pmaNumber;
        
        @JsonProperty("k_number")
        private String kNumber;
        
        @JsonProperty("products")
        private List<Product> products;

        // Getters and Setters
        public List<String> getProprietaryName() { return proprietaryName; }
        public void setProprietaryName(List<String> proprietaryName) { this.proprietaryName = proprietaryName; }
        
        public List<String> getEstablishmentType() { return establishmentType; }
        public void setEstablishmentType(List<String> establishmentType) { this.establishmentType = establishmentType; }
        
        public Registration getRegistration() { return registration; }
        public void setRegistration(Registration registration) { this.registration = registration; }
        
        public String getPmaNumber() { return pmaNumber; }
        public void setPmaNumber(String pmaNumber) { this.pmaNumber = pmaNumber; }
        
        public String getKNumber() { return kNumber; }
        public void setKNumber(String kNumber) { this.kNumber = kNumber; }
        
        public List<Product> getProducts() { return products; }
        public void setProducts(List<Product> products) { this.products = products; }

        public static class Registration {
            @JsonProperty("registration_number")
            private String registrationNumber;
            
            @JsonProperty("fei_number")
            private String feiNumber;
            
            @JsonProperty("status_code")
            private String statusCode;
            
            @JsonProperty("name")
            private String name;
            
            @JsonProperty("address_line_1")
            private String addressLine1;
            
            @JsonProperty("city")
            private String city;
            
            @JsonProperty("state_code")
            private String stateCode;
            
            @JsonProperty("iso_country_code")
            private String isoCountryCode;
            
            @JsonProperty("postal_code")
            private String postalCode;

            // Getters and Setters
            public String getRegistrationNumber() { return registrationNumber; }
            public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
            
            public String getFeiNumber() { return feiNumber; }
            public void setFeiNumber(String feiNumber) { this.feiNumber = feiNumber; }
            
            public String getStatusCode() { return statusCode; }
            public void setStatusCode(String statusCode) { this.statusCode = statusCode; }
            
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            
            public String getAddressLine1() { return addressLine1; }
            public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }
            
            public String getCity() { return city; }
            public void setCity(String city) { this.city = city; }
            
            public String getStateCode() { return stateCode; }
            public void setStateCode(String stateCode) { this.stateCode = stateCode; }
            
            public String getIsoCountryCode() { return isoCountryCode; }
            public void setIsoCountryCode(String isoCountryCode) { this.isoCountryCode = isoCountryCode; }
            
            public String getPostalCode() { return postalCode; }
            public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
        }

        public static class Product {
            @JsonProperty("product_code")
            private String productCode;
            
            @JsonProperty("created_date")
            private String createdDate;
            
            @JsonProperty("exempt")
            private String exempt;
            
            @JsonProperty("openfda")
            private OpenFDA openfda;

            // Getters and Setters
            public String getProductCode() { return productCode; }
            public void setProductCode(String productCode) { this.productCode = productCode; }
            
            public String getCreatedDate() { return createdDate; }
            public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }
            
            public String getExempt() { return exempt; }
            public void setExempt(String exempt) { this.exempt = exempt; }
            
            public OpenFDA getOpenfda() { return openfda; }
            public void setOpenfda(OpenFDA openfda) { this.openfda = openfda; }
        }
    }

    /**
     * OpenFDA通用数据模型
     */
    public static class OpenFDA {
        @JsonProperty("device_name")
        private String deviceName;
        
        @JsonProperty("medical_specialty_description")
        private String medicalSpecialtyDescription;
        
        @JsonProperty("regulation_number")
        private String regulationNumber;
        
        @JsonProperty("device_class")
        private String deviceClass;

        // Getters and Setters
        public String getDeviceName() { return deviceName; }
        public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
        
        public String getMedicalSpecialtyDescription() { return medicalSpecialtyDescription; }
        public void setMedicalSpecialtyDescription(String medicalSpecialtyDescription) { this.medicalSpecialtyDescription = medicalSpecialtyDescription; }
        
        public String getRegulationNumber() { return regulationNumber; }
        public void setRegulationNumber(String regulationNumber) { this.regulationNumber = regulationNumber; }
        
        public String getDeviceClass() { return deviceClass; }
        public void setDeviceClass(String deviceClass) { this.deviceClass = deviceClass; }
    }

    /**
     * 解析设备注册信息JSON文件
     */
    public List<DeviceRegistration> parseDeviceRegistration(String jsonFile) throws IOException {
        CollectionType listType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, DeviceRegistration.class);
        return objectMapper.readValue(new File(jsonFile), listType);
    }

    /**
     * 分析设备注册信息数据
     */
    public void analyzeDeviceRegistration(List<DeviceRegistration> registrations) {
        System.out.println("=== 设备注册信息数据分析 ===");
        System.out.println("总记录数: " + registrations.size());
        
        // 统计国家分布
        Map<String, Long> countryStats = registrations.stream()
                .filter(r -> r.getRegistration() != null && r.getRegistration().getIsoCountryCode() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        r -> r.getRegistration().getIsoCountryCode(),
                        java.util.stream.Collectors.counting()
                ));
        
        System.out.println("\n国家分布:");
        countryStats.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
        
        // 统计产品代码分布
        Map<String, Long> productCodeStats = registrations.stream()
                .filter(r -> r.getProducts() != null)
                .flatMap(r -> r.getProducts().stream())
                .filter(p -> p.getProductCode() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        DeviceRegistration.Product::getProductCode,
                        java.util.stream.Collectors.counting()
                ));
        
        System.out.println("\n产品代码分布 (前10):");
        productCodeStats.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
    }

    /**
     * 主方法 - 演示JSON解析功能
     */
    public static void main(String[] args) {
        FDAJsonParser parser = new FDAJsonParser();
        
        try {
            // 解析设备注册信息
            if (new File("device_reglist_results.json").exists()) {
                System.out.println("正在解析设备注册信息...");
                List<DeviceRegistration> registrations = parser.parseDeviceRegistration("device_reglist_results.json");
                parser.analyzeDeviceRegistration(registrations);
                System.out.println();
            }
            
        } catch (IOException e) {
            System.err.println("解析JSON文件时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
