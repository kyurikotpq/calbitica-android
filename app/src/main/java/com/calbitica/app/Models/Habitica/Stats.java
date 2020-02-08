package com.calbitica.app.Models.Habitica;

import com.google.gson.annotations.SerializedName;

public class Stats {
    @SerializedName(value = "class")
    private String classname;

    private Float lvl, hp, exp, mp, gp, maxHealth, toNextLevel, maxMP;

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public Float getLvl() {
        return lvl;
    }

    public void setLvl(Float lvl) {
        this.lvl = lvl;
    }

    public Float getHp() {
        return hp;
    }

    public void setHp(Float hp) {
        this.hp = hp;
    }

    public Float getExp() {
        return exp;
    }

    public void setExp(Float exp) {
        this.exp = exp;
    }

    public Float getMp() {
        return mp;
    }

    public void setMp(Float mp) {
        this.mp = mp;
    }

    public Float getGp() {
        return gp;
    }

    public void setGp(Float gp) {
        this.gp = gp;
    }

    public Float getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(Float maxHealth) {
        this.maxHealth = maxHealth;
    }

    public Float getToNextLevel() {
        return toNextLevel;
    }

    public void setToNextLevel(Float toNextLevel) {
        this.toNextLevel = toNextLevel;
    }

    public Float getMaxMP() {
        return maxMP;
    }

    public void setMaxMP(Float maxMP) {
        this.maxMP = maxMP;
    }
}
