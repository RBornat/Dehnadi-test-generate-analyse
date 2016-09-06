package uk.ac.mdx.RBornat.Saeedgenerator;

import java.util.Locale;
import java.util.ResourceBundle;

public final class Localizer {
    
    private Localizer() {
    }
    
    public static final Locale locale;
    public static final ResourceBundle messages;

    static {
        locale = Locale.getDefault();
        messages = ResourceBundle.getBundle("MessagesBundle", locale, new UTF8Control());
    }
    
    public static String __(final String key) {
        return messages.getString(key);
    }

}