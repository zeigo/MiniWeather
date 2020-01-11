package com.example.zjj.miniweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zjj.miniweather.db.City;
import com.example.zjj.miniweather.db.County;
import com.example.zjj.miniweather.db.Province;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LocationActivity extends AppCompatActivity {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    private PlaceAdapter adapter;
    /**
     * 省列表
     */
    private List<Province> provinceList;

    /**
     * 市列表
     */
    private List<City> cityList;

    /**
     * 县列表
     */
    private List<County> countyList;

    /**
     * 选中的省份
     */
    private Province selectedProvince;

    /**
     * 选中的城市
     */
    private City selectedCity;

    /**
     * 当前选中的级别
     */
    private int currentLevel;
    private SearchView searchView;
    private List<String> places = new ArrayList<>();
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        recyclerView = findViewById(R.id.recycler_view);
        searchView = findViewById(R.id.search);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PlaceAdapter(this, places);
        recyclerView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (TextUtils.isEmpty(query)) {
                    Toast.makeText(LocationActivity.this, "输入不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    places.clear();
                    if (currentLevel == LEVEL_PROVINCE) {
                        for (Province province : provinceList) {
                            if (province.getProvinceName().contains(query)) {
                                places.add(province.getProvinceName());
                            }
                        }
                    } else if (currentLevel == LEVEL_CITY) {
                        for (City city : cityList) {
                            if (city.getCityName().contains(query)) {
                                places.add(city.getCityName());
                            }
                        }
                    } else if (currentLevel == LEVEL_COUNTY) {
                        for (County county : countyList) {
                            if (county.getCountyName().contains(query)) {
                                places.add(county.getCountyName());
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
//                Log.d("zjj", "search text: " + query);
////                if (!TextUtils.isEmpty(query)) {
//                places.clear();
//                if (currentLevel == LEVEL_PROVINCE) {
//                    for (Province province : provinceList) {
//                        if (province.getProvinceName().contains(query)) {
//                            places.add(province.getProvinceName());
//                        }
//                    }
//                } else if (currentLevel == LEVEL_CITY) {
//                    for (City city : cityList) {
//                        if (city.getCityName().contains(query)) {
//                            places.add(city.getCityName());
//                        }
//                    }
//                } else if (currentLevel == LEVEL_COUNTY) {
//                    for (County county : countyList) {
//                        if (county.getCountyName().contains(query)) {
//                            places.add(county.getCountyName());
//                        }
//                    }
//                }
//                adapter.notifyDataSetChanged();
//                } else {
//                }
                return false;
            }
        });
        queryProvinces();

    }

    @Override
    public void onBackPressed() {
        if (currentLevel == LEVEL_COUNTY) {
            queryCities();
        } else if (currentLevel == LEVEL_CITY) {
            queryProvinces();
        } else {
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("zjj", "clicked" + item.getItemId());
        switch (item.getItemId()) {
            case android.R.id.home:
                if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                } else {
                    finish();
                }
                break;
        }
        return true;

    }

    public void selected(String place) {
        if (currentLevel == LEVEL_PROVINCE) {
            for (Province province : provinceList) {
                if (province.getProvinceName().equals(place)) {
                    selectedProvince = province;
                    queryCities();
                    return;
                }
            }


        } else if (currentLevel == LEVEL_CITY) {
            for (City city : cityList) {
                if (city.getCityName().equals(place)) {
                    selectedCity = city;
                    queryCounties();
                    return;
                }
            }

        } else if (currentLevel == LEVEL_COUNTY) {
            Toast.makeText(LocationActivity.this, "已选取" +
                    selectedProvince.getProvinceName() + selectedCity.getCityName() +
                    place, Toast.LENGTH_SHORT).show();
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(LocationActivity.this).edit();
            editor.remove("nowWeather");
            editor.remove("dailyWeather");
            editor.putString("county_name", place);
            editor.apply();
            Intent intent = new Intent(LocationActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryProvinces() {
        actionBar.setTitle("选择省份");
        provinceList = DataSupport.findAll(Province.class);
        currentLevel = LEVEL_PROVINCE;

        if (provinceList.size() > 0) {
            places.clear();
            for (Province province : provinceList) {
                places.add(province.getProvinceName());
            }
            adapter.setData(places);
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    /**
     * 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryCities() {
        actionBar.setTitle(selectedProvince.getProvinceName());
        cityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        currentLevel = LEVEL_CITY;
        if (cityList.size() > 0) {
            places.clear();
            for (City city : cityList) {
                places.add(city.getCityName());
            }
            searchView.setQuery("", false);
            adapter.setData(places);
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    /**
     * 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryCounties() {
        actionBar.setTitle(selectedCity.getCityName());
        countyList = DataSupport.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);
        Log.d("county", String.valueOf(countyList.size()));
        currentLevel = LEVEL_COUNTY;
        if (countyList.size() > 0) {
            places.clear();
            for (County county : countyList) {
                places.add(county.getCountyName());
            }
            adapter.setData(places);
            searchView.setQuery("", false);

        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据。
     */
    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpGet(address, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    result = HttpUtil.handleProvinceResp(responseText);
                } else if ("city".equals(type)) {
                    result = HttpUtil.handleCityResp(responseText, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = HttpUtil.handleCountyResp(responseText, selectedCity.getId());
                }
                if (result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            switch (type) {
                                case "province":
                                    queryProvinces();
                                    break;
                                case "city":
                                    queryCities();
                                    break;
                                case "county":
                                    queryCounties();
                                    break;
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                // 通过runOnUiThread()方法回到主线程处理逻辑
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(LocationActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载...");
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
}
