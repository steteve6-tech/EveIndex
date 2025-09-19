package com.certification.crawler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 关键词配置文件
 * 用于存储和管理爬虫过滤使用的关键词
 */
@Slf4j
@Component
public class KeywordsConfig {

    // 认证相关关键词（补充医疗器械特有的认证术语、监管机构）
    private static final List<String> CERTIFICATION_KEYWORDS = Arrays.asList(
            // 中文关键词
            "认证", "证书", "注册", "备案", "许可", "批准", "认可", "资质",
            "医疗器械", "医疗设备", "二类医疗器械", "三类医疗器械", "CE认证", "FDA认证",
            "NMPA认证", "KFDA认证", "PMDA认证", "CE Class I", "CE Class II", "510(k)认证",
            "监管机构", "FDA", "NMPA", "EUDAMED", "MFDS", "PMDA", "TGA", "ANVISA",
            "合规", "符合标准", "预审核", "医疗注册", "非医疗宣称", "医疗器械分类",

            // 英文关键词
            "certification", "certificate", "registration", "license", "approval", "accreditation",
            "medical device", "medical equipment", "FDA approval", "CE certification", "510(k) clearance",
            "NMPA registration", "PMDA approval", "CE Class I", "CE Class II", "regulatory authority",
            "FDA", "NMPA", "EUDAMED", "MFDS", "TGA", "ANVISA", "compliance", "pre-approval"
    );

    // 产品召回相关关键词（补充医疗器械召回特有的术语）
    private static final List<String> RECALL_KEYWORDS = Arrays.asList(
            // 中文关键词
            "召回", "撤回", "下架", "停止销售", "医疗器械召回", "缺陷", "安全隐患",
            "违规", "处罚", "警告", "风险预警", "FDA召回", "CE违规", "标签不符",
            "未注册", "分类错误", "进口违规", "退货", "投诉", "医疗设备缺陷",

            // 英文关键词
            "recall", "withdraw", "remove from market", "stop sale", "medical device recall",
            "defect", "safety hazard", "violation", "penalty", "warning", "FDA recall",
            "CE non-compliance", "mislabeling", "unregistered", "classification error",
            "import violation", "return", "complaint"
    );

    // 法规通知相关关键词（补充医疗器械法规特有的术语、条款）
    private static final List<String> REGULATION_KEYWORDS = Arrays.asList(
            // 中文关键词
            "法规", "规定", "条例", "政策", "通知", "公告", "医疗器械法规", "MDR",
            "general wellness", "border line product", "医疗器械定义", "分类目录",
            "监管趋严", "法规更新", "生效", "施行", "废止", "医疗用途", "非医疗用途",
            "诊断", "治疗", "皮肤检测", "医疗器械与化妆品边界", "条款修订",

            // 英文关键词
            "regulation", "rule", "policy", "notice", "announcement", "medical device regulation",
            "MDR", "general wellness", "border line product", "device definition", "classification catalog",
            "regulatory tightening", "法规 update", "effective", "enforcement", "repeal",
            "diagnosis", "treatment", "skin analysis", "medical/cosmetic borderline"
    );

    // HS编码相关关键词（补充项目核心HS编码）
    private static final List<String> HS_CODE_KEYWORDS = Arrays.asList(
            "HS编码", "海关编码", "9018", "8543.70", "9031.49", "9027", "8525",
            "HS code", "9018", "8543.70", "9031.49", "9027", "8525"
    );

    // 竞品相关关键词（补充项目核心竞品名称）
    private static final List<String> COMPETITOR_KEYWORDS = Arrays.asList(
            "Visia", "PSI", "PIE", "ISEMECO", "小膚", "OBSERV",
            "Visia skin analyzer", "PSI skin scanner", "小膚检测仪"
    );

    // 产品功能相关关键词（补充皮肤检测类产品特有功能词）
    private static final List<String> PRODUCT_FUNCTION_KEYWORDS = Arrays.asList(
            // 中文关键词
            "皮肤分析", "皮肤检测仪", "3D皮肤成像", "面部成像", "皮肤色素分析",
            "皮肤弹性分析", "皮肤扫描", "皮肤质量检测",

            // 英文关键词
            "Skin Analysis", "Skin Scanner", "3D skin imaging system", "Facial Imaging",
            "Skin pigmentation analysis", "skin elasticity analysis"
    );

