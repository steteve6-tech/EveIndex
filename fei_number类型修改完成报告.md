# fei_number字段类型修改完成报告

## 修改内容

### 1. 数据库迁移文件更新
- **V20241202__.sql** - 将 `fei_number` 字段类型从 `LONGTEXT` 修改为 `VARCHAR(50)`

### 2. 实体类字段注解更新
- **DeviceRegistrationRecord.java** - 将 `feiNumber` 字段注解从 `@Lob @Column(columnDefinition = "TEXT")` 修改为 `@Column(length = 50)`
- **CommonDeviceRegistrationRecord.java** - 将 `feiNumber` 字段长度从 `length = 100` 修改为 `length = 50`

## 修改详情

### fei_number 字段信息
- **字段名**: `fei_number`
- **原数据类型**: `LONGTEXT`
- **新数据类型**: `VARCHAR(50)`
- **是否为空**: `NULL`
- **注释**: `次要标识符（US: fei_number, EU: basic_udi_di）`
- **对应实体字段**: `feiNumber`
- **用途**: 存储次要标识符（US: fei_number, EU: basic_udi_di）

## 数据库执行脚本

已创建 `modify_fei_number_type.sql` 文件，包含完整的字段类型修改SQL语句。

## 执行步骤

1. **备份数据库**（重要！）
2. 执行 `modify_fei_number_type.sql` 中的SQL语句
3. 检查是否有数据超过50个字符（脚本中包含检查语句）
4. 重启Spring Boot应用

## 注意事项

- 修改字段类型前请务必先备份数据库
- 如果现有数据中有超过50个字符的 `fei_number` 值，修改操作可能会失败
- 建议在测试环境中先执行验证
- 执行脚本后会检查是否有数据超过新长度限制

## 验证方法

执行SQL脚本后，可以使用以下命令验证：
```sql
DESCRIBE t_device_registration;
```

确认 `fei_number` 字段类型已修改为 `varchar(50)`。

## 影响评估

修改 `fei_number` 字段类型后：
- 减少了数据库存储空间占用
- 提高了查询性能（VARCHAR比LONGTEXT查询更快）
- 限制了字段长度，防止过长的无效数据
- 需要确保现有数据不超过50个字符

## 数据长度检查

脚本中包含数据长度检查语句：
```sql
SELECT fei_number, LENGTH(fei_number) as length 
FROM t_device_registration 
WHERE fei_number IS NOT NULL 
AND LENGTH(fei_number) > 50;
```

如果查询结果不为空，说明有数据超过50个字符，需要：
1. 截断数据到50个字符，或
2. 调整字段长度限制，或
3. 清理无效的长数据

## 相关文件清单

### 已修改的文件：
1. `spring-boot-backend/migration/V20241202__.sql`
2. `spring-boot-backend/src/main/java/com/certification/entity/common/DeviceRegistrationRecord.java`
3. `spring-boot-backend/src/main/java/com/certification/entity/common/CommonDeviceRegistrationRecord.java`

### 新创建的文件：
1. `modify_fei_number_type.sql` - 数据库字段类型修改脚本
2. `fei_number类型修改完成报告.md` - 本报告

现在所有相关的代码都已经更新，`fei_number` 字段类型已从 `LONGTEXT` 修改为 `VARCHAR(50)`。
