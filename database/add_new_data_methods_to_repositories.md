# 为其他Repository添加新增数据查询方法

## 需要修改的Repository文件

为以下Repository接口添加相同的三个方法：

1. `DeviceEventReportRepository.java`
2. `DeviceRegistrationRecordRepository.java`
3. `GuidanceDocumentRepository.java`
4. `CustomsCaseRepository.java`

## 需要添加的方法

在每个Repository接口的末尾（`}` 之前）添加以下代码：

```java
    /**
     * 统计新增数据数量
     */
    long countByIsNew(Boolean isNew);

    /**
     * 查找新增数据（分页）
     */
    org.springframework.data.domain.Page<实体类名> findByIsNew(Boolean isNew, org.springframework.data.domain.Pageable pageable);

    /**
     * 查找已查看的新增数据
     */
    List<实体类名> findByIsNewAndNewDataViewed(Boolean isNew, Boolean newDataViewed);
```

## 具体替换说明

### 1. DeviceEventReportRepository.java
- 实体类名：`DeviceEventReport`

### 2. DeviceRegistrationRecordRepository.java
- 实体类名：`DeviceRegistrationRecord`

### 3. GuidanceDocumentRepository.java
- 实体类名：`GuidanceDocument`

### 4. CustomsCaseRepository.java
- 实体类名：`CustomsCase`

## 示例

以 `DeviceEventReportRepository.java` 为例：

```java
    /**
     * 统计新增数据数量
     */
    long countByIsNew(Boolean isNew);

    /**
     * 查找新增数据（分页）
     */
    org.springframework.data.domain.Page<DeviceEventReport> findByIsNew(Boolean isNew, org.springframework.data.domain.Pageable pageable);

    /**
     * 查找已查看的新增数据
     */
    List<DeviceEventReport> findByIsNewAndNewDataViewed(Boolean isNew, Boolean newDataViewed);
}
```

## 注意事项

- 确保在接口的最后一个方法之后、`}` 之前添加
- 注意替换正确的实体类名
- 保持代码格式一致

## 已完成

✅ DeviceRecallRecordRepository.java
✅ Device510KRepository.java
⏳ DeviceEventReportRepository.java
⏳ DeviceRegistrationRecordRepository.java
⏳ GuidanceDocumentRepository.java
⏳ CustomsCaseRepository.java
