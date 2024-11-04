package io.aoitori043.aoitoriproject.config.loader;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @Author: natsumi
 * @CreateTime: 2024-10-25  23:32
 * @Description: ?
 */
@Ignore
public class YamlTest {

    @Test
    public void startTest(){

        String replacedContents = "&aa&&&d";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < replacedContents.length(); i++) {
            char current = replacedContents.charAt(i);
            if (current == '&' && (i == 0 || replacedContents.charAt(i - 1) != '&') &&
                    (i == replacedContents.length() - 1 || !Character.isDigit(replacedContents.charAt(i + 1)) &&
                            "abcdef".indexOf(Character.toLowerCase(replacedContents.charAt(i + 1))) != -1)) {
                // 替换为§
                result.append('§');
            } else {
                result.append(current);
            }
        }
        System.out.println(result.toString());
    }
}
