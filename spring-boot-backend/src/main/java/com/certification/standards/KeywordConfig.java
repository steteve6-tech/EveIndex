package com.certification.standards;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

import java.util.List;
import java.util.Map;

/**
 * 标准认证关键词配置类
 * 用于统一管理各国家/地区的认证关键词、官方域名、爬虫模板等信息
 */
@Configuration
@ConfigurationProperties(prefix = "certification.keyword") // 支持从 application.yml 动态配置
@Data
public class KeywordConfig {

    /**
     * 分市场认证关键词配置（核心配置）
     * key: 国家/地区编码（如 OVERSEAS、CHINA、EU）
     * value: 对应地区的认证关键词详情
     */
    private Map<String, MarketKeyword> marketKeywords;

    /**
     * 爬虫查询模板（可直接拼接参数使用）
     */
    private CrawlerTemplate crawlerTemplates;

    /**
     * 高权重监测项（2025-2026 重点关注）
     */
    private List<String> highPriorityWatchItems;


    // ========================== 内部静态类：分市场关键词详情 ==========================
    @Data
    public static class MarketKeyword {
        /** 国家/地区名称（如 "全海外/Overseas"、"中国/China"） */
        private String regionName;

        /** 主管机构（如 "Global"、"MIIT/SAMR"、"FCC"） */
        private String authority;

        /** 主要监测关键词（英文，用逗号分隔，如 "RoHS 2.0,RoHS 3,Restriction of Hazardous Substances"） */
        private String primaryKeywords;

        /** 本地语言关键词（多语言用逗号分隔，如 "有害物質限制,RoHS"） */
        private String localLangKeywords;

        /** 延伸/技术词（含必追新制标注，如 "IEC 62321,harmonised"、"GB 26572-2025(必追新制),SJ/T 11364"） */
        private String extendTechTerms;

        /** 官方来源域名（用逗号分隔，如 "eur-lex.europa.eu,ec.europa.eu"） */
        private String officialDomains;

        /** 补充说明（如 "GB/T 39560已替代GB/T 26125，2024-03-01实施"） */
        private String supplement;
    }


    // ========================== 内部静态类：爬虫查询模板 ==========================
    @Data
    public static class CrawlerTemplate {
        /** 官方动态监测模板（占位符 {domain} 替换为对应官方域名） */
        private String officialDynamicTemplate = "(\"notice\" OR \"announcement\" OR 公告 OR 通知) (Wi-Fi OR WLAN OR SRD OR RLAN OR 6GHz OR 6E) site:{domain}";

        /** 法规模块更新模板（默认欧盟RED示例，可根据需求扩展） */
        private String regulationUpdateTemplate = "(harmonised OR 列入協調標準 OR 相容性聲明) (\"2014/53/EU\" OR RED OR EN 300 328 OR EN 18031) site:{domain}";

        /** 频段/功率限制监测模板（占位符 {domain} 替换为对应官方域名） */
        private String frequencyPowerTemplate = "(\"5.925-6.425\" OR 6GHz OR 6E) (限值 OR 发射功率 OR EIRP OR PSD) site:{domain}";

        /** PDF文件筛选模板（占位符 {domain} 替换为对应域名，{yearStart}-{yearEnd} 替换为年份范围） */
        private String pdfFilterTemplate = "site:{domain} filetype:pdf {yearStart}..{yearEnd}";

        /** 配件安全监测模板（占位符 {domain} 替换为对应官方域名） */
        private String accessorySafetyTemplate = "\"62368-1\" OR \"電源適配器 安規\" site:{domain}";

        /** 模组整合监测模板（无占位符，通用） */
        private String moduleIntegrationTemplate = "(module integration OR permissive change OR 組件 認證 延伸) (FCC OR RED OR NCC)";
    }


