package com.example.zjj.miniweather;

import android.text.TextUtils;

import com.example.zjj.miniweather.db.City;
import com.example.zjj.miniweather.db.County;
import com.example.zjj.miniweather.db.Province;
import com.example.zjj.miniweather.heweather.DailyResultBean;
import com.example.zjj.miniweather.heweather.NowResultBean;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by zjj on 2019/9/27.
 */

public class HttpUtil {
    public static void sendOkHttpGet(String address, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request req = new Request.Builder().url(address).build();
        client.newCall(req).enqueue(callback);
    }

    public static NowResultBean handleNowResp(String resp) {
        try {
            String result = new JSONObject(resp).getJSONArray("HeWeather6").getJSONObject(0).toString();
            return new Gson().fromJson(result, NowResultBean.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static DailyResultBean handleDailyResp(String resp) {
        try {
            String result = new JSONObject(resp).getJSONArray("HeWeather6").getJSONObject(0).toString();
            return new Gson().fromJson(result, DailyResultBean.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean handleProvinceResp(String resp) {
        if(!TextUtils.isEmpty(resp)) {
            try {
                JSONArray allProvinces = new JSONArray(resp);
                for(int i = 0; i < allProvinces.length(); i++) {
                    JSONObject jsonObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceCode(jsonObject.getInt("id"));
                    province.setProvinceName(jsonObject.getString("name"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleCityResp(String resp, int provinceId) {
        if(!TextUtils.isEmpty(resp)) {
            try {
                JSONArray allCities = new JSONArray(resp);
                for(int i = 0; i < allCities.length(); i++) {
                    JSONObject jsonObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityCode(jsonObject.getInt("id"));
                    city.setCityName(jsonObject.getString("name"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleCountyResp(String resp, int cityId) {
        if(!TextUtils.isEmpty(resp)) {
            try {
                JSONArray allCounties = new JSONArray(resp);
                for(int i = 0; i < allCounties.length(); i++) {
                    JSONObject jsonObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(jsonObject.getString("name"));
                    county.setWeatherId(jsonObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
