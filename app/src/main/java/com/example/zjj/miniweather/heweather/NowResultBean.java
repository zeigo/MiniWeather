package com.example.zjj.miniweather.heweather;

/**
 * Created by zjj on 2019/9/27.
 */

public class NowResultBean {
    private BasicBean basic;
    private UpdateBean update;
    private String status;
    private NowWeatherBean now;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public NowWeatherBean getNow() {
        return now;
    }

    public void setNow(NowWeatherBean now) {
        this.now = now;
    }

    public UpdateBean getUpdate() {
        return update;
    }

    public void setUpdate(UpdateBean update) {
        this.update = update;
    }

    public BasicBean getBasic() {
        return basic;
    }

    public void setBasic(BasicBean basic) {
        this.basic = basic;
    }

}