    // ========================== 初始化默认配置（也可通过 application.yml 覆盖） ==========================
    /**
     * 初始化默认的分市场关键词配置
     * 若需动态调整，可在 application.yml 中配置 certification.keyword.market-keywords 覆盖
     */
    public void initDefaultMarketKeywords() {
        if (this.marketKeywords == null) {
            this.marketKeywords = Map.of(
                    // 1. 全海外/Overseas
                    "OVERSEAS", buildMarketKeyword(
                            "全海外/Overseas",
                            "Global",
                            "RoHS 2.0,RoHS 3,Restriction of Hazardous Substances",
                            "有害物質限制,RoHS",
                            "IEC 62321,harmonised",
                            "eur-lex.europa.eu,ec.europa.eu",
                            "全球通用RoHS标准，重点关注IEC 62321测试方法更新"
                    ),
                    // 2. 中国/China
                    "CHINA", buildMarketKeyword(
                            "中国/China",
                            "MIIT/SAMR",
                            "China RoHS,GB/T 39560,CMIIT ID,SRRC",
                            "中国RoHS,有害物质限制,型号核准",
                            "GB 26572-2025(必追新制),SJ/T 11364",
                            "miit.gov.cn,cnca.gov.cn,samr.gov.cn,std.gov.cn",
                            "GB/T 39560已全面替代GB/T 26125，2024-03-01实施；2025年8月发布GB 26572-2025强标升级"
                    ),
                    // 3. 美国/USA
                    "USA", buildMarketKeyword(
                            "美国/USA",
                            "FCC",
                            "FCC ID,Equipment Authorization,Part 15B,Part 15C,Part 15E",
                            "SDoC,KDB 447498,KDB 996369",
                            "permissive change,grant",
                            "fcc.gov,apps.fcc.gov",
                            "KDB 447498为RF技术文件，KDB 996369为模组整合文件，需关注permissive change（许可变更）动态"
                    ),
                    // 4. 欧盟/EU-RED基础
                    "EU_RED_BASE", buildMarketKeyword(
                            "欧盟/EU",
                            "EC/ETSI",
                            "CE RED 2014/53/EU,EN 300 328,EN 301 893,EN 301 489-1,EN 301 489-17",
                            "協調標準,DoC,NB",
                            "OJEU,EN 62311,EN IEC 62368-1",
                            "ec.europa.eu,eur-lex.europa.eu,etsi.org",
                            "EN IEC 62368-1替代旧版EN 60950，需通过OJEU查询协调标准更新"
                    ),
                    // 5. 欧盟/EU-RED资安新制
                    "EU_RED_CYBER", buildMarketKeyword(
                            "欧盟/EU",
                            "EC",
                            "EN 18031-1,EN 18031-2,EN 18031-3,RED cybersecurity",
                            "資安,隱私,欺詐",
                            "Delegated Reg. (EU) 2022/30,Article 3(3)(d)(e)(f),2025-08-01(必追新制)",
                            "ec.europa.eu",
                            "2022/30法规原适用日延至2025-08-01，现已生效，对应RED第3条第3款(d)(e)(f)资安要求"
                    ),
                    // 6. 台湾/Taiwan
                    "TAIWAN", buildMarketKeyword(
                            "台湾/TW",
                            "NCC",
                            "NCC Type Approval,LP0002,低功率射頻電機",
                            "型式認證,審驗合格標籤",
                            "6GHz 擴頻公告(必追新制),2024-02实施",
                            "ncc.gov.tw",
                            "2024年2月实施6GHz频段扩频，需关注LP0002技术规范更新"
                    ),
                    // 7. 韩国/Korea
                    "KOREA", buildMarketKeyword(
                            "韩国/Korea",
                            "RRA/KC",
                            "KC Conformity,적합등록,적합인증",
                            "국립전파연구원,무선설비",
                            "EMC,전기안전",
                            "rra.go.kr",
                            "现行认证名称为KC(RRA)，非早期\"KCC\"，需修正关键词避免遗漏"
                    ),
                    // 8. 印尼/Indonesia
                    "INDONESIA", buildMarketKeyword(
                            "印尼/IDN",
                            "SDPPI/Kominfo",
                            "SDPPI Type Approval,Postel,Wi-Fi 6E,KEPDIRJEN 161/2022",
                            "認證,公告",
                            "6GHz 規範(必追新制)",
                            "postel.go.id,kominfo.go.id",
                            "依据KEPDIRJEN 161/2022法规管理Wi-Fi 6E，需关注6GHz频段正式实施细节"
                    ),
                    // 其他国家/地区可参考上述格式补充（如印度、泰国、新加坡等）
                    "INDIA", buildMarketKeyword(
                            "印度/India",
                            "WPC",
                            "WPC ETA,Equipment Type Approval",
                            "自我聲明,進口備案",
                            "Wi-Fi,2.4 GHz,5 GHz,6 GHz",
                            "wpc.dot.gov.in",
                            "重点监测2.4/5/6 GHz频段的许可动态"
                    )
            );
        }
    }

    /**
     * 初始化默认高权重监测项
     */
    public void initDefaultHighPriorityItems() {
        if (this.highPriorityWatchItems == null) {
            this.highPriorityWatchItems = List.of(
                    "1. EU EN 18031系列与RED协调标准更新（OJEU公报）",
                    "2. 各国6GHz频段开放及限值调整（NBTC、NCC、IMDA、SDPPI）",
                    "3. 中国RoHS 2.0强标升级（GB 26572-2025）与测试方法更新",
                    "4. 泰国NBTC对ETSI标准的接受进展（Wi-Fi 6E合规依据变更）",
                    "5. 印尼SDPPI 6GHz频段正式实施规范（KEPDIRJEN 161/2022后续更新）"
            );
        }
    }

    /**
     * 构建 MarketKeyword 实例（简化初始化代码）
     */
    private MarketKeyword buildMarketKeyword(
            String regionName, String authority, String primaryKeywords,
            String localLangKeywords, String extendTechTerms, String officialDomains,
            String supplement) {
        MarketKeyword keyword = new MarketKeyword();
        keyword.setRegionName(regionName);
        keyword.setAuthority(authority);
        keyword.setPrimaryKeywords(primaryKeywords);
        keyword.setLocalLangKeywords(localLangKeywords);
        keyword.setExtendTechTerms(extendTechTerms);
        keyword.setOfficialDomains(officialDomains);
        keyword.setSupplement(supplement);
        return keyword;
    }

    /**
     * Bean 初始化时执行默认配置加载
     */
    @PostConstruct
    public void afterPropertiesSet() {
        initDefaultMarketKeywords();
        initDefaultHighPriorityItems();
        if (this.crawlerTemplates == null) {
            this.crawlerTemplates = new CrawlerTemplate();
        }
    }
}