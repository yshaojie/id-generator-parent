package com.self.generator.common.utils;


import com.google.gson.Gson;
import com.self.generator.common.RegisterBean;

/**
 * Created by shaojieyue
 * Created time 2016-05-05 17:51
 */
public class JsonUtil {
    private JsonUtil() {
    }

    public static RegisterBean toObject(String data) {
        Gson gson = new Gson();
        return gson.fromJson(data,RegisterBean.class);
    }

    public static String toJson(RegisterBean registerBean) {
        Gson gson = new Gson();
        return gson.toJson(registerBean);
    }
}
