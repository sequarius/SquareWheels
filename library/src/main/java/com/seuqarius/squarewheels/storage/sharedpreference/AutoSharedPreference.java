package com.seuqarius.squarewheels.storage.sharedpreference;

import android.content.Context;


import com.google.dexmaker.stock.ProxyBuilder;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sequarius on 2016/5/29.
 */
public class AutoSharedPreference implements InvocationHandler {
    private boolean isCreate = true;
    private Context mContext;
    private String PREF_FILE_NAME;

    public static <T> T newModel(Context context, Class<T> modelClazz, String sharedPrefFileName)
            throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        AutoSharedPreference proxy = new AutoSharedPreference(context, sharedPrefFileName);
        T object = ProxyBuilder.forClass(modelClazz)
                .dexCache(context.getDir("dx", Context.MODE_PRIVATE))
                .handler(proxy)
                .build();

        Map<String, Object> sharedValueMap = SharedPreferenceRepository.getAll(context, sharedPrefFileName);
        Map<String, Field> fieldMap = ReflectUtil.getFiled(modelClazz);
        Map<Field, Object> objectValueMap = new HashMap<>();
        for(String key : fieldMap.keySet()){
            Object value = sharedValueMap.get(key);
            if(value != null)
                objectValueMap.put(fieldMap.get(key), value);
        }
        objectValueMap = DeSerialization.canSaveToMap(objectValueMap);

        Map<String, Object> keyWithValueMap = new HashMap<>();
        for(Field fe : objectValueMap.keySet())
            keyWithValueMap.put(fe.getFieldName(), objectValueMap.get(fe));
        ReflectUtil.setFiledAndValue(keyWithValueMap, object, modelClazz);

        proxy.startProxy();
        return object;
    }

    public AutoSharedPreference(Context context, String sharedPrefFileName) {
        mContext = context;
        PREF_FILE_NAME = sharedPrefFileName;
    }

    public static void saveModel(Context context, Object object, String prefFileName){
        try {
            Map<String, Field> fieldMap = ReflectUtil.getFiled(object.getClass());
            for(String key : fieldMap.keySet()) {
                Field fieldEntity = fieldMap.get(key);
                SharedPreferenceRepository.putValue(context,
                        prefFileName,
                        fieldEntity.getFieldName(),
                        DeSerialization.objectToCanSaveObject(fieldEntity, fieldEntity.getValue()));
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void startProxy(){
        isCreate = false;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = ProxyBuilder.callSuper(proxy, method, args);
        if(isCreate)
            return result;

        if(method.getName().startsWith("set")) {
            Field fieldEntity = ReflectUtil.getOneFiledAndValueByMethod(proxy, method);
            if(fieldEntity == null)
                return result;

            SharedPreferenceRepository.putValue(mContext,
                    PREF_FILE_NAME,
                    fieldEntity.getFieldName(),
                    DeSerialization.objectToCanSaveObject(fieldEntity, fieldEntity.getValue()));
        }
        return result;
    }
}
