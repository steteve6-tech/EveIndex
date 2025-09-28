# "too many columns" 错误解决方案

## 问题分析

"too many columns" 错误表示导入的数据文件中的列数与目标数据库表的列数不匹配。由于我们最近删除了多个字段，这很可能是导致错误的原因。

## 解决方案

### 方案一：同步远程数据库表结构（推荐）

1. **在远程数据库执行表结构更新脚本**：
   ```sql
   -- 执行以下脚本（按顺序执行）
   -- 1. 删除 t_device_510k 表的字段
   ALTER TABLE t_device_510k DROP COLUMN IF EXISTS decision_date;
   ALTER TABLE t_device_510k DROP COLUMN IF EXISTS device_url;
   ALTER TABLE t_device_510k DROP COLUMN IF EXISTS product_code;
   ALTER TABLE t_device_510k DROP COLUMN IF EXISTS regulation_number;
   ALTER TABLE t_device_510k DROP COLUMN IF EXISTS openfda;

   -- 2. 删除 d_510k_records 表的字段
   ALTER TABLE d_510k_records DROP COLUMN IF EXISTS decision_date;
   ALTER TABLE d_510k_records DROP COLUMN IF EXISTS device_url;

   -- 3. 删除 t_device_event 表的字段
   ALTER TABLE t_device_event DROP COLUMN IF EXISTS adverse_event_flag;
   ALTER TABLE t_device_event DROP COLUMN IF EXISTS event_type;
   -- ... (其他39个字段，参考 remove_device_event_fields.sql)

   -- 4. 删除 t_device_registration 表的字段
   ALTER TABLE t_device_registration DROP COLUMN IF EXISTS risk_class;

   -- 5. 删除 t_crawler_data 表的字段
   ALTER TABLE t_crawler_data DROP COLUMN IF EXISTS risk_description;

   -- 6. 删除 t_device_pma 表（如果存在）
   DROP TABLE IF EXISTS t_device_pma;
   ```

2. **验证表结构**：
   ```sql
   DESCRIBE t_device_510k;
   DESCRIBE d_510k_records;
   DESCRIBE t_device_event;
   DESCRIBE t_device_registration;
   DESCRIBE t_crawler_data;
   ```

### 方案二：重新导出数据

1. **使用当前表结构重新导出**：
   - 确保导出工具使用的是更新后的表结构
   - 重新生成数据导出文件

2. **导出命令示例**：
   ```bash
   # 导出指定表
   mysqldump -h hostname -u username -p database_name table_name > table_name.sql
   
   # 或者只导出数据（不包含结构）
   mysqldump -h hostname -u username -p --no-create-info database_name table_name > table_name_data.sql
   ```

### 方案三：修改导入方式

1. **指定列名导入**：
   ```sql
   LOAD DATA INFILE 'data_file.csv'
   INTO TABLE table_name
   FIELDS TERMINATED BY ','
   ENCLOSED BY '"'
   LINES TERMINATED BY '\n'
   (column1, column2, column3, ...); -- 只指定存在的列
   ```

2. **使用IGNORE选项**：
   ```sql
   LOAD DATA INFILE 'data_file.csv'
   IGNORE INTO TABLE table_name
   FIELDS TERMINATED BY ','
   ENCLOSED BY '"'
   LINES TERMINATED BY '\n';
   ```

### 方案四：数据预处理

1. **编辑数据文件**：
   - 删除文件中多余的列（对应已删除的字段）
   - 确保列顺序与目标表结构一致

2. **使用脚本处理**：
   ```python
   import pandas as pd
   
   # 读取数据文件
   df = pd.read_csv('data_file.csv')
   
   # 删除不存在的列
   columns_to_remove = [
       'decision_date', 'device_url', 'product_code', 
       'regulation_number', 'openfda', 'risk_class', 
       'risk_description'
   ]
   
   for col in columns_to_remove:
       if col in df.columns:
           df = df.drop(columns=[col])
   
   # 保存处理后的文件
   df.to_csv('processed_data.csv', index=False)
   ```

## 推荐执行步骤

### 步骤1：备份远程数据库
```bash
mysqldump -h hostname -u username -p database_name > backup_before_fix.sql
```

### 步骤2：同步表结构
在远程数据库执行所有表结构修改脚本

### 步骤3：验证表结构
确认所有表的结构与本地一致

### 步骤4：重新导入数据
使用更新后的表结构重新导入数据

## 预防措施

1. **版本控制**：
   - 记录数据库表结构变更
   - 使用迁移脚本管理表结构

2. **同步机制**：
   - 确保所有环境的数据库结构一致
   - 定期同步开发、测试、生产环境

3. **导入前检查**：
   - 验证数据文件与目标表结构的匹配性
   - 使用dry-run模式测试导入

## 快速修复脚本

创建 `fix_remote_database_structure.sql`：

```sql
-- 快速修复远程数据库表结构
-- 执行前请先备份数据库！

-- 删除所有已删除的字段
ALTER TABLE t_device_510k 
DROP COLUMN IF EXISTS decision_date,
DROP COLUMN IF EXISTS device_url,
DROP COLUMN IF EXISTS product_code,
DROP COLUMN IF EXISTS regulation_number,
DROP COLUMN IF EXISTS openfda;

ALTER TABLE d_510k_records 
DROP COLUMN IF EXISTS decision_date,
DROP COLUMN IF EXISTS device_url;

-- t_device_event 表的字段删除（部分示例）
ALTER TABLE t_device_event 
DROP COLUMN IF EXISTS adverse_event_flag,
DROP COLUMN IF EXISTS event_type,
DROP COLUMN IF EXISTS brands_list;

-- 其他表...
ALTER TABLE t_device_registration DROP COLUMN IF EXISTS risk_class;
ALTER TABLE t_crawler_data DROP COLUMN IF EXISTS risk_description;

-- 删除不存在的表
DROP TABLE IF EXISTS t_device_pma;
```

执行此脚本后，远程数据库的表结构将与本地保持一致，导入数据时就不会再出现"too many columns"错误。

