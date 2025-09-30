public class TestParsing {
    public static void main(String[] args) {
        // 测试US_recall_api的解析逻辑
        String crawlResult = "保存成功: 5 条新记录, 跳过重复: 10 条";
        
        System.out.println("原始字符串: " + crawlResult);
        
        if (crawlResult != null && crawlResult.contains("保存成功:")) {
            try {
                // 解析格式: "保存成功: X 条新记录, 跳过重复: Y 条"
                String[] parts = crawlResult.split("保存成功: | 条新记录, 跳过重复: | 条");
                System.out.println("分割后的部分数量: " + parts.length);
                for (int i = 0; i < parts.length; i++) {
                    System.out.println("parts[" + i + "] = '" + parts[i] + "'");
                }
                
                if (parts.length >= 3) {
                    int savedCount = Integer.parseInt(parts[1].trim());
                    int skippedCount = Integer.parseInt(parts[2].trim());
                    System.out.println("解析结果 - savedCount: " + savedCount + ", skippedCount: " + skippedCount);
                }
            } catch (Exception e) {
                System.err.println("解析失败: " + e.getMessage());
            }
        }
    }
}
