package com.huanggusheng.coolweather.util;

import android.text.TextUtils;

import com.huanggusheng.coolweather.db.CoolWeatherDB;
import com.huanggusheng.coolweather.model.City;
import com.huanggusheng.coolweather.model.Country;
import com.huanggusheng.coolweather.model.Province;

/**
 * Created by Huang on 2015/11/8.
 * 主要负责分解存入数据
 */
public class Utility {
    /**
     * 解析处理省份数据
     */
    public synchronized static boolean handleProvinceResponse(
            CoolWeatherDB coolWeatherDB, String response) {
        if (!TextUtils.isEmpty(response)) {
            String[] allProvinces = response.split(",");
            if (allProvinces != null &&allProvinces.length > 0) {
                for (String p : allProvinces) {
//                    1、如果用“.”作为分隔的话,必须是如下写法,String.split("\\."),这样才能正确的分隔开,不能用String.split(".");
//                    2、如果用“|”作为分隔的话,必须是如下写法,String.split("\\|"),这样才能正确的分隔开,不能用String.split("|");
//                    “.”和“|”都是转义字符,必须得加"\\";
//                    3、如果在一个字符串中有多个分隔符,可以用“|”作为连字符,比如,“acount=? and uu =? or n=?”,把三个都分隔出来,可以用String.split("and|or");
                    String[] array = p.split("\\|");
                    Province province = new Province();
                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);
                    coolWeatherDB.saveProvince(province);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析并处理城市数据
     * @param coolWeatherDB
     * @param response
     * @return
     */
    public synchronized static boolean handleCityResponse(
            CoolWeatherDB coolWeatherDB, String response,int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            String[] allCities = response.split(",");
            if (allCities != null &&allCities.length > 0) {
                for (String c : allCities) {
                    String[] array = c.split("\\|");
                    City city = new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);
                    coolWeatherDB.saveCity(city);
                }
                return true;
            }
        }
        return false;
    }


    /**
     * 处理并解析县级数据
     * @param coolWeatherDB
     * @param response
     * @param cityId
     * @return
     */
    public synchronized static boolean handleCountryResponse(
            CoolWeatherDB coolWeatherDB, String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            String[] countries = response.split(",");
            if (countries != null && countries.length > 0) {
                for (String c : countries) {
                    String[] array = c.split("\\|");
                    Country country = new Country();
                    country.setCountryCode(array[0]);
                    country.setCountryName(array[1]);
                    country.setCityId(cityId);
                    coolWeatherDB.saveCountry(country);
                }
                return true;
            }
        }
        return false;
    }
}
