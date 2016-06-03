package com.seuqarius.squarewheels.storage.sharedpreference;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sequarius on 2016/5/29.
 */
public class SharedPreferenceManager {
    private Map<String, Object> mModels;
    private static SharedPreferenceManager mInstance;

    private SharedPreferenceManager() {
        mModels = new HashMap<>();
    }

    public static synchronized SharedPreferenceManager getInStance() {
        if (mInstance == null) {
            mInstance = new SharedPreferenceManager();
        }
        return mInstance;
    }

    public <T> T getModel(Context context, Class<T> clazz) {
        return getModel(context, clazz, clazz.getSimpleName());
    }

    public <T> T getModel(Context context, Class<T> clazz, String sharedPreferenceName) {
        synchronized (mInstance) {
            T t = null;
            try {
                t = clazz.newInstance();
                String className = t.getClass().getSimpleName();
                if (mModels.containsKey(className)) {
                    t = (T) mModels.get(className);
                } else {
                    t = AutoSharedPreference.newModel(context, clazz, sharedPreferenceName);
                    mModels.put(className, t);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return t;
        }
    }
}
