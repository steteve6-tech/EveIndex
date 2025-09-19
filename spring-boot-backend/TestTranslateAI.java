import com.certification.analysis.analysisByai.TranslateAI;

public class TestTranslateAI {
    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("    火山引擎翻译服务测试程序");
        System.out.println("==========================================");
        
        TranslateAI translateAI = new TranslateAI();
        
        try {
            // 测试单个文本翻译
            System.out.println("\n测试单个文本翻译:");
            String testText = "Hello World";
            System.out.println("原文: " + testText);
            String result = translateAI.translateSingleText(testText);
            System.out.println("译文: " + result);
            System.out.println("翻译状态: " + (testText.equals(result) ? "❌ 翻译失败" : "✅ 翻译成功"));
            
            // 测试自动翻译
            System.out.println("\n测试自动翻译:");
            String autoText = "Good morning";
            System.out.println("原文: " + autoText);
            String autoResult = translateAI.translateSingleTextAuto(autoText);
            System.out.println("译文: " + autoResult);
            System.out.println("翻译状态: " + (autoText.equals(autoResult) ? "❌ 翻译失败" : "✅ 翻译成功"));
            
        } catch (Exception e) {
            System.err.println("❌ 测试失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            translateAI.destroy();
        }
        
        System.out.println("\n==========================================");
        System.out.println("    测试完成！");
        System.out.println("==========================================");
    }
}

