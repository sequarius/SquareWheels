package com.seuqarius.squarewheels.storage.sharedpreference;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReflectUtil {
    public static Object setFiledAndValue(Map<String, Object> map, Class<?> clazz)
            throws SecurityException, IllegalArgumentException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException {

        Object object = setFiledAndValue(map, clazz.newInstance(), clazz);

        return object;
    }

    public static <T> T setFiledAndValue(Map<String, Object> map, T object, Class targetClazz)
            throws SecurityException, IllegalArgumentException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException {

        java.lang.reflect.Field[] fields = targetClazz.getDeclaredFields();

        for (java.lang.reflect.Field f : fields)
            if (f.getAnnotation(Ignore.class) == null) {
                try {
                    if (map.containsKey(f.getName()))
                        setObjectValue(object, targetClazz, f.getName(), map.get(f.getName()));
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

        return object;
    }

    public static List<String> getFiledName(Class<?> clazz) {
        java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
        List<String> list = new ArrayList<>();

        for (java.lang.reflect.Field f : fields)
            if (f.getAnnotation(Ignore.class) == null)
                list.add(f.getName());

        return list;
    }

    public static Map<String, Field> getFiled(Class<?> clazz)
            throws SecurityException, IllegalArgumentException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
        Map<String, Field> map = new HashMap<>();

        for (java.lang.reflect.Field f : fields)
            if (f.getAnnotation(Ignore.class) == null) {
                Field fe = new Field();
                fe.setFieldName(f.getName());
                fe.setType(f.getType());
                fe.setGenericType(getGenericType(f));
                map.put(f.getName(), fe);
            }

        return map;
    }

    public static Field getOneFiledAndValueByMethod(Object object, Method method)
            throws SecurityException, IllegalArgumentException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        Class clazz = method.getDeclaringClass();
        java.lang.reflect.Field field = getFiledNameByMethod(clazz, method);
        if (field == null)
            return null;
        Field fe = new Field();
        fe.setFieldName(field.getName());
        fe.setValue(getObjectValue(field.getType(), object, field.getName()));
        fe.setType(field.getType());
        fe.setGenericType(getGenericType(field));

        return fe;
    }

    public static java.lang.reflect.Field getFiledNameByMethod(Class clazz, Method method) {
        java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
        java.lang.reflect.Field field = null;
        for (java.lang.reflect.Field f : fields)
            if (f.getAnnotation(Ignore.class) == null)
                if (toSetter(f.getName()).equals(method.getName()))
                    field = f;

        return field;
    }

    public static Map<String, Field> getFiledAndValue(Object object)
            throws SecurityException, IllegalArgumentException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        Class clazz = object.getClass();
        java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
        Map<String, Field> map = new HashMap<>();

        for (java.lang.reflect.Field f : fields)
            if (f.getAnnotation(Ignore.class) == null) {
                Object resultObject = getObjectValue(f.getType(), object, f.getName());
                Field fe = new Field();
                fe.setFieldName(f.getName());
                fe.setValue(resultObject);
                fe.setType(f.getType());
                fe.setGenericType(getGenericType(f));
                map.put(f.getName(), fe);
            }

        return map;
    }

    public static Object getObjectValue(Class type, Object owner, String fieldname)
            throws SecurityException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Class ownerClass = owner.getClass();

        try {
            Method method = null;
            if (type == boolean.class) {
                method = ownerClass.getMethod(toIs(fieldname));
            } else {
                method = ownerClass.getMethod(toGetter(fieldname));
            }
            Object object = method.invoke(owner);

            return object;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setObjectValue(Object owner, Class targetClazz, String fieldName, Object value)
            throws SecurityException, NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Method[] method = null;
        method = targetClazz.getMethods();
        for (Method m : method)
            if (m.getName().equals(toSetter(fieldName)))
                try {
                    m.invoke(owner, value);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
    }

    private static Class getGenericType(java.lang.reflect.Field f) {
        if (f.getType().isAssignableFrom(List.class) ||
                f.getType().isAssignableFrom(Set.class)) {
            Type fc = f.getGenericType();
            if (fc == null) return null;
            if (fc instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) fc;
                return (Class) pt.getActualTypeArguments()[0];
            }
        } else if (f.getType().isAssignableFrom(Map.class)) {
            Type fc = f.getGenericType();
            if (fc == null) return null;
            if (fc instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) fc;
                return (Class) pt.getActualTypeArguments()[1];
            }
        }
        return null;
    }

    public static String toGetter(String fieldname) {
        if (fieldname == null || fieldname.length() == 0) {
            return null;
        }

    /* If the second char is upper, make 'get' + field name as getter name. For example, eBlog -> geteBlog */
        if (fieldname.length() > 1) {
            String second = fieldname.substring(1, 2);
            if (second.equals(second.toUpperCase())) {
                return new StringBuffer("get").append(fieldname).toString();
            }
        }

    /* Common situation */
        fieldname = new StringBuffer("get").append(fieldname.substring(0, 1).toUpperCase())
                .append(fieldname.substring(1)).toString();

        return fieldname;
    }

    public static String toSetter(String fieldName) {
        if (fieldName == null || fieldName.length() == 0) {
            return null;
        }

    /* If the second char is upper, make 'set' + field name as getter name. For example, eBlog -> seteBlog */
        if (fieldName.length() > 2) {
            String second = fieldName.substring(1, 2);
            if (second.equals(second.toUpperCase())) {
                return new StringBuffer("set").append(fieldName).toString();
            }
        }

    /* Common situation */
        fieldName = new StringBuffer("set").append(fieldName.substring(0, 1).toUpperCase())
                .append(fieldName.substring(1)).toString();

        return fieldName;
    }

    public static String toIs(String fieldName) {
        if (fieldName == null || fieldName.length() == 0) {
            return null;
        }

        if (fieldName.startsWith("is") || fieldName.startsWith("iS") || fieldName.startsWith("IS"))
            return fieldName;
        if (fieldName.startsWith("Is"))
            return new StringBuffer("i").append(fieldName.substring(1)).toString();

        if (fieldName.length() > 1) {
            String second = fieldName.substring(1, 2);
            if (second.equals(second.toUpperCase())) {
                return new StringBuffer("is").append(fieldName).toString();
            }
        }

    /* Common situation */
        fieldName = new StringBuffer("is").append(fieldName.substring(0, 1).toUpperCase())
                .append(fieldName.substring(1)).toString();

        return fieldName;
    }
}
