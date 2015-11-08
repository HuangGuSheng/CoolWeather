package com.huanggusheng.coolweather.util;

/**
 * Created by Huang on 2015/11/8.
 */
public interface HttpCallBackListener {
    void onFinish(String response);

    void onError(Exception e);

}