    // 标准相关关键词（保持原有基础上补充医疗行业标准）
    private static final List<String> STANDARD_KEYWORDS = Arrays.asList(
            // 中文关键词
            "标准", "规范", "指南", "ISO", "GB", "医疗器械标准", "行业标准",
            "国际标准", "强制性标准", "推荐性标准", "IMDRF指南",

            // 英文关键词
            "standard", "specification", "guideline", "ISO", "medical device standard",
            "industry standard", "international standard", "mandatory standard",
            "IMDRF guideline"
    );

    // 安全相关关键词（补充医疗设备安全特有的术语）
    private static final List<String> SAFETY_KEYWORDS = Arrays.asList(
            // 中文关键词
            "安全", "风险", "防护", "医疗安全", "设备安全", "隐患", "预警",
            "应急", "生物安全", "辐射安全",

            // 英文关键词
            "safety", "risk", "protection", "medical safety", "device safety",
            "hazard", "early warning", "emergency", "biosafety"
    );

    // 环保相关关键词（保持原有，医疗设备相关环保术语）
    private static final List<String> ENVIRONMENTAL_KEYWORDS = Arrays.asList(
            // 中文关键词
            "环保", "环境", "污染", "节能", "减排", "医疗废弃物", "绿色生产",

            // 英文关键词
            "environmental", "pollution", "energy saving", "emission reduction",
            "medical waste", "green production"
    );

    // 产品相关关键词（整合竞品和功能词，补充医疗设备属性）
    private static final List<String> PRODUCT_KEYWORDS = Arrays.asList(
            // 基础产品术语
            "产品", "设备", "仪器", "器械", "模块", "系统", "进口", "出口",
            "品牌", "型号", "规格",

            // 补充医疗设备特有属性
            "家用医疗设备", "专业医疗设备", "美容仪器", "皮肤检测设备", "诊断设备",
            "医疗级设备", "消费级设备",

            // 英文关键词
            "product", "equipment", "instrument", "device", "system", "import", "export",
            "medical device", "cosmetic instrument", "skin analysis device", "diagnostic equipment"
    );

    // 行业相关关键词（补充医疗、美容行业术语）
    private static final List<String> INDUSTRY_KEYWORDS = Arrays.asList(
            // 中文关键词
            "医疗行业", "美容行业", "医疗器械行业", "监管行业", "研发", "生产",
            "销售", "医疗服务", "皮肤护理",

            // 英文关键词
            "medical industry", "cosmetic industry", "medical device industry",
            "regulatory industry", "R&D", "production", "sales"
    );

    // 通用关键词（保持原有，补充风险评估相关术语）
    private static final List<String> GENERAL_KEYWORDS = Arrays.asList(
            // 中文关键词
            "重要", "紧急", "通知", "更新", "变化", "风险评估", "趋势分析",
            "报告", "监测", "分析",

            // 英文关键词
            "important", "urgent", "notice", "update", "change", "risk assessment",
            "trend analysis", "report", "monitoring", "analysis"
    );


    /**
     * 获取认证相关关键词
     */
    public List<String> getCertificationKeywords() {
        return new ArrayList<>(CERTIFICATION_KEYWORDS);
    }
    
    /**
     * 获取召回相关关键词
     */
    public List<String> getRecallKeywords() {
        return new ArrayList<>(RECALL_KEYWORDS);
    }
    
    /**
     * 获取法规相关关键词
     */
    public List<String> getRegulationKeywords() {
        return new ArrayList<>(REGULATION_KEYWORDS);
    }
    
    /**
     * 获取标准相关关键词
     */
    public List<String> getStandardKeywords() {
        return new ArrayList<>(STANDARD_KEYWORDS);
    }
    
    /**
     * 获取安全相关关键词
     */
    public List<String> getSafetyKeywords() {
        return new ArrayList<>(SAFETY_KEYWORDS);
    }
    
    /**
     * 获取环保相关关键词
     */
    public List<String> getEnvironmentalKeywords() {
        return new ArrayList<>(ENVIRONMENTAL_KEYWORDS);
    }
    
    /**
     * 获取产品相关关键词
     */
    public List<String> getProductKeywords() {
        return new ArrayList<>(PRODUCT_KEYWORDS);
    }
    
    /**
     * 获取行业相关关键词
     */
    public List<String> getIndustryKeywords() {
        return new ArrayList<>(INDUSTRY_KEYWORDS);
    }
    
    /**
     * 获取通用关键词
     */
    public List<String> getGeneralKeywords() {
        return new ArrayList<>(GENERAL_KEYWORDS);
    }
    
    /**
     * 获取HS编码相关关键词
     */
    public List<String> getHsCodeKeywords() {
        return new ArrayList<>(HS_CODE_KEYWORDS);
    }
    
