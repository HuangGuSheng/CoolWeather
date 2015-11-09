package com.huanggusheng.coolweather.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.huanggusheng.coolweather.R;
import com.huanggusheng.coolweather.util.HttpCallBackListener;
import com.huanggusheng.coolweather.util.HttpUtil;
import com.huanggusheng.coolweather.util.Utility;

import org.w3c.dom.Text;

/**
 * Created by Huang on 2015/11/8.
 */
public class WeatherActivity extends Activity implements View.OnClickListener {
    /**
     * 气温区间
     */
    private LinearLayout weather_temp;

    /**
     * 城市名称
     */
    private TextView cityName_tv;

    /**
     * 发布时间
     */
    private TextView publishTime_tv;

    /**
     * 天气情况
     */
    private TextView weatherDesp_tv;

    /**
     * 最低气温
     */
    private TextView temp1_tv;

    /**
     * 最高气温
     */
    private TextView temp2_tv;

    /**
     * 当前日期
     */
    private TextView currentData_tv;

    private ProgressBar progressBar;

    private ImageButton btn_switch;
    private ImageButton btn_refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);

        //初始化各个控件
        weather_temp = (LinearLayout) findViewById(R.id.temp_layout);
        cityName_tv = (TextView) findViewById(R.id.cityname_tv);
        publishTime_tv = (TextView) findViewById(R.id.publish_tv);
        weatherDesp_tv = (TextView) findViewById(R.id.weatherinfo_tv);
        temp1_tv = (TextView) findViewById(R.id.temp1_tv);
        temp2_tv = (TextView) findViewById(R.id.temp2_tv);
        currentData_tv = (TextView) findViewById(R.id.currentdate_tv);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btn_switch = (ImageButton) findViewById(R.id.switch_btn);
        btn_refresh = (ImageButton) findViewById(R.id.refresh_btn);

        btn_switch.setOnClickListener(this);
        btn_refresh.setOnClickListener(this);
        String countryCode = getIntent().getStringExtra("country_code");

        if (!TextUtils.isEmpty(countryCode)) {
            //有县级代号是就去查询天气
            publishTime_tv.setText("查询中");
            progressBar.setVisibility(View.VISIBLE);
            weather_temp.setVisibility(View.INVISIBLE);
            weatherDesp_tv.setVisibility(View.INVISIBLE);
            queryWeatherCode(countryCode);
        } else {
            //没有县级代号是就直接显示本地天气
            showWeather();
        }
    }

    /**
     * 查询县级代号所对应的天气代号
     *
     * @param countryCode
     */
    private void queryWeatherCode(String countryCode) {
        String address = "http://www.weather.com.cn/data/list3/city" +
                countryCode + ".xml";
        queryFromServer(address, "countryCode");
    }

    /**
     * 查询天气代号所对应的天气
     */
    private void queryWeatherInfo(String weatherCode) {
        String address = "http://www.weather.com.cn/data/cityinfo/" +
                weatherCode + ".html";
        queryFromServer(address, "weatherCode");
    }

    /**
     * 根据传入的地址和类型去向服务器查询天气代号或者天气信息
     */
    private void queryFromServer(final String address, final String type) {
        HttpUtil.sendHttpRequest(address, new HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                if ("countryCode".equals(type)) {
                    if (!TextUtils.isEmpty(response)) {
                        //解析出天气代号
                        String[] array = response.split("\\|");
                        if (array != null && array.length == 2) {
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                } else if ("weatherCode".equals(type)) {
                    //处理天气信息
                    Utility.handleWeatherResopnse(WeatherActivity.this, response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                publishTime_tv.setText("同步失败");
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    /**
     * 从SharePreference读取天气信息，并显示到界面上S
     */
    private void showWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        cityName_tv.setText(prefs.getString("city_name", ""));
        temp1_tv.setText(prefs.getString("temp1", ""));
        temp2_tv.setText(prefs.getString("temp2", ""));
        weatherDesp_tv.setText(prefs.getString("weather_desp", ""));
        publishTime_tv.setText("今天" + prefs.getString("publish_time", "") + "发布");
        currentData_tv.setText(prefs.getString("current_date", ""));
        weather_temp.setVisibility(View.VISIBLE);
        weatherDesp_tv.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.switch_btn) {
            //选择查询城市
            Intent intent = new Intent(this, ChoosesAreaActivity.class);
            intent.putExtra("from_weather_activity", true);
            startActivity(intent);
            finish();
        }else if (v.getId() == R.id.refresh_btn) {
            //刷新天气信息
            publishTime_tv.setText("查询中..");
            progressBar.setVisibility(View.VISIBLE);
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            String weatherCode = pref.getString("weather_code", "");
            if (!TextUtils.isEmpty(weatherCode)) {
                queryWeatherInfo(weatherCode);
            }
        }
    }
}
