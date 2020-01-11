package com.example.zjj.miniweather.heweather;

import java.util.List;

/**
 * Created by zjj on 2019/9/27.
 */

public class DailyResultBean {
    private BasicBean basic;
    private UpdateBean update;
    private String status;
    private List<DailyWeatherBean> daily_forecast;

    public BasicBean getBasic() {
        return basic;
    }

    public void setBasic(BasicBean basic) {
        this.basic = basic;
    }

    public UpdateBean getUpdate() {
        return update;
    }

    public void setUpdate(UpdateBean update) {
        this.update = update;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<DailyWeatherBean> getDaily_forecast() {
        return daily_forecast;
    }

    public void setDaily_forecast(List<DailyWeatherBean> daily_forecast) {
        this.daily_forecast = daily_forecast;
    }
}
