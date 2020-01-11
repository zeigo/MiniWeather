package com.example.zjj.miniweather;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class AutoUpdateTimeAcitivity extends AppCompatActivity implements View.OnClickListener {
    private Map<Integer, Integer> id2val = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_update_time);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("更新频率");
        }

        id2val.put(R.id.hour1, 1 * 60);
        id2val.put(R.id.hour2, 2 * 60);
        id2val.put(R.id.hour3, 4 * 60);
        id2val.put(R.id.hour4, 8 * 60);
        id2val.put(R.id.hour5, 10 * 60);
        for(int viewId : id2val.keySet()) {
            Button btn = findViewById(viewId);
            btn.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (id2val.containsKey(viewId)) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.putInt("autoUpdateTime", id2val.get(viewId));
            editor.apply();
            Toast.makeText(this, "设置成功", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
        }
        return true;
    }
}
