package com.certification.service.common;

import com.certification.entity.common.RegulationNotice;
import com.certification.repository.common.RegulationNoticeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 相关法规通知服务类
 */
@Slf4j
@Service
@Transactional
public class RegulationNoticeService {
    
    @Autowired
    private RegulationNoticeRepository regulationNoticeRepository;
    
    /**
     * 保存法规通知
     */
    public RegulationNotice saveRegulationNotice(RegulationNotice regulationNotice) {
        log.info("保存法规通知: {}", regulationNotice.getNoticeTitle());
        return regulationNoticeRepository.save(regulationNotice);
    }
    
    /**
     * 批量保存法规通知
     */
    public List<RegulationNotice> saveRegulationNotices(List<RegulationNotice> regulationNotices) {
        log.info("批量保存法规通知，数量: {}", regulationNotices.size());
        return regulationNoticeRepository.saveAll(regulationNotices);
    }
    
    /**
     * 根据ID查询法规通知
     */
    public Optional<RegulationNotice> findById(Long id) {
        log.info("根据ID查询法规通知: {}", id);
        return regulationNoticeRepository.findById(id);
    }
    
    /**
     * 根据国家ID查询法规通知
     */
    public List<RegulationNotice> findByCountryId(Long countryId) {
        log.info("根据国家ID查询法规通知: {}", countryId);
        return regulationNoticeRepository.findByCountryId(countryId);
    }
    
    /**
     * 根据法规编号查询
     */
    public Optional<RegulationNotice> findByNoticeNumber(String noticeNumber) {
        log.info("根据法规编号查询: {}", noticeNumber);
        return regulationNoticeRepository.findByNoticeNumber(noticeNumber);
    }
    
    /**
     * 根据发布机构查询
     */
    public List<RegulationNotice> findByIssuingAuthority(String issuingAuthority) {
        log.info("根据发布机构查询: {}", issuingAuthority);
        return regulationNoticeRepository.findByIssuingAuthority(issuingAuthority);
    }
    
    /**
     * 根据生效日期查询
     */
    public List<RegulationNotice> findByEffectiveDate(LocalDate effectiveDate) {
        log.info("根据生效日期查询: {}", effectiveDate);
        return regulationNoticeRepository.findByEffectiveDate(effectiveDate);
    }
    
    /**
     * 查询指定日期范围内的法规通知
     */
    public List<RegulationNotice> findByEffectiveDateBetween(LocalDate startDate, LocalDate endDate) {
        log.info("查询指定日期范围内的法规通知: {} - {}", startDate, endDate);
        return regulationNoticeRepository.findByEffectiveDateBetween(startDate, endDate);
    }
    
    /**
     * 根据发布时间查询
     */
    public List<RegulationNotice> findByPublishTime(LocalDateTime publishTime) {
        log.info("根据发布时间查询: {}", publishTime);
        return regulationNoticeRepository.findByPublishTime(publishTime);
    }
    
    /**
     * 查询指定时间范围内的法规通知
     */
    public List<RegulationNotice> findByPublishTimeBetween(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("查询指定时间范围内的法规通知: {} - {}", startTime, endTime);
        return regulationNoticeRepository.findByPublishTimeBetween(startTime, endTime);
    }
    
    /**
     * 根据标题模糊查询
     */
    public List<RegulationNotice> findByNoticeTitleContaining(String noticeTitle) {
        log.info("根据标题模糊查询: {}", noticeTitle);
        return regulationNoticeRepository.findByNoticeTitleContaining(noticeTitle);
    }
    
    /**
     * 根据内容摘要模糊查询
     */
    public List<RegulationNotice> findByContentSummaryContaining(String contentSummary) {
        log.info("根据内容摘要模糊查询: {}", contentSummary);
        return regulationNoticeRepository.findByContentSummaryContaining(contentSummary);
    }
    
    /**
     * 根据关联HS编码查询
     */
    public List<RegulationNotice> findByRelatedHsCodesContaining(String hsCode) {
        log.info("根据关联HS编码查询: {}", hsCode);
        return regulationNoticeRepository.findByRelatedHsCodesContaining(hsCode);
    }
    
    /**
     * 根据关联产品类型查询
     */
    public List<RegulationNotice> findByRelatedProductTypesContaining(String productType) {
        log.info("根据关联产品类型查询: {}", productType);
        return regulationNoticeRepository.findByRelatedProductTypesContaining(productType);
    }
    
