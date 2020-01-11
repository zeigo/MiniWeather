package com.example.zjj.miniweather;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.zjj.miniweather.heweather.DailyResultBean;
import com.example.zjj.miniweather.heweather.DailyWeatherBean;
import com.example.zjj.miniweather.heweather.NowResultBean;
import com.example.zjj.miniweather.heweather.NowWeatherBean;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private String county;
    private TextView titleCounty;
    private TextView degreeText;
    private TextView weatherInfoText;
    private TextView updateTimeText;
    private LinearLayout forecastLayout;
    private LinearLayout mainLayout;
    List<String> permissionList = new ArrayList<>();
    // LBS
    public LocationClient mlocationClient;
    private SharedPreferences sp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("zjjlog", "main activity created");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mainLayout = findViewById(R.id.activity_main);
        TextView dateTimeText = findViewById(R.id.dateTime);
        dateTimeText.setText(dateString());
        titleCounty = findViewById(R.id.title_county);
        degreeText = findViewById(R.id.degree_text);
        weatherInfoText = findViewById(R.id.weather_info_text);
        updateTimeText = findViewById(R.id.update_time_text);
        forecastLayout = findViewById(R.id.forecast_layout);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        county = sp.getString("county_name", null);
        if (county == null) {
            // 还没有设置地区，即第一次启动时，或者缓存被清空
            mlocationClient = new LocationClient(getApplicationContext());
            mlocationClient.registerLocationListener(new MyLocationListener());
            // 运行时权限，先将没有获得授权的权限进行添加到list，然后集中申请
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.READ_PHONE_STATE);
            }
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (!permissionList.isEmpty()) {
                String[] permissions = permissionList.toArray(new String[permissionList.size()]);
                ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
            } else {
                getLocation();
            }

        } else {
            getNow();
            getDailyForecast();
        }
    }

    private void getLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);
        mlocationClient.setLocOption(option);
        mlocationClient.start();
    }

    /**
     * 处理权限申请结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            // 如果存在某个权限没有处理
                            showShort("必须同意所有权限才能使用本程序");
                            finish();
                        }
                    }
                    getLocation();
                } else {
                    // 发生未知错误
                    showShort("权限申请出现错误");
                    finish();
                }
                break;
            default:
        }
    }

    private void showShort(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 用来自动定位,显示第一次的天气信息
     */
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            county = bdLocation.getDistrict();
            Log.d("zjjlog", "get county: " + county);
            if (county == null) county = "大兴区";
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("county_name", county);
            editor.apply();
            showShort(county + " 定位成功");
            getNow();
            getDailyForecast();
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }

    private void startAutoUpdateService() {
        Intent it = new Intent(this, UpdateNowWeatherService.class);
//        if (Build.VERSION.SDK_INT >= 26) {
//            startForegroundService(it);
//        } else {
            startService(it);
//        }
    }

    /**
     * 添加 actionbar 菜单项
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * 菜单点击事件响应
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.choose_location:
                Intent intent = new Intent(MainActivity.this, LocationActivity.class);
                startActivity(intent);
                break;
            case R.id.setting:
//                 跳转到设置更新频率界面
                Intent intent1 = new Intent(this, AutoUpdateTimeAcitivity.class);
                startActivity(intent1);
                break;
            case R.id.night_model:
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = pref.edit();
                boolean isNight = pref.getBoolean("isNight", false);
                if (isNight) {
                    // 如果已经是夜间模式
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    recreate();
                    editor.putBoolean("isNight", false);
                    editor.apply();
                } else {
                    // 如果是日间模式
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    recreate();
                    editor.putBoolean("isNight", true);
                    editor.apply();
                }
                break;
        }
        return true;
    }

    private String dateString() {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        String mYear = String.valueOf(c.get(Calendar.YEAR)); // 获取年
        String mMonth = String.valueOf(c.get(Calendar.MONTH) + 1);// 获取月
        if (mMonth.length() == 1) mMonth = "0" + mMonth;
        String mDay = String.valueOf(c.get(Calendar.DAY_OF_MONTH));// 获取日
        String mWay = String.valueOf(c.get(Calendar.DAY_OF_WEEK));//获取星期
        switch (mWay) {
            case "1":
                mWay = "日";
                break;
            case "2":
                mWay = "一";
                break;
            case "3":
                mWay = "二";
                break;
            case "4":
                mWay = "三";
                break;
            case "5":
                mWay = "四";
                break;
            case "6":
                mWay = "五";
                break;
            case "7":
                mWay = "六";
                break;
        }
        return mMonth + "月" + mDay + "日 周" + mWay + " " + new Lunar(c).toString();
    }

    // 如果已经存有今天的天气，直接取出并展示；如果没有，网络上获取，存储再展示
    private void getDailyForecast() {
        String dailyResp = sp.getString("dailyWeather", null);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        final String today = sdf.format(new Date());
        if (dailyResp != null && dailyResp.startsWith(today)) {
            Log.d("zjj", "no need to fetch forecast again");
            dailyResp = dailyResp.substring(dailyResp.indexOf(' ') + 1); // 第一个空格之后
            showDaily(HttpUtil.handleDailyResp(dailyResp));
        } else {
            // https://free-api.heweather.net/s6/weather/forecast?location=CN101011100&key=8ad3b3cb4ae1492d8b1567e444ff2c36
            String dailyUrl = getString(R.string.hefeng_apiprefix) + getString(R.string.hefeng_forecast)
                    + "?location=" + county
                    + "&key=" + getString(R.string.web_key);
            HttpUtil.sendOkHttpGet(dailyUrl, new okhttp3.Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String resp = response.body().string();
                    final DailyResultBean dailyRes = HttpUtil.handleDailyResp(resp);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (dailyRes != null && dailyRes.getStatus().equals("ok")) {
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("dailyWeather", today + " " + resp);
                                editor.apply();
                                showDaily(dailyRes);
                            } else {
                                showShort("获取当天天气失败");
                            }
                        }
                    });
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "获取当天天气失败", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    // county必须已经初始化
    private void getNow() {
        String resp = sp.getString("nowWeather", null);
        Log.d("zjjlog", "cached nowWeather： " + resp);
        if (resp != null) {
            String updateTimeStr = resp.substring(0, 16);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            try {
                Date updateTimeDate = df.parse(updateTimeStr);
                Date now = new Date();
                Log.d("zjjlog", now + " " + updateTimeDate);
                int autoUpdateTime = sp.getInt("autoUpdateTime", 1); // minute
                if (now.getTime() - updateTimeDate.getTime() < autoUpdateTime * 60 * 1000) {
                    //  not time out
                    showNow(HttpUtil.handleNowResp(resp.substring(17)));
                    return;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // https://free-api.heweather.net/s6/weather/now?location=CN101011100&key=8ad3b3cb4ae1492d8b1567e444ff2c36
        String dailyUrl = getString(R.string.hefeng_apiprefix) + getString(R.string.hefeng_now)
                + "?location=" + county
                + "&key=" + getString(R.string.web_key);
        HttpUtil.sendOkHttpGet(dailyUrl, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String resp = response.body().string();
                final NowResultBean nowRes = HttpUtil.handleNowResp(resp);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (nowRes != null && nowRes.getStatus().equals("ok")) {
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("nowWeather", nowRes.getUpdate().getLoc() + " " + resp);
                            editor.apply();
                            showNow(nowRes);
                        } else {
                            Toast.makeText(MainActivity.this, "获取实时天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(MainActivity.this, "获取实时天气信息失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDaily(DailyResultBean dailyRes) {
        forecastLayout.removeAllViews();
        for (DailyWeatherBean forecast : dailyRes.getDaily_forecast()) {
            // 将未来几天的天气添加到视图中
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.weather_forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.data_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxMinText = (TextView) view.findViewById(R.id.max_min_text);
            ImageView weatherPic = (ImageView) view.findViewById(R.id.weather_pic);

            // 动态获取 资源id
            String weatherCode = "w" + forecast.getCond_code_d();
            int resId = getResources().getIdentifier(weatherCode, "drawable", this.getPackageName());
            if (resId != 0) {
                weatherPic.setImageResource(resId);
            }

            dateText.setText(forecast.getDate().substring(5));
            infoText.setText(forecast.getCond_txt_d());
            maxMinText.setText(forecast.getTmp_min() + " ～ " + forecast.getTmp_max() + "℃");
            forecastLayout.addView(view);
        }
    }

    private void showNow(NowResultBean nowRes) {
        titleCounty.setText(county);
        NowWeatherBean weather = nowRes.getNow();
        degreeText.setText(weather.getTmp());
        weatherInfoText.setText(weather.getCond_txt());
        updateTimeText.setText("数据发布于：" + nowRes.getUpdate().getLoc().split(" ")[1]);
        mainLayout.setVisibility(View.VISIBLE);
        startAutoUpdateService();
    }
}
