package com.certification.service.common;

import com.certification.entity.common.Country;
import com.certification.repository.common.CountryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 国家基础信息服务类
 */
@Slf4j
@Service
@Transactional
public class CountryService {
    
    @Autowired
    private CountryRepository countryRepository;
    
    /**
     * 保存国家信息
     */
    public Country saveCountry(Country country) {
        log.info("保存国家信息: {}", country.getCountryName());
        return countryRepository.save(country);
    }
    
    /**
     * 批量保存国家信息
     */
    public List<Country> saveCountries(List<Country> countries) {
        log.info("批量保存国家信息，数量: {}", countries.size());
        return countryRepository.saveAll(countries);
    }
    
    /**
     * 根据ID查询国家信息
     */
    public Optional<Country> findById(Long id) {
        log.info("根据ID查询国家信息: {}", id);
        return countryRepository.findById(id);
    }
    
    /**
     * 根据国家编码查询
     */
    public Optional<Country> findByCountryCode(String countryCode) {
        log.info("根据国家编码查询: {}", countryCode);
        return countryRepository.findByCountryCode(countryCode);
    }
    
    /**
     * 根据国家名称查询
     */
    public Optional<Country> findByCountryName(String countryName) {
        log.info("根据国家名称查询: {}", countryName);
        return countryRepository.findByCountryName(countryName);
    }
    
    /**
     * 根据地区查询国家列表
     */
    public List<Country> findByRegion(String region) {
        log.info("根据地区查询国家列表: {}", region);
        return countryRepository.findByRegion(region);
    }
    
    /**
     * 根据关键词模糊查询
     */
    public List<Country> findByKeyword(String keyword) {
        log.info("根据关键词模糊查询: {}", keyword);
        return countryRepository.findByKeyword(keyword);
    }
    
    /**
     * 查询所有国家（按名称排序）
     */
    public List<Country> findAllOrderByCountryName() {
        log.info("查询所有国家（按名称排序）");
        return countryRepository.findAllOrderByCountryName();
    }
    
    /**
     * 根据国家编码列表查询
     */
    public List<Country> findByCountryCodeIn(List<String> countryCodes) {
        log.info("根据国家编码列表查询: {}", countryCodes);
        return countryRepository.findByCountryCodeIn(countryCodes);
    }
    
    /**
     * 查询所有国家
     */
    public List<Country> findAll() {
        log.info("查询所有国家");
        return countryRepository.findAll();
    }
    
    /**
     * 更新国家信息
     */
    public Country updateCountry(Country country) {
        log.info("更新国家信息: {}", country.getCountryName());
        if (country.getId() == null) {
            throw new IllegalArgumentException("更新国家信息时ID不能为空");
        }
        return countryRepository.save(country);
    }
    
    /**
     * 删除国家信息
     */
    public void deleteCountry(Long id) {
        log.info("删除国家信息: {}", id);
        countryRepository.deleteById(id);
    }
    
    /**
     * 批量删除国家信息
     */
    public void deleteCountries(List<Long> ids) {
        log.info("批量删除国家信息: {}", ids);
        countryRepository.deleteAllById(ids);
    }
    
    /**
     * 检查国家编码是否存在
     */
    public boolean existsByCountryCode(String countryCode) {
        return countryRepository.findByCountryCode(countryCode).isPresent();
    }
    
    /**
     * 检查国家名称是否存在
     */
    public boolean existsByCountryName(String countryName) {
        return countryRepository.findByCountryName(countryName).isPresent();
    }
    
    /**
     * 统计国家总数
     */
    public long count() {
        return countryRepository.count();
    }
}
