import org.apache.commons.jexl3.*;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: natsumi
 * @CreateTime: 2024-10-13  23:37
 * @Description: ?
 */
public class JexlTest {
    @Test
    public void test(){
        JexlBuilder jexlBuilder = new JexlBuilder();
        Map<String,Object> functions = new HashMap<>();
        functions.put("custom", new MathFunctions());
        jexlBuilder.namespaces(functions);
        JexlEngine jexlEngine = jexlBuilder.create();

        JexlContext jc = new MapContext();
        jc.set("custom",MathFunctions.class);
        String expression = "custom:pow(2, 3)";
        JexlExpression e = jexlEngine.createExpression(expression);
        Object result = e.evaluate(jc);
        System.out.println(result);

    }

    @Test
    public void Mytest(){
        JexlBuilder jexlBuilder = new JexlBuilder();
        Map<String, Object> functions = new HashMap<>();

        // 创建上面类的对象，并起一个名称（下面会使用到名称）
        functions.put("Func", new PriceFunction());
        // 或者不起名直接为null（不是字符串null，不要带引号）： functions.put(null, new PriceFunction());

        jexlBuilder.namespaces(functions);
        JexlEngine jexlEngine = jexlBuilder.create();


        // 表达式想使用对应的函数时需要
        // Func:getMax 上面创建对象起的名称（Func）:函数名称
        String expressionStr = "Func:getMax(a,b) == 4 and Func:getMin(a,b) == 3 and Func:getMaxByList(a,b,c) == 10 and Func:sameNumber(list1,list2,num)";
        // 如果上面创建对象时名称为null即：functions.put(null, new PriceFunction());
        // 则表达式可以直接写函数名称：getMax(a,b) == 4 and getMin(a,b)即可


        JexlExpression expression = jexlEngine.createExpression(expressionStr);
        MapContext mapContext = new MapContext();

        // 给对应的变量赋值
        mapContext.set("a", 3);
        mapContext.set("b", 4);
        mapContext.set("c", 10);
        mapContext.set("list1", "1,2,3,4,5,6,7");
        mapContext.set("list2", "5,6,7,8,9");
        mapContext.set("num", "3");

        Object evaluate = expression.evaluate(mapContext);
        System.out.println(String.format("结果为:%s", evaluate));

    }


}
