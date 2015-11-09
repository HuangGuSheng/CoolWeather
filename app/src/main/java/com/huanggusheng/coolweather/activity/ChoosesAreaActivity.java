package com.huanggusheng.coolweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.UtteranceProgressListener;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.huanggusheng.coolweather.R;
import com.huanggusheng.coolweather.db.CoolWeatherDB;
import com.huanggusheng.coolweather.model.City;
import com.huanggusheng.coolweather.model.Country;
import com.huanggusheng.coolweather.model.Province;
import com.huanggusheng.coolweather.util.HttpCallBackListener;
import com.huanggusheng.coolweather.util.HttpUtil;
import com.huanggusheng.coolweather.util.Utility;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Huang on 2015/11/8.
 */
public class ChoosesAreaActivity extends Activity {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTRY = 2;

    private ProgressDialog progressDialog;
    private TextView tv_tittle;
    private ListView lv_list;

    private ArrayAdapter<String> adapter;
    private CoolWeatherDB coolWeatherDB;
    private List<String> dataList = new ArrayList<>();
    /**
     * 省列表
     */
    private List<Province> provinceList;

    /**
     * 城市列表
     */
    private List<City> cityList;

    /**
     * 县列表
     */
    private List<Country> countryList;

    /**
     * 选中的省份
     */
    private Province selectedProvince;

    /**
     * 选中的城市
     */
    private City selectedCity;

    /**
     * 当前列表级别
     */
    private int currentLevel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        if (pref.getBoolean("city_selected", false)) {
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        setContentView(R.layout.choose_area);

        tv_tittle = (TextView) findViewById(R.id.tittle_text);
        lv_list = (ListView) findViewById(R.id.list_view);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
        lv_list.setAdapter(adapter);
        coolWeatherDB = CoolWeatherDB.getInstance(this);
        lv_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                }else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCountries();
                }else if (currentLevel == LEVEL_COUNTRY) {
                    String countryCode = countryList.get(position).getCountryCode();
                    Intent intent = new Intent(ChoosesAreaActivity.this, WeatherActivity.class);
                    intent.putExtra("country_code", countryCode);
                    startActivity(intent);
                    finish();
                }
            }
        });
        queryProvince();        //加载省级数据
    }

    /**
     * 查询全国所有的省，如果本地数据库中没有就去服务器上查
     */
    private void queryProvince() {
        provinceList = coolWeatherDB.loadProvinces();
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            lv_list.setSelection(0);
            tv_tittle.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        } else {
            queryFromServer(null,"province");
        }
    }

    /**
     * 查询选中省份的所有城市，有限从数据库查，如果没有再去服务器上查
     */
    private void queryCities() {
        cityList = coolWeatherDB.loadCity(selectedProvince.getId());
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            lv_list.setSelection(0);
            tv_tittle.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        } else {
            queryFromServer(selectedProvince.getProvinceCode(), "city");
        }
    }

    /**
     * 查询选中城市的所有县，有限从数据库查，如果没有再去服务器上查
     */
    private void queryCountries() {
        countryList = coolWeatherDB.loadCountry(selectedCity.getId());
        if (countryList.size() > 0) {
            dataList.clear();
            for (Country country : countryList) {
                dataList.add(country.getCountryName());
            }
            adapter.notifyDataSetChanged();
            lv_list.setSelection(0);
            tv_tittle.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTRY;
        } else {
            queryFromServer(selectedCity.getCityCode(), "country");
        }
    }

    /**
     * 根据传入的代号和类型从服务器上进行查询
     * @param code
     * @param type
     */
    private void queryFromServer(final String code, final String type) {
        String address;
        if (!TextUtils.isEmpty(code)) {
            address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
        } else {
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }
        showProgressDiaog();
        HttpUtil.sendHttpRequest(address, new HttpCallBackListener() {
            boolean result = false;
            @Override
            public void onFinish(String response) {
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(coolWeatherDB, response);
                }else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(coolWeatherDB, response,
                            selectedProvince.getId());
                }else if ("country".equals(type)) {
                    result = Utility.handleCountryResponse(coolWeatherDB, response,
                            selectedCity.getId());
                }
                if (result) {
                    //通过runOnUiThread方法回到主线程处理逻辑
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvince();
                            }else if ("city".equals(type)) {
                                queryCities();
                            }else if ("country".equals(type)) {
                                queryCountries();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChoosesAreaActivity.this, "加载失败，",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDiaog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载...");

            //Sets whether this dialog is canceled when touched outside the window's bounds.
            // If setting to true, the dialog is set to be cancelable if not already set.
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    /**
     * 捕获Back按键，根据当前列表级别来判断返回省列表还是城市列表
     */
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (currentLevel == LEVEL_COUNTRY) {
            queryCities();
        }else if (currentLevel == LEVEL_CITY) {
            queryProvince();
        } else {
            finish();
        }
    }
}