    /**
     * 根据国家ID和发布机构查询
     */
    public List<RegulationNotice> findByCountryIdAndIssuingAuthority(Long countryId, String issuingAuthority) {
        log.info("根据国家ID和发布机构查询: {} - {}", countryId, issuingAuthority);
        return regulationNoticeRepository.findByCountryIdAndIssuingAuthority(countryId, issuingAuthority);
    }
    
    /**
     * 查询最近的法规通知
     */
    public List<RegulationNotice> findRecentNotices() {
        log.info("查询最近的法规通知");
        return regulationNoticeRepository.findRecentNotices();
    }
    
    /**
     * 根据国家ID查询最近的法规通知
     */
    public List<RegulationNotice> findRecentNoticesByCountryId(Long countryId) {
        log.info("根据国家ID查询最近的法规通知: {}", countryId);
        return regulationNoticeRepository.findRecentNoticesByCountryId(countryId);
    }
    
    /**
     * 根据发布机构查询最近的法规通知
     */
    public List<RegulationNotice> findRecentNoticesByIssuingAuthority(String issuingAuthority) {
        log.info("根据发布机构查询最近的法规通知: {}", issuingAuthority);
        return regulationNoticeRepository.findRecentNoticesByIssuingAuthority(issuingAuthority);
    }
    
    /**
     * 查询即将生效的法规通知
     */
    public List<RegulationNotice> findUpcomingNotices(LocalDate startDate, LocalDate endDate) {
        log.info("查询即将生效的法规通知: {} - {}", startDate, endDate);
        return regulationNoticeRepository.findUpcomingNotices(startDate, endDate);
    }
    
    /**
     * 根据国家ID列表查询法规通知
     */
    public List<RegulationNotice> findByCountryIdIn(List<Long> countryIds) {
        log.info("根据国家ID列表查询法规通知: {}", countryIds);
        return regulationNoticeRepository.findByCountryIdIn(countryIds);
    }
    
    /**
     * 根据发布机构列表查询法规通知
     */
    public List<RegulationNotice> findByIssuingAuthorityIn(List<String> issuingAuthorities) {
        log.info("根据发布机构列表查询法规通知: {}", issuingAuthorities);
        return regulationNoticeRepository.findByIssuingAuthorityIn(issuingAuthorities);
    }
    
    /**
     * 根据标题或内容摘要模糊查询
     */
    public List<RegulationNotice> findByKeyword(String keyword) {
        log.info("根据标题或内容摘要模糊查询: {}", keyword);
        return regulationNoticeRepository.findByKeyword(keyword);
    }
    
    /**
     * 查询所有法规通知
     */
    public List<RegulationNotice> findAll() {
        log.info("查询所有法规通知");
        return regulationNoticeRepository.findAll();
    }
    
    /**
     * 更新法规通知
     */
    public RegulationNotice updateRegulationNotice(RegulationNotice regulationNotice) {
        log.info("更新法规通知: {}", regulationNotice.getNoticeTitle());
        if (regulationNotice.getId() == null) {
            throw new IllegalArgumentException("更新法规通知时ID不能为空");
        }
        return regulationNoticeRepository.save(regulationNotice);
    }
    
    /**
     * 删除法规通知
     */
    public void deleteRegulationNotice(Long id) {
        log.info("删除法规通知: {}", id);
        regulationNoticeRepository.deleteById(id);
    }
    
    /**
     * 批量删除法规通知
     */
    public void deleteRegulationNotices(List<Long> ids) {
        log.info("批量删除法规通知: {}", ids);
        regulationNoticeRepository.deleteAllById(ids);
    }
    
    /**
     * 检查法规编号是否存在
     */
    public boolean existsByNoticeNumber(String noticeNumber) {
        return regulationNoticeRepository.findByNoticeNumber(noticeNumber).isPresent();
    }
    
    /**
     * 统计法规通知总数
     */
    public long count() {
        return regulationNoticeRepository.count();
    }
    
    /**
     * 根据发布机构统计数量
     */
    public long countByIssuingAuthority(String issuingAuthority) {
        return regulationNoticeRepository.findByIssuingAuthority(issuingAuthority).size();
    }
}
