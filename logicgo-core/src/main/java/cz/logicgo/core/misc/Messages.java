package cz.logicgo.core.misc;

import java.util.Locale;
import java.util.ResourceBundle;

public class Messages {

    private static final ResourceBundle MESSAGES = ResourceBundle.getBundle(
            "cz.logicgo.core.messages.messages",
            Locale.of("cs"),
            Messages.class.getModule()
    );

    public static String getFormatted(String key, Object... args) {
        try {
            String pattern = MESSAGES.getString(key);
            return String.format(pattern, args);
        } catch (Exception e) {
            return key + " ERROR ERROR ERROR";
        }
    }
}
