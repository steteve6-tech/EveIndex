package com.certification.example;

import com.certification.entity.common.DeviceRegistrationRecord;
import com.certification.service.DeviceRegistrationConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 设备注册记录转换器使用示例
 */
@Component
public class DeviceRegistrationConverterExample {

    @Autowired
    private DeviceRegistrationConverter converter;

    /**
     * US FDA数据转换示例
     */
    public DeviceRegistrationRecord convertUSFDAExample() {
        // 模拟US FDA数据
        Map<String, Object> usData = new HashMap<>();
        usData.put("registration_number", "1234567890");
        usData.put("fei_number", "FEI123456");
        usData.put("manufacturer_name", "Medtronic Inc.");
        usData.put("owner_operator_number", "OP123456");
        usData.put("owner_operator_country_code", "US");
        usData.put("owner_operator_full_address", "710 Medtronic Parkway, Minneapolis, MN 55432");
        usData.put("device_names", "[\"Cardiac Pacemaker\", \"Implantable Defibrillator\"]");
        usData.put("proprietary_names", "[\"Medtronic Pacemaker\", \"Medtronic ICD\"]");
        usData.put("device_classes", "[\"Class III\", \"Class III\"]");
        usData.put("regulation_numbers", "[\"21 CFR 870.3610\", \"21 CFR 870.3605\"]");
        usData.put("product_codes", "[\"DXY\", \"LWS\"]");
        usData.put("risk_class", "Class III");
        usData.put("status_code", "Active");
        usData.put("reg_expiry_year", "2025");

        return converter.convertFromUSFDA(usData);
    }

    /**
     * EU EUDAMED数据转换示例
     */
    public DeviceRegistrationRecord convertEUEUDAMEDExample() {
        // 模拟EU EUDAMED数据
        Map<String, Object> euData = new HashMap<>();
        euData.put("udi_di", "01234567890123");
        euData.put("basic_udi_di", "01234567890123");
        euData.put("manufacturer", "Medtronic International Trading Sarl");
        euData.put("actor_id", "ACT123456");
        euData.put("manufacturer_country_code", "CH");
        euData.put("manufacturer_full_address", "Route du Molliau 31, 1131 Tolochenaz, Switzerland");
        euData.put("trade_name", "Medtronic Pacemaker");
        euData.put("device_class", "Class III");
        euData.put("risk_class", "Class III");
        euData.put("status_code", "Active");
        euData.put("reg_expiry_year", "2025");
        euData.put("regulation_number", "MDR 2017/745");
        euData.put("product_code", "DXY");

        return converter.convertFromEUEUDAMED(euData);
    }

    /**
     * 字段映射说明
     */
    public void printFieldMapping() {
        System.out.println("=== 设备注册记录字段映射说明 ===");
        System.out.println();
        
        System.out.println("【数据源标识】");
        System.out.println("dataSource: US_FDA / EU_EUDAMED");
        System.out.println("jdCountry: US / EU");
        System.out.println();
        
        System.out.println("【核心标识字段】");
        System.out.println("US FDA -> registrationNumber: registration_number");
        System.out.println("EU EUDAMED -> registrationNumber: udi_di");
        System.out.println("US FDA -> feiNumber: fei_number");
        System.out.println("EU EUDAMED -> feiNumber: basic_udi_di");
        System.out.println();
        
        System.out.println("【制造商信息】");
        System.out.println("US FDA -> manufacturerName: manufacturer_name");
        System.out.println("EU EUDAMED -> manufacturerName: manufacturer");
        System.out.println("US FDA -> manufacturerId: owner_operator_number");
        System.out.println("EU EUDAMED -> manufacturerId: actor_id");
        System.out.println("US FDA -> manufacturerCountryCode: owner_operator_country_code");
        System.out.println("EU EUDAMED -> manufacturerCountryCode: manufacturer_country_code");
        System.out.println();
        
        System.out.println("【设备信息】");
        System.out.println("US FDA -> deviceName: device_names[0]");
        System.out.println("EU EUDAMED -> deviceName: trade_name");
        System.out.println("US FDA -> proprietaryName: proprietary_names[0]");
        System.out.println("EU EUDAMED -> proprietaryName: trade_name");
        System.out.println("US FDA -> deviceClass: device_classes[0]");
        System.out.println("EU EUDAMED -> deviceClass: device_class");
        System.out.println();
        
        System.out.println("【状态信息】");
        System.out.println("US FDA -> statusCode: status_code");
        System.out.println("EU EUDAMED -> statusCode: status_code");
        System.out.println("US FDA -> regExpiryYear: reg_expiry_year");
        System.out.println("EU EUDAMED -> regExpiryYear: reg_expiry_year");
        System.out.println();
        
        System.out.println("【监管信息】");
        System.out.println("US FDA -> regulationNumber: regulation_numbers[0]");
        System.out.println("EU EUDAMED -> regulationNumber: regulation_number");
        System.out.println("US FDA -> productCode: product_codes[0]");
        System.out.println("EU EUDAMED -> productCode: product_code");
        System.out.println();
        
        System.out.println("【扩展信息（JSON数组）】");
        System.out.println("两个数据源都支持：productCodes, deviceNames, deviceClasses, regulationNumbers, proprietaryNames");
        System.out.println();
        
        System.out.println("【分析字段】");
        System.out.println("riskLevel: 计算得出（LOW/MEDIUM/HIGH）");
        System.out.println("keywords: 提取的关键词（JSON数组）");
        System.out.println();
        
        System.out.println("【元数据】");
        System.out.println("crawlTime: 爬取时间");
        System.out.println("createTime: 创建时间（自动）");
        System.out.println("updateTime: 更新时间（自动）");
    }
}
