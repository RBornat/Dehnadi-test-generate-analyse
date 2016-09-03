package uk.ac.mdx.RBornat.Saeedgenerator;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Prefs {

    private static final Preferences prefs = Preferences.userNodeForPackage(Prefs.class);
    
    static String getProp(String key, String defaultvalue) {
        return prefs.get(key, defaultvalue);
    }
    
    static int getProp(String key, int defaultvalue) {
        String v = getProp(key, null);
        if (v==null) return defaultvalue;
        else try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            Utils.showErrorAlert("GUI error: preference key "+TextUtils.enQuote(key)+TextUtils.LineSep+
                                 "returns "+TextUtils.enQuote(v)+","+TextUtils.LineSep+
                                 "which is not an integer-looking string.");
            return defaultvalue;
        }
    }
    
    static byte getProp(String key, byte defaultvalue) {
        return (byte)getProp(key, (int)defaultvalue);
    }
    
    static void putProp(String key, String value) {
        if (value==null) prefs.remove(key);
        else prefs.put(key, value);
        flush();
    }
    
    static void putProp(String key, int value) {
        prefs.putInt(key, value);
        flush();
    }
    
    static void putProp(String key, byte value) {
        putProp(key, (int)value);
    }
    
    static void flush() {
        try {
            prefs.flush();
        } catch (BackingStoreException e) {
            Utils.showErrorAlert("prefs.flush got BackingStoreException "+e);
        }
    }
}
