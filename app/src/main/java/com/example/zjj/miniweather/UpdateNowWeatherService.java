package com.example.zjj.miniweather;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.zjj.miniweather.heweather.NowResultBean;
import com.example.zjj.miniweather.heweather.NowWeatherBean;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;


public class UpdateNowWeatherService extends Service {
    static final int NOTIFY_ID = 1;
    static final String CHANNEL_ID = "channel_1";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("zjjlog", "creating service");
        if (Build.VERSION.SDK_INT >= 26) {
            setForeground();
        }
    }

    @TargetApi(26)
    private void setForeground() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        final String NAME = "更新天气";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, NAME, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setShowBadge(false);
        manager.createNotificationChannel(channel);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
//        Notification notification = new NotificationCompat.Builder(this)
        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("")
                .setContentText("正在后台更新天气")
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentIntent(pi)
                .build();
        startForeground(NOTIFY_ID, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @TargetApi(26)
    private void updateNotification(String newTxt) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("")
                .setContentText(newTxt)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentIntent(pi)
                .build();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFY_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("zjjlog", "start service command");
        updateNowWeather(); // 更新天气数据
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        int autoUpdateTime = sp.getInt("autoUpdateTime", 60);
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int deltaMillis = autoUpdateTime * 60 * 1000; // unit: millisecond
        long triggerAtTime = SystemClock.elapsedRealtime() + deltaMillis;
        Intent i = new Intent(this, UpdateNowWeatherService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("zjjlog", "destroy service");
    }

    private void updateNowWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String countyName = prefs.getString("county_name", null);
        if (countyName != null) {
            String nowUrl = getString(R.string.hefeng_apiprefix) + getString(R.string.hefeng_now)
                    + "?location=" + countyName
                    + "&key=" + getString(R.string.web_key);
            HttpUtil.sendOkHttpGet(nowUrl, new okhttp3.Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String resp = response.body().string();
                    NowResultBean nowRes = HttpUtil.handleNowResp(resp);
                    if (nowRes != null && nowRes.getStatus().equals("ok")) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(UpdateNowWeatherService.this).edit();
                        // store {"nowWeather": "YY-MM-DD hh:mm" + " " + resp}
                        editor.putString("nowWeather", nowRes.getUpdate().getLoc() + " " + resp);
                        editor.apply();
                        NowWeatherBean weather = nowRes.getNow();
                        updateNotification(countyName + " " + weather.getTmp() + "℃ " +
                                weather.getCond_txt());
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
