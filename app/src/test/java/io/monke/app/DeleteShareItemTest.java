package io.monke.app;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Example local unit test, which will execute on the development machine (host).
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class DeleteShareItemTest {
    @Test
    public void testDeletion() {
        List<String> shares = new ArrayList<String>() {{
            add("\uD83D\uDCB8Sent");
            add("No money, no honey \uD83C\uDF6F");
        }};
        String src = "No money, no honey \uD83C\uDF6F hello world \uD83D\uDCB8Sent";

        int pos = src.length() - 1;
        Stack<String> stack = new Stack<>();
        int toRemove = 0;
        for (; pos >= 0; pos--) {
            stack.push(src.substring(pos, pos + 1));
            StringBuilder tmp = new StringBuilder();
            Stack<String> cp = ((Stack<String>) stack.clone());
            while (!cp.isEmpty()) {
                tmp.append(cp.pop());
            }

            final String test = tmp.toString();
            for (String item : shares) {

                if (test.length() >= item.length() && test.substring(0, item.length()).equals(item)) {
                    pos = 0;
                    stack.clear();
                    toRemove = test.length() + 1;
                    break;

                }
            }
        }

        System.out.println(String.format("ToRemove %d", toRemove));

    }
}