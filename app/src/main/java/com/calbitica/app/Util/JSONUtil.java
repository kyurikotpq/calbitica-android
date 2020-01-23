package com.calbitica.app.Util;

import org.json.JSONStringer;

import java.util.HashMap;

public class JSONUtil {
    public static String json(HashMap<String, Object> data) {
        JSONStringer jsonText = new JSONStringer();
        try {
            jsonText.object(); // init an obj

            for(String key : data.keySet()) {
                Object value = data.get(key);
                jsonText.key(key);
                jsonText.value(value);
            }

            jsonText.endObject(); // obj is created

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonText.toString(); // convert to string for sending to server
    }
}
