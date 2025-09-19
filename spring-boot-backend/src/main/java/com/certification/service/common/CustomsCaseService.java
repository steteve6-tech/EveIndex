package com.certification.service.common;

import com.certification.entity.common.CustomsCase;
import com.certification.repository.common.CustomsCaseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 海关过往判例服务类
 */
@Slf4j
@Service
@Transactional
public class CustomsCaseService {
    
    @Autowired
    private CustomsCaseRepository customsCaseRepository;
    
    /**
     * 保存海关判例
     */
    public CustomsCase saveCustomsCase(CustomsCase customsCase) {
        log.info("保存海关判例: {}", customsCase.getCaseNumber());
        return customsCaseRepository.save(customsCase);
    }
    
    /**
     * 批量保存海关判例
     */
    public List<CustomsCase> saveCustomsCases(List<CustomsCase> customsCases) {
        log.info("批量保存海关判例，数量: {}", customsCases.size());
        return customsCaseRepository.saveAll(customsCases);
    }
    
    /**
     * 根据ID查询海关判例
     */
    public Optional<CustomsCase> findById(Long id) {
        log.info("根据ID查询海关判例: {}", id);
        return customsCaseRepository.findById(id);
    }
    
    /**
     * 根据判例编号查询
     */
    public Optional<CustomsCase> findByCaseNumber(String caseNumber) {
        log.info("根据判例编号查询: {}", caseNumber);
        return customsCaseRepository.findByCaseNumber(caseNumber);
    }
    
    /**
     * 根据判例日期查询
     */
    public List<CustomsCase> findByCaseDate(LocalDate caseDate) {
        log.info("根据判例日期查询: {}", caseDate);
        return customsCaseRepository.findByCaseDate(caseDate);
    }
    
    /**
     * 查询指定日期范围内的判例
     */
    public List<CustomsCase> findByCaseDateBetween(LocalDate startDate, LocalDate endDate) {
        log.info("查询指定日期范围内的判例: {} - {}", startDate, endDate);
        return customsCaseRepository.findByCaseDateBetween(startDate, endDate);
    }
    
    /**
     * 根据HS编码查询
     */
    public List<CustomsCase> findByHsCodeUsed(String hsCodeUsed) {
        log.info("根据HS编码查询: {}", hsCodeUsed);
        return customsCaseRepository.findByHsCodeUsed(hsCodeUsed);
    }
    
    /**
     * 根据违规类型查询
     */
    public List<CustomsCase> findByViolationType(String violationType) {
        log.info("根据违规类型查询: {}", violationType);
        return customsCaseRepository.findByViolationType(violationType);
    }
    
    /**
     * 根据处罚金额范围查询
     */
    public List<CustomsCase> findByPenaltyAmountBetween(BigDecimal minAmount, BigDecimal maxAmount) {
        log.info("根据处罚金额范围查询: {} - {}", minAmount, maxAmount);
        return customsCaseRepository.findByPenaltyAmountBetween(minAmount, maxAmount);
    }
    
    /**
     * 查询有处罚金额的判例
     */
    public List<CustomsCase> findByPenaltyAmountIsNotNull() {
        log.info("查询有处罚金额的判例");
        return customsCaseRepository.findByPenaltyAmountIsNotNull();
    }
    
    /**
     * 查询最近的判例
     */
    public List<CustomsCase> findRecentCases() {
        log.info("查询最近的判例");
        return customsCaseRepository.findRecentCases();
    }
    

    
    /**
     * 根据裁定结果模糊查询
     */
    public List<CustomsCase> findByRulingResultContaining(String rulingResult) {
        log.info("根据裁定结果模糊查询: {}", rulingResult);
        return customsCaseRepository.findByRulingResultContaining(rulingResult);
    }
    
    /**
     * 查询所有海关判例
     */
    public List<CustomsCase> findAll() {
        log.info("查询所有海关判例");
        return customsCaseRepository.findAll();
    }
    
    /**
     * 更新海关判例
     */
    public CustomsCase updateCustomsCase(CustomsCase customsCase) {
        log.info("更新海关判例: {}", customsCase.getCaseNumber());
        if (customsCase.getId() == null) {
            throw new IllegalArgumentException("更新海关判例时ID不能为空");
        }
        return customsCaseRepository.save(customsCase);
    }
    
