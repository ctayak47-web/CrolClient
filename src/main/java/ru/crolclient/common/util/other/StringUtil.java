package ru.crolclient.common.util.other;

import net.minecraft.client.util.InputUtil;
import ru.crolclient.api.system.font.Fonts;
import org.lwjgl.glfw.GLFW;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StringUtil {
    public static String randomString(int length) {
        return IntStream.range(0, length)
                .mapToObj(operand -> String.valueOf((char) new Random().nextInt('a', 'z' + 1)))
                .collect(Collectors.joining());
    }

    public static String getBindName(int key) {
        if (key == -1) {
            return "N/A";
        }

        if (key >= GLFW.GLFW_MOUSE_BUTTON_1 && key <= GLFW.GLFW_MOUSE_BUTTON_LAST) {
            return "MOUSE " + (key + 1);
        }

        InputUtil.Key code = InputUtil.Type.KEYSYM.createFromCode(key);
        return code.getTranslationKey()
                .replace("key.keyboard.", "")
                .replace(".", " ")
                .toUpperCase();
    }

    public static String wrap(String input, int width, int size) {
        String[] words = input.split(" ");
        StringBuilder output = new StringBuilder();
        float lineWidth = 0;
        for (String word : words) {
            float wordWidth = Fonts.getSize(size).getStringWidth(word);
            if (lineWidth + wordWidth > width) {
                output.append("\n");
                lineWidth = 0;
            } else if (lineWidth > 0) {
                output.append(" ");
                lineWidth += Fonts.getSize(size).getStringWidth(" ");
            }
            output.append(word);
            lineWidth += wordWidth;
        }
        return output.toString();
    }
}
