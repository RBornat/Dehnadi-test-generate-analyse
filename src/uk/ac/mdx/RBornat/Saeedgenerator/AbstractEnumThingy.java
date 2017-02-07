package uk.ac.mdx.RBornat.Saeedgenerator;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractEnumThingy<T extends Enum<T>> {
    protected Class<T> enumClass;
    
    @SuppressWarnings("unchecked")
    AbstractEnumThingy() {
        this.enumClass = (Class<T>) ((ParameterizedType) getClass()  
                .getGenericSuperclass()).getActualTypeArguments()[0]; 
    }

    /*public Class<?> returnedClass {
        return getTypeArguments(AbstractEnumThingy.class, this.getClass()).get(0);
    }*/
    
    // a solution (from the web (http://www.artima.com/weblogs/viewpost.jsp) for the problem of
    // finding out what T is.
    /**
     * Get the underlying class for a type, or null if the type is a variable type.
     * @param type the type
     * @return the underlying class
     */
    @SuppressWarnings("rawtypes")
    public static Class<?> getClassFromType(Type type, Map<Type, Type> resolvedTypes) {
        System.err.println("getClassFromType("+type+")");
      if (type instanceof Class) {
        return (Class) type;
      }
      else 
      if (type instanceof ParameterizedType) {
          System.err.println("-- is ParameterisedType "+((ParameterizedType) type).getRawType());
          ParameterizedType parameterizedType = (ParameterizedType) type;
          Class<?> rawType = (Class) parameterizedType.getRawType();

          Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
          TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
          for (int i = 0; i < actualTypeArguments.length; i++) {
              System.err.println("typeParameter "+typeParameters[i] +
                      ", actualTypeArgument "+actualTypeArguments[i]);
              resolvedTypes.put(typeParameters[i], actualTypeArguments[i]);
          }
        return getClassFromType(((ParameterizedType) type).getRawType(), resolvedTypes);
      }
      else 
      if (type instanceof GenericArrayType) {
          System.err.println("-- is GenericArrayType "+((GenericArrayType) type).getGenericComponentType());
        Type componentType = ((GenericArrayType) type).getGenericComponentType();
        Class<?> componentClass = getClassFromType(componentType, resolvedTypes);
        if (componentClass != null ) {
          return Array.newInstance(componentClass, 0).getClass();
        }
        else {
          return null;
        }
      }
      else {
        return null;
      }
    }

    /**
     * Get the actual type arguments a child class has used to extend a generic base class.
     *
     * @param baseClass the base class
     * @param childClass the child class
     * @return a list of the raw classes for the actual type arguments.
     */
    @SuppressWarnings("rawtypes")
    public static <T> List<Class<?>> getTypeArguments(Class<T> baseClass, Class<? extends T> childClass) {
        System.err.println("getTypeArguments("+baseClass+", "+childClass+")");
        Map<Type, Type> resolvedTypes = new HashMap<Type, Type>();
        Type type = childClass;
        // start walking up the inheritance hierarchy until we hit baseClass
        while (! getClassFromType(type, resolvedTypes).equals(baseClass)) {
            System.err.println("walking");
            if (type instanceof Class) {
                // there is no useful information for us in raw types, so just keep going.
                type = ((Class) type).getGenericSuperclass();
                System.err.println("raw, going up to "+type+" which is "+(type instanceof Class ? "raw": "parameterised"));
            }
            else {
                System.err.println("Parameterized");
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Class<?> rawType = (Class) parameterizedType.getRawType();

                /*Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
                for (int i = 0; i < actualTypeArguments.length; i++) {
                    System.err.println("typeParameter "+typeParameters[i] +
                            ", actualTypeArgument "+actualTypeArguments[i]);
                    resolvedTypes.put(typeParameters[i], actualTypeArguments[i]);
                }*/

                if (!rawType.equals(baseClass)) {
                    System.err.println("otherwise, going up");
                    type = rawType.getGenericSuperclass();
                }
            }
        }
        System.err.println("fixed point");

        // finally, for each actual type argument provided to baseClass, determine (if possible)
        // the raw class for that type argument.
        Type[] actualTypeArguments;
        if (type instanceof Class) {
            System.err.println("is just a class");
            actualTypeArguments = ((Class) type).getTypeParameters();
        }
        else {
            System.err.println("is parameterised");
           actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
        }
        List<Class<?>> typeArgumentsAsClasses = new ArrayList<Class<?>>();
        // resolve types by chasing down type variables.
        for (Type baseType: actualTypeArguments) {
            System.err.println("baseType starts at "+baseType);
            while (resolvedTypes.containsKey(baseType)) {
                System.err.println("resolving "+baseType);
                baseType = resolvedTypes.get(baseType);
                System.err.println("resolved to "+baseType);
            }
            System.err.println("couldn't resolve "+baseType);
            typeArgumentsAsClasses.add(getClassFromType(baseType, resolvedTypes));
        }
        return typeArgumentsAsClasses;
    }        

}
