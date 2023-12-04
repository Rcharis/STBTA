package com.ta.stb_03;

public class DataPoint {
    private String date;
    private int xValue;
    private int yValue;
    private int Anorganik_Kap;
    private int Organik_Kap;
    private float Anorganik_Kelemb;
    private float Organik_Kelemb;
    private float Anorganik_Suhu;
    private float Organik_Suhu;
    private int hour;
    private String time;
    private float AnorganikSuhu;
    private float OrganikSuhu;
    private float AnorganikKel;
    private float OrganikKel;

    public DataPoint() {
        // Empty constructor for Firebase deserialization
    }

    public DataPoint(String date, String time, int xValue, int yValue, int hour,
                     int Anorganik_Kap, int Organik_Kap,
                     float AnorganikSuhu, float OrganikSuhu,
                     float AnorganikKel, float OrganikKel,
                     float Anorganik_Kelemb, float Anorganik_Suhu,
                     float Organik_Kelemb, float Organik_Suhu) {
        this.date = date;
        this.time = time;
        this.xValue = xValue;
        this.yValue = yValue;
        this.hour = hour;
        this.Anorganik_Kap = Anorganik_Kap;
        this.Organik_Kap = Organik_Kap;
        this.Anorganik_Kelemb = Anorganik_Kelemb;
        this.Organik_Kelemb = Organik_Kelemb;
        this.Anorganik_Suhu = Anorganik_Suhu;
        this.Organik_Suhu = Organik_Suhu;
        this.AnorganikSuhu = AnorganikSuhu;
        this.OrganikSuhu = OrganikSuhu;
        this.AnorganikKel = AnorganikKel;
        this.OrganikKel = OrganikKel;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    public int getxValue() {
        return xValue;
    }
    public void setxValue(int xValue) {
        this.xValue = xValue;
    }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public int getyValue() { return yValue; }
    public void setyValue(int yValue) { this.yValue = yValue; }

    public float getHour() { return hour; }
    public void setHour(int hour) { this.hour = hour; }

    public int getAnorganik_Kap() { return Anorganik_Kap; }
    public void setAnorganik_Kap(int Anorganik_Kap) { this.yValue = Anorganik_Kap; }

    public int getOrganik_Kap() {
        return Organik_Kap;
    }
    public void setOrganik_Kap(int Organik_Kap) {
        this.yValue = Organik_Kap;
    }

    public float getAnorganik_Kelemb() { return Anorganik_Kelemb; }
    public void setAnorganik_Kelemb(float Anorganik_Kelemb) { this.Anorganik_Kelemb = Anorganik_Kelemb; }

    public float getOrganik_Kelemb() {
        return Organik_Kelemb;
    }
    public void setOrganik_Kelemb(float Organik_Kelemb) { this.Organik_Kelemb = Organik_Kelemb; }

    public float getAnorganik_Suhu() { return Anorganik_Suhu; }
    public void setAnorganik_Suhu(float Anorganik_Suhu) { this.Anorganik_Suhu = Anorganik_Suhu; }

    public float getOrganik_Suhu() {
        return Organik_Suhu;
    }
    public void setOrganik_Suhu(float Organik_Suhu) {
        this.Organik_Suhu = Organik_Suhu;
    }

    public float getAnorganikSuhu() { return AnorganikSuhu; }
    public void setAnorganikSuhu(float AnorganikSuhu) { this.AnorganikSuhu = AnorganikSuhu; }

    public float getOrganikSuhu() {
        return OrganikSuhu;
    }
    public void setOrganikSuhu(float OrganikSuhu) {
        this.OrganikSuhu = OrganikSuhu;
    }

    public float getAnorganikKel() {
        return AnorganikKel;
    }
    public void setAnorganikKel(float AnorganikKel) {
        this.AnorganikKel = AnorganikKel;
    }

    public float getOrganikKel() {
        return OrganikKel;
    }
    public void setOrganikKel(float OrganikKel) {
        this.OrganikKel = OrganikKel;
    }

}