    /**
     * 获取竞品相关关键词
     */
    public List<String> getCompetitorKeywords() {
        return new ArrayList<>(COMPETITOR_KEYWORDS);
    }
    
    /**
     * 获取产品功能相关关键词
     */
    public List<String> getProductFunctionKeywords() {
        return new ArrayList<>(PRODUCT_FUNCTION_KEYWORDS);
    }
    
    /**
     * 获取所有关键词
     */
    public List<String> getAllKeywords() {
        List<String> allKeywords = new ArrayList<>();
        allKeywords.addAll(CERTIFICATION_KEYWORDS);
        allKeywords.addAll(RECALL_KEYWORDS);
        allKeywords.addAll(REGULATION_KEYWORDS);
        allKeywords.addAll(HS_CODE_KEYWORDS);
        allKeywords.addAll(COMPETITOR_KEYWORDS);
        allKeywords.addAll(PRODUCT_FUNCTION_KEYWORDS);
        allKeywords.addAll(STANDARD_KEYWORDS);
        allKeywords.addAll(SAFETY_KEYWORDS);
        allKeywords.addAll(ENVIRONMENTAL_KEYWORDS);
        allKeywords.addAll(PRODUCT_KEYWORDS);
        allKeywords.addAll(INDUSTRY_KEYWORDS);
        allKeywords.addAll(GENERAL_KEYWORDS);
        return allKeywords;
    }
    
    /**
     * 根据类别获取关键词
     */
    public List<String> getKeywordsByCategory(String category) {
        switch (category.toLowerCase()) {
            case "certification":
                return getCertificationKeywords();
            case "recall":
                return getRecallKeywords();
            case "regulation":
                return getRegulationKeywords();
            case "hscode":
            case "hs_code":
                return getHsCodeKeywords();
            case "competitor":
                return getCompetitorKeywords();
            case "product_function":
            case "productfunction":
                return getProductFunctionKeywords();
            case "standard":
                return getStandardKeywords();
            case "safety":
                return getSafetyKeywords();
            case "environmental":
                return getEnvironmentalKeywords();
            case "product":
                return getProductKeywords();
            case "industry":
                return getIndustryKeywords();
            case "general":
                return getGeneralKeywords();
            case "all":
                return getAllKeywords();
            default:
                log.warn("未知的关键词类别: {}", category);
                return new ArrayList<>();
        }
    }
    
    /**
     * 获取关键词统计信息
     */
    public Map<String, Integer> getKeywordStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("certification", CERTIFICATION_KEYWORDS.size());
        stats.put("recall", RECALL_KEYWORDS.size());
        stats.put("regulation", REGULATION_KEYWORDS.size());
        stats.put("hscode", HS_CODE_KEYWORDS.size());
        stats.put("competitor", COMPETITOR_KEYWORDS.size());
        stats.put("product_function", PRODUCT_FUNCTION_KEYWORDS.size());
        stats.put("standard", STANDARD_KEYWORDS.size());
        stats.put("safety", SAFETY_KEYWORDS.size());
        stats.put("environmental", ENVIRONMENTAL_KEYWORDS.size());
        stats.put("product", PRODUCT_KEYWORDS.size());
        stats.put("industry", INDUSTRY_KEYWORDS.size());
        stats.put("general", GENERAL_KEYWORDS.size());
        stats.put("total", getAllKeywords().size());
        return stats;
    }
    
    /**
     * 添加自定义关键词
     */
    public void addCustomKeywords(String category, List<String> keywords) {
        log.info("添加自定义关键词到类别 {}: {}", category, keywords);
        // 这里可以实现动态添加关键词的逻辑
        // 目前是静态配置，可以根据需要扩展为动态配置
    }
    
    /**
     * 移除关键词
     */
    public void removeKeywords(String category, List<String> keywords) {
        log.info("从类别 {} 移除关键词: {}", category, keywords);
        // 这里可以实现动态移除关键词的逻辑
    }
    
    /**
     * 搜索关键词
     */
    public List<String> searchKeywords(String searchTerm) {
        List<String> results = new ArrayList<>();
        List<String> allKeywords = getAllKeywords();
        
        for (String keyword : allKeywords) {
            if (keyword.toLowerCase().contains(searchTerm.toLowerCase())) {
                results.add(keyword);
            }
        }
        
        return results;
    }
    
    /**
     * 获取关键词类别列表
     */
    public List<String> getKeywordCategories() {
        return Arrays.asList(
            "certification", "recall", "regulation", "hscode", "competitor", "product_function",
            "standard", "safety", "environmental", "product", "industry", "general", "all"
        );
    }
}