    /**
     * 删除海关判例
     */
    public void deleteCustomsCase(Long id) {
        log.info("删除海关判例: {}", id);
        customsCaseRepository.deleteById(id);
    }
    
    /**
     * 批量删除海关判例
     */
    public void deleteCustomsCases(List<Long> ids) {
        log.info("批量删除海关判例: {}", ids);
        customsCaseRepository.deleteAllById(ids);
    }
    
    /**
     * 检查判例编号是否存在
     */
    public boolean existsByCaseNumber(String caseNumber) {
        return customsCaseRepository.findByCaseNumber(caseNumber).isPresent();
    }
    
    /**
     * 统计海关判例总数
     */
    public long count() {
        return customsCaseRepository.count();
    }
    
    /**
     * 根据违规类型统计数量
     */
    public long countByViolationType(String violationType) {
        return customsCaseRepository.findByViolationType(violationType).size();
    }
    
    /**
     * 根据jd_country查询
     */
    public List<CustomsCase> findByJdCountry(String jdCountry) {
        log.info("根据jd_country查询: {}", jdCountry);
        return customsCaseRepository.findByJdCountry(jdCountry);
    }
    
    /**
     * 根据jd_country和违规类型查询
     */
    public List<CustomsCase> findByJdCountryAndViolationType(String jdCountry, String violationType) {
        log.info("根据jd_country和违规类型查询: {} - {}", jdCountry, violationType);
        return customsCaseRepository.findByJdCountryAndViolationType(jdCountry, violationType);
    }
    
    /**
     * 根据jd_country查询最近的判例
     */
    public List<CustomsCase> findRecentCasesByJdCountry(String jdCountry) {
        log.info("根据jd_country查询最近的判例: {}", jdCountry);
        return customsCaseRepository.findRecentCasesByJdCountry(jdCountry);
    }
    
    /**
     * 根据jd_country和HS编码查询
     */
    public List<CustomsCase> findByJdCountryAndHsCodeUsed(String jdCountry, String hsCodeUsed) {
        log.info("根据jd_country和HS编码查询: {} - {}", jdCountry, hsCodeUsed);
        return customsCaseRepository.findByJdCountryAndHsCodeUsed(jdCountry, hsCodeUsed);
    }
    
    /**
     * 根据HS编码模糊查询（支持多个编码）
     */
    public List<CustomsCase> findByHsCodeUsedContaining(String hsCode) {
        log.info("根据HS编码模糊查询: {}", hsCode);
        return customsCaseRepository.findByHsCodeUsedContaining(hsCode);
    }
    
    /**
     * 根据jd_country和HS编码模糊查询
     */
    public List<CustomsCase> findByJdCountryAndHsCodeUsedContaining(String jdCountry, String hsCode) {
        log.info("根据jd_country和HS编码模糊查询: {} - {}", jdCountry, hsCode);
        return customsCaseRepository.findByJdCountryAndHsCodeUsedContaining(jdCountry, hsCode);
    }
    
    /**
     * 根据章编码查询（前4位）
     */
    public List<CustomsCase> findByChapterCode(String chapterCode) {
        log.info("根据章编码查询: {}", chapterCode);
        return customsCaseRepository.findByChapterCode(chapterCode);
    }
    
    /**
     * 根据jd_country和章编码查询
     */
    public List<CustomsCase> findByJdCountryAndChapterCode(String jdCountry, String chapterCode) {
        log.info("根据jd_country和章编码查询: {} - {}", jdCountry, chapterCode);
        return customsCaseRepository.findByJdCountryAndChapterCode(jdCountry, chapterCode);
    }
    
    /**
     * 检查HS编码是否存在于记录中
     */
    public boolean containsHsCode(String hsCode) {
        log.info("检查HS编码是否存在: {}", hsCode);
        List<CustomsCase> cases = findByHsCodeUsedContaining(hsCode);
        return !cases.isEmpty();
    }
    
    /**
     * 根据HS编码获取所有相关的章编码
     */
    public List<String> getChapterCodesByHsCode(String hsCode) {
        log.info("根据HS编码获取章编码: {}", hsCode);
        List<CustomsCase> cases = findByHsCodeUsedContaining(hsCode);
        return cases.stream()
                .map(customsCase -> HsCodeUtils.parseHsCodes(customsCase.getHsCodeUsed()))
                .flatMap(List::stream)
                .map(HsCodeUtils::getChapterCode)
                .distinct()
                .collect(Collectors.toList());
    }
}
