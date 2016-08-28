package uk.ac.mdx.RBornat.Saeedgenerator;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

class EnumUtils<T extends Enum<T>> /* extends AbstractEnumThingy<T> */ {
    /* after 24 hours of useless experiment (see AbstractEnumThingy) I conclude that 
     * reflection won't do it. An argument to the constructor is the only way.
     */
    final private Class<T> enumClass;
    
    EnumUtils(Class<T> enumClass) {
        this.enumClass = enumClass;
    }
        
    T showOptionDialog(String title, String message, T defaultval) {
        EnumSet<T> eset = EnumSet.allOf(enumClass);
        int typeCount = eset.size();
        T[] values = eset.toArray((T[])java.lang.reflect.Array.newInstance(enumClass,typeCount));
        int at = 
            JOptionPane.showOptionDialog(
                null, message, title,
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                values, defaultval);
        if (at==JOptionPane.CLOSED_OPTION)
            return null;
        return values[at];
    }
        
   }