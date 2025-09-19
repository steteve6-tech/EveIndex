package com.certification.standards;

import com.certification.entity.common.Standard;
import com.certification.repository.StandardRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 标准数据初始化器
 * 用于初始化标准表中的基础数据
 */
@Slf4j
@Component
public class StandardDataInitializer implements CommandLineRunner {

    @Autowired
    private StandardRepository standardRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化标准数据...");
        
        // 检查是否已有数据
        long count = standardRepository.count();
        if (count > 0) {
            log.info("标准表中已有 {} 条数据，跳过初始化", count);
            return;
        }

        // 创建标准数据列表
        List<Standard> standards = createStandardData();
        
        // 批量保存
        standardRepository.saveAll(standards);
        
        log.info("标准数据初始化完成，共插入 {} 条数据", standards.size());
    }

    private List<Standard> createStandardData() {
        return Arrays.asList(
            // 1. RoHS 2.0
            createStandard(
                "2011/65/EU",
                "2011/65/EU，(EU)2015/863",
                "RoHS 2.0",
                "欧盟电子电气设备中有害物质限制指令，限制铅、汞、镉等 10 种有害物质",
                "2011-07-01",
                "2013-01-02",
                "https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:32011L0065",
                "RoHS,有害物质,欧盟,电子电气设备",
                Standard.RiskLevel.HIGH,
                Standard.RegulatoryImpact.CRITICAL,
                Standard.StandardStatus.ACTIVE,
                "EU",
                Arrays.asList("EU"),
                "欧盟市场的电子电气产品"
            ),

            // 2. GB/T 39560 (Chinese RoHS)
            createStandard(
                "GB/T 39560",
                "GB/T 39560-1-2020",
                "GB/T 39560 (Chinese RoHS)",
                "中国《电子电气产品有害物质限制使用标准》",
                "2020-01-01",
                "2021-01-01",
                "http://www.gb688.cn/bzgk/gb/newGbInfo?hcno=39560",
                "Chinese RoHS,有害物质,中国,电子电气产品",
                Standard.RiskLevel.HIGH,
                Standard.RegulatoryImpact.CRITICAL,
                Standard.StandardStatus.ACTIVE,
                "CN",
                Arrays.asList("CN"),
                "中国大陆销售的电子电气产品"
            ),

            // 3. SRRC
            createStandard(
                "SRRC",
                "无具体版本",
                "SRRC",
                "中国无线电发射设备型号核准",
                "1999-01-01",
                "2001-01-01",
                "http://www.miit.gov.cn",
                "SRRC,无线电,中国,型号核准",
                Standard.RiskLevel.HIGH,
                Standard.RegulatoryImpact.HIGH,
                Standard.StandardStatus.ACTIVE,
                "CN",
                Arrays.asList("CN"),
                "在中国销售和使用的无线产品"
            ),

            // 4. China Quality Test Report
            createStandard(
                "CCC",
                "CCC 认证",
                "China Quality Test Report",
                "中国强制性产品检测报告（CQC/CCC）",
                "2002-01-01",
                "2003-01-01",
                "http://www.cnca.gov.cn",
                "CCC,CQC,强制性认证,中国",
                Standard.RiskLevel.HIGH,
                Standard.RegulatoryImpact.CRITICAL,
                Standard.StandardStatus.ACTIVE,
                "CN",
                Arrays.asList("CN"),
                "中国大陆市场电子电气产品"
            ),

            // 5. FCC ID
            createStandard(
                "FCC ID",
                "CFR Title 47 Part 15/22/24 等",
                "FCC ID",
                "美国联邦通信委员会无线设备认证",
                "1934-01-01",
                "1934-01-01",
                "https://www.fcc.gov",
                "FCC,无线设备,美国,认证",
                Standard.RiskLevel.HIGH,
                Standard.RegulatoryImpact.HIGH,
                Standard.StandardStatus.ACTIVE,
                "US",
                Arrays.asList("US"),
                "在美国销售和使用的无线设备"
            ),

            // 6. WPC
            createStandard(
                "WPC ETA",
                "ETA",
                "WPC",
                "印度无线规划与协调部门认证 (ETA)",
                "1952-01-01",
                "1952-01-01",
                "https://wpc.dot.gov.in",
                "WPC,ETA,印度,无线认证",
                Standard.RiskLevel.MEDIUM,
                Standard.RegulatoryImpact.HIGH,
                Standard.StandardStatus.ACTIVE,
                "IN",
                Arrays.asList("IN"),
                "在印度进口或使用的无线产品"
            ),

            // 7. NBTC
            createStandard(
                "NBTC",
                "无具体版本",
                "NBTC",
                "泰国国家广播电信委员会认证",
                "2010-01-01",
                "2010-01-01",
                "https://www.nbtc.go.th",
                "NBTC,泰国,无线电认证",
                Standard.RiskLevel.MEDIUM,
                Standard.RegulatoryImpact.HIGH,
                Standard.StandardStatus.ACTIVE,
                "TH",
                Arrays.asList("TH"),
                "泰国销售的无线电通信设备"
            ),

            // 8. CE-RED
            createStandard(
                "2014/53/EU",
                "2014/53/EU",
                "CE-RED",
                "欧盟无线电设备指令 (RED)",
                "2014-06-12",
                "2016-06-13",
                "https://ec.europa.eu/growth/sectors/electrical-engineering/red-directive_en",
                "CE,RED,欧盟,无线电设备",
                Standard.RiskLevel.HIGH,
                Standard.RegulatoryImpact.CRITICAL,
                Standard.StandardStatus.ACTIVE,
                "EU",
                Arrays.asList("EU"),
                "欧盟市场销售的无线电设备"
            ),

            // 9. IMDA
            createStandard(
                "IMDA",
                "无版本",
                "IMDA",
                "新加坡信息通信发展局认证",
                "1999-01-01",
                "1999-01-01",
                "https://www.imda.gov.sg",
                "IMDA,新加坡,电信认证",
                Standard.RiskLevel.MEDIUM,
                Standard.RegulatoryImpact.HIGH,
                Standard.StandardStatus.ACTIVE,
                "SG",
                Arrays.asList("SG"),
                "在新加坡使用和进口的电信与无线产品"
            ),

            // 10. TELEC/MIC
            createStandard(
                "TELEC/MIC",
                "技术标准符合证明",
                "TELEC/MIC",
                "日本无线电法认证",
                "1950-01-01",
                "1950-01-01",
                "https://www.telec.or.jp",
                "TELEC,MIC,日本,无线电认证",
                Standard.RiskLevel.HIGH,
                Standard.RegulatoryImpact.HIGH,
                Standard.StandardStatus.ACTIVE,
                "JP",
                Arrays.asList("JP"),
                "日本市场销售的无线电产品"
            ),

            // 11. NCC
            createStandard(
                "NCC",
                "NCC 认证规范",
                "NCC",
                "台湾国家通讯传播委员会认证",
                "2006-01-01",
                "2006-01-01",
                "https://www.ncc.gov.tw",
                "NCC,台湾,无线通信认证",
                Standard.RiskLevel.HIGH,
                Standard.RegulatoryImpact.HIGH,
                Standard.StandardStatus.ACTIVE,
                "TW",
                Arrays.asList("TW"),
                "台湾销售和使用的无线通信设备"
            ),

            // 12. RCM
            createStandard(
                "RCM",
                "电气安全、电磁兼容标准",
                "RCM",
                "澳大利亚与新西兰电气安全与无线认证",
                "2013-01-01",
                "2016-01-01",
                "https://www.acma.gov.au",
                "RCM,澳大利亚,新西兰,电气安全",
                Standard.RiskLevel.HIGH,
                Standard.RegulatoryImpact.HIGH,
                Standard.StandardStatus.ACTIVE,
                "AU",
                Arrays.asList("AU", "NZ"),
                "澳大利亚与新西兰市场"
            ),

            // 13. KCC
            createStandard(
                "KCC",
                "KC 认证",
                "KCC",
                "韩国无线电与通信设备认证",
                "2008-01-01",
                "2008-01-01",
                "https://rra.go.kr",
                "KCC,KC,韩国,无线电认证",
                Standard.RiskLevel.HIGH,
                Standard.RegulatoryImpact.HIGH,
                Standard.StandardStatus.ACTIVE,
                "KR",
                Arrays.asList("KR"),
                "韩国市场无线电与信息通信设备"
            ),

            // 14. SUBTEL
            createStandard(
                "SUBTEL",
                "无版本",
                "SUBTEL",
                "智利电信部认证",
                "1977-01-01",
                "1977-01-01",
                "https://www.subtel.gob.cl",
                "SUBTEL,智利,电信认证",
                Standard.RiskLevel.MEDIUM,
                Standard.RegulatoryImpact.MEDIUM,
                Standard.StandardStatus.ACTIVE,
                "CL",
                Arrays.asList("CL"),
                "智利市场无线通信设备"
            ),

            // 15. SIRIM
            createStandard(
                "SIRIM",
                "ST/SIRIM 认证",
                "SIRIM",
                "马来西亚标准与工业研究院认证",
                "1993-01-01",
                "1993-01-01",
                "https://www.sirim-qas.com.my",
                "SIRIM,马来西亚,无线设备认证",
                Standard.RiskLevel.MEDIUM,
                Standard.RegulatoryImpact.HIGH,
                Standard.StandardStatus.ACTIVE,
                "MY",
                Arrays.asList("MY"),
                "马来西亚进口和销售的无线设备"
            ),

            // 16. TDRA
            createStandard(
                "TDRA",
                "TRA 认证",
                "TDRA",
                "阿联酋电信监管局认证",
                "2003-01-01",
                "2003-01-01",
                "https://www.tdra.gov.ae",
                "TDRA,TRA,阿联酋,电信认证",
                Standard.RiskLevel.MEDIUM,
                Standard.RegulatoryImpact.HIGH,
                Standard.StandardStatus.ACTIVE,
                "AE",
                Arrays.asList("AE"),
                "阿联酋进口和销售的电信设备"
            ),

            // 17. ESMA RoHS
            createStandard(
                "UAE.S 5010-1",
                "UAE.S 5010-1",
                "ESMA RoHS",
                "阿联酋版 RoHS 管控标准",
                "2017-01-01",
                "2018-01-01",
                "https://www.esma.gov.ae",
                "ESMA RoHS,阿联酋,有害物质",
                Standard.RiskLevel.HIGH,
                Standard.RegulatoryImpact.CRITICAL,
                Standard.StandardStatus.ACTIVE,
                "AE",
                Arrays.asList("AE"),
                "阿联酋市场电子电气产品"
            ),

            // 18. MTC
            createStandard(
                "MTC",
                "无版本",
                "MTC",
                "南非莫桑比克国家电信监管机构认证",
                "2000-01-01",
                "2000-01-01",
                "https://www.incm.gov.mz",
                "MTC,莫桑比克,电信认证",
                Standard.RiskLevel.MEDIUM,
                Standard.RegulatoryImpact.MEDIUM,
                Standard.StandardStatus.ACTIVE,
                "MZ",
                Arrays.asList("MZ"),
                "莫桑比克电信与无线设备"
            ),

            // 19. ICASA
            createStandard(
                "ICASA",
                "ICASA 认证标准",
                "ICASA",
                "南非通信管理局认证",
                "2000-01-01",
                "2000-01-01",
                "https://www.icasa.org.za",
                "ICASA,南非,通信认证",
                Standard.RiskLevel.MEDIUM,
                Standard.RegulatoryImpact.HIGH,
                Standard.StandardStatus.ACTIVE,
                "ZA",
                Arrays.asList("ZA"),
                "南非市场的电信与无线电设备"
            ),

            // 20. NRCS LOA
            createStandard(
                "NRCS LOA",
                "LOA 标准",
                "NRCS LOA",
                "南非国家强制规范监管局 LOA（批准证书）",
                "2008-01-01",
                "2008-01-01",
                "https://www.nrcs.org.za",
                "NRCS,LOA,南非,批准证书",
                Standard.RiskLevel.HIGH,
                Standard.RegulatoryImpact.HIGH,
                Standard.StandardStatus.ACTIVE,
                "ZA",
                Arrays.asList("ZA"),
                "南非进口和销售的电子电气产品"
            ),

            // 21. MOE
            createStandard(
                "MOE",
                "无具体标准",
                "MOE",
                "柬埔寨教育部无线电设备许可",
                "2000-01-01",
                "2000-01-01",
                null,
                "MOE,柬埔寨,无线电许可",
                Standard.RiskLevel.MEDIUM,
                Standard.RegulatoryImpact.MEDIUM,
                Standard.StandardStatus.ACTIVE,
                "KH",
                Arrays.asList("KH"),
                "柬埔寨市场无线产品"
            ),

            // 22. MOC
            createStandard(
                "MOC",
                "MIC/MOC 认证标准",
                "MOC",
                "越南信息通信部认证",
                "2009-01-01",
                "2009-01-01",
                "https://www.mic.gov.vn",
                "MOC,MIC,越南,通信认证",
                Standard.RiskLevel.MEDIUM,
                Standard.RegulatoryImpact.HIGH,
                Standard.StandardStatus.ACTIVE,
                "VN",
                Arrays.asList("VN"),
                "越南市场无线通信设备"
            ),

            // 23. SDPPI
            createStandard(
                "SDPPI",
                "SDPPI 标准",
                "SDPPI",
                "印度尼西亚邮电总局认证",
                "2000-01-01",
                "2000-01-01",
                "https://www.postel.go.id",
                "SDPPI,印度尼西亚,邮电认证",
                Standard.RiskLevel.MEDIUM,
                Standard.RegulatoryImpact.HIGH,
                Standard.StandardStatus.ACTIVE,
                "ID",
                Arrays.asList("ID"),
                "印度尼西亚销售的无线通信设备"
            )
        );
    }

    private Standard createStandard(
            String standardNumber,
            String version,
            String title,
            String description,
            String publishedDate,
            String effectiveDate,
            String downloadUrl,
            String keywords,
            Standard.RiskLevel riskLevel,
            Standard.RegulatoryImpact regulatoryImpact,
            Standard.StandardStatus standardStatus,
            String country,
            List<String> countries,
            String scope) {
        
        Standard standard = new Standard();
        standard.setStandardNumber(standardNumber);
        standard.setVersion(version);
        standard.setTitle(title);
        standard.setDescription(description);
        standard.setPublishedDate(publishedDate);
        standard.setEffectiveDate(effectiveDate);
        standard.setDownloadUrl(downloadUrl);
        standard.setKeywords(keywords);
        standard.setRiskLevel(riskLevel);
        standard.setRegulatoryImpact(regulatoryImpact);
        standard.setStandardStatus(standardStatus);
        standard.setCountry(country);
        standard.setCountries(convertCountriesToJson(countries));
        standard.setScope(scope);
        standard.setIsMonitored(true);
        standard.setCreatedAt(LocalDateTime.now());
        standard.setUpdatedAt(LocalDateTime.now());
        standard.setDeleted(0);
        
        return standard;
    }

    private String convertCountriesToJson(List<String> countries) {
        // 简单的JSON转换，实际项目中可以使用Jackson
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < countries.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append("\"").append(countries.get(i)).append("\"");
        }
        json.append("]");
        return json.toString();
    }
}
