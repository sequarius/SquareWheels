package com.seuqarius.squarewheels.storage.sharedpreference;


import com.alibaba.fastjson.JSON;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DeSerialization {
    private static final String MAP_KEY = "key";
    private static final String MAP_VALUE = "value";

    private static Map<Class<?>, Convert<?>> mConvertMap;

    protected static <T> void addConvertor(Class<T> clazz, Convert<T> convertor) {
        mConvertMap.put(clazz, convertor);
    }

    public static void reInitConvertor() {
        mConvertMap = new HashMap<>();
        initConvert();
    }

    protected static Map<Class<?>, Convert<?>> getConvertorMap() {
        if (mConvertMap == null)
            synchronized (DeSerialization.class) {
                if (mConvertMap == null) {
                    mConvertMap = new HashMap<>();
                    initConvert();
                }
            }
        return mConvertMap;
    }

    public static Map<Field, Object> canSaveToMap(Map<Field, Object> map) {
        Map<Field, Object> newMap = new HashMap<>();
        for (Field fe : map.keySet())
            newMap.put(fe, canSaveObjectToOriObject(fe, map.get(fe)));
        return newMap;
    }

    public static Object canSaveObjectToOriObject(Field valueType, Object value) {
        Map<Class<?>, Convert<?>> convertorMap = getConvertorMap();
        Class<?> fieldType = valueType.getType();
        if (isBaseClass(fieldType))
            return value;

        if (fieldType == Set.class) {
            Class genericType = valueType.getGenericType();
            if (genericType == String.class || isBaseClass(value.getClass())) {
                return value;
            }

            Set<Object> set = null;
            try {
                JSONArray jsonArray = new JSONArray((String) value);
                set = new HashSet<>();
                for (int i = 0; i < jsonArray.length(); i++)
                    set.add(JSON.parseObject(jsonArray.getString(i), genericType));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return set;
        }
        if (fieldType == Map.class) {
            Class genericType = valueType.getGenericType();
            if (genericType != String.class) {
                Map<String, Object> m = null;
                try {
                    JSONObject jo = new JSONObject((String) value);
                    m = new HashMap<>();
                    Iterator<String> keys = jo.keys();
                    while (keys.hasNext()) {
                        String k = keys.next();
                        String s = jo.getString(k);
                        m.put(k, JSON.parseObject(s, genericType));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return m;
            }
        }
        if (fieldType == List.class) {
            Class genericType = valueType.getGenericType();
            List<Object> list = null;
            try {
                list = new ArrayList<>();
                JSONArray jo = new JSONArray((String) value);
                for (int i = 0; i < jo.length(); i++) {
                    list.add(JSON.parseObject(jo.getString(i), genericType));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return list;
        }

        for (Class<?> c : convertorMap.keySet())
            if (fieldType == c)
                return convertorMap.get(c).string2Object((String) value);
        return JSON.parseObject((String) value, fieldType);
    }

    public static Map<Field, Object> mapObjectToCanSave(Map<Field, Object> map) {
        Map<Field, Object> newMap = new HashMap<>();
        for (Field fe : map.keySet())
            newMap.put(fe, objectToCanSaveObject(fe, map.get(fe)));
        return newMap;
    }

    public static Object objectToCanSaveObject(Field valueType, Object value) {
        Map<Class<?>, Convert<?>> convertorMap = getConvertorMap();
        Class<?> clazz = valueType.getType();
        if (isBaseClass(clazz)) {
            return value;
        }

        if (clazz == Set.class) {
            Class genericType = valueType.getGenericType();
            if (genericType == String.class || value.getClass().isPrimitive()) {
                return value;
            }
            return JSON.toJSON(value).toString();
        }
        if (clazz == Map.class) {
            Class genericType = valueType.getGenericType();
            if (genericType != String.class) {
                return JSON.toJSON(value).toString();
            }
        }
        if (clazz == List.class) {
            return JSON.toJSON(value).toString();
        }

        for (Class<?> c : convertorMap.keySet())
            if (clazz == c)
                return convertorMap.get(c).object2String(value);
        return JSON.toJSON(value).toString();
    }

    private static boolean isBaseClass(Class clazz) {
        return clazz.isPrimitive() ||
                clazz == Integer.class ||
                clazz == Long.class ||
                clazz == Float.class ||
                clazz == Double.class ||
                clazz == Boolean.class ||
                clazz == String.class;
    }

    private static void initConvert() {
        addDateConvert();
        addMapConvert();
    }

    private static void addDateConvert() {
        addConvertor(Date.class,
                new Convert<Date>() {
                    @Override
                    public String object2String(Object object) {
                        return String.valueOf(((Date) object).getTime());
                    }

                    @Override
                    public Date string2Object(String string) {
                        Date date = new Date();
                        if (string != null && !string.equals(""))
                            date.setTime(Long.parseLong(string));
                        return date;
                    }
                });
    }

    private static void addMapConvert() {
        addConvertor(Map.class,
                new Convert<Map>() {
                    @Override
                    public String object2String(Object object) {
                        Map<String, String> map = (Map<String, String>) object;
                        if (map == null || map.size() == 0)
                            return "";
                        JSONArray jsonArray = new JSONArray();
                        for (String key : map.keySet()) {
                            JSONObject jo = new JSONObject();
                            try {
                                jo.put(MAP_KEY, key);
                                jo.put(MAP_VALUE, map.get(key));
                            } catch (JSONException e) {
                                e.printStackTrace();
                                continue;
                            }
                            jsonArray.put(jo);
                        }
                        return jsonArray.toString();
                    }

                    @Override
                    public Map string2Object(String string) {
                        Map<String, String> map = new HashMap<>();
                        try {
                            if (string == null)
                                string = "";
                            JSONArray jsonArray = new JSONArray(string);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jo = jsonArray.getJSONObject(i);
                                map.put(jo.getString(MAP_KEY), jo.optString(MAP_VALUE));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return map;
                    }
                });
    }

    public interface Convert<T> {
        String object2String(Object object);
        T string2Object(String string);
    }
}
