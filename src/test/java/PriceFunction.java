/**
 * @Author: natsumi
 * @CreateTime: 2024-10-13  23:59
 * @Description: ?
 */
public class PriceFunction {
    /**
     * 函数名称
     */
    String functionName() {
        return "priceFunction";
    }

    //获取最大值
    public static Object getMax(int a, int b) {
        return Math.max(a, b);
    }
    //获取最小值
    public static Object getMin(int a, int b) {
        return Math.min(a, b);
    }
    // 获取列表中最大值
    public static Object getMaxByList(Byte... nums) {
        int max = 0;
        // 想要集合就只能收到后转一下,或者参数传String类型,根据指定符号拆一下就可以得到String类型的集合数组
//        List<Byte> list = Arrays.asList(nums);
        for (int i = 0; i < nums.length; i++) {
            if (max < Integer.parseInt(nums[i].toString())) {
                max = Integer.parseInt(nums[i].toString());
            }
        }
        return max;
    }
    // 两个列表中只能有num个相同值验证
    public static Object sameNumber(String list1, String list2, String num) {
        String [] strings1 = list1.split(",");
        String [] strings2 = list2.split(",");
        int n = 0;
        for (int i = 0; i < strings1.length; i++) {
            String s1 = strings1[i];
            for (int j = 0; j < strings2.length; j++) {
                String s2 = strings2[j];
                if (s1.equals(s2)){
                    n++;
                    //超过个数后就可以返回了
                    if(n > Integer.parseInt(num)){
                        return false;
                    }
                    break;
                }
            }
        }
        if (n == Integer.parseInt(num)){
            return true;
        } else {
            return false;
        }
    }
}