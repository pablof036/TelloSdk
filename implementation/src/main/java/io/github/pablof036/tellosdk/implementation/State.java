package io.github.pablof036.tellosdk.implementation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Stores state about drone
 */
public class State {
    private final Integer pitch;
    private final Integer roll;
    private final Integer yaw;
    private final Integer vgx;
    private final Integer vgy;
    private final Integer vgz;
    private final Integer temph;
    private final Integer templ;
    private final Integer tof;
    private final Integer height;
    private final Integer bat;
    private final Double baro;
    private final Integer time;
    private final Double agx;
    private final Double agy;
    private final Double agz;


    public State(Integer pitch, Integer roll, Integer yaw, Integer vgx, Integer vgy, Integer vgz, Integer temph, Integer templ, Integer tof, Integer height, Integer bat, Double baro, Integer time, Double agx, Double agy, Double agz) {
        this.pitch = pitch;
        this.roll = roll;
        this.yaw = yaw;
        this.vgx = vgx;
        this.vgy = vgy;
        this.vgz = vgz;
        this.temph = temph;
        this.templ = templ;
        this.tof = tof;
        this.height = height;
        this.bat = bat;
        this.baro = baro;
        this.time = time;
        this.agx = agx;
        this.agy = agy;
        this.agz = agz;
    }

    public static State parse(String stateStr) {
        Map<String, String> mapped = mapStateString(stateStr.trim());

        return new State(
                tryInteger(mapped.get("pitch")),
                tryInteger(mapped.get("roll")),
                tryInteger(mapped.get("yaw")),
                tryInteger(mapped.get("vgx")),
                tryInteger(mapped.get("vgy")),
                tryInteger(mapped.get("vgz")),
                tryInteger(mapped.get("templ")),
                tryInteger(mapped.get("temph")),
                tryInteger(mapped.get("tof")),
                tryInteger(mapped.get("h")),
                tryInteger(mapped.get("bat")),
                tryDouble(mapped.get("baro")),
                tryInteger(mapped.get("time")),
                tryDouble(mapped.get("agx")),
                tryDouble(mapped.get("agy")),
                tryDouble(mapped.get("agz"))
        );
    }

    private static Integer tryInteger(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException | NullPointerException ex) {
            return null;
        }
    }

    private static Double tryDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException | NullPointerException ex) {
            return null;
        }
    }

    private static Map<String, String> mapStateString(String stateStr) {
        String withoutSemicolon = stateStr.replaceAll(";", ":");
        String[] splitted = withoutSemicolon.split(":");
        Map<String, String> mapped = new HashMap<>();
        for (int i = 0; i < splitted.length - 1; i += 2) {
            mapped.put(splitted[i], splitted[i + 1]);
        }
        return mapped;
    }

    public Integer getPitch() {
        return pitch;
    }

    public Integer getRoll() {
        return roll;
    }

    public Integer getYaw() {
        return yaw;
    }

    public Integer getVgx() {
        return vgx;
    }

    public Integer getVgy() {
        return vgy;
    }

    public Integer getVgz() {
        return vgz;
    }

    public Integer getTemph() {
        return temph;
    }

    public Integer getTempl() {
        return templ;
    }

    public Integer getTof() {
        return tof;
    }

    public Integer getHeight() {
        return height;
    }

    public Integer getBat() {
        return bat;
    }

    public Double getBaro() {
        return baro;
    }

    public Integer getTime() {
        return time;
    }

    public Double getAgx() {
        return agx;
    }

    public Double getAgy() {
        return agy;
    }

    public Double getAgz() {
        return agz;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return Objects.equals(pitch, state.pitch) && Objects.equals(roll, state.roll) && Objects.equals(yaw, state.yaw) && Objects.equals(vgx, state.vgx) && Objects.equals(vgy, state.vgy) && Objects.equals(vgz, state.vgz) && Objects.equals(temph, state.temph) && Objects.equals(templ, state.templ) && Objects.equals(tof, state.tof) && Objects.equals(height, state.height) && Objects.equals(bat, state.bat) && Objects.equals(baro, state.baro) && Objects.equals(time, state.time) && Objects.equals(agx, state.agx) && Objects.equals(agy, state.agy) && Objects.equals(agz, state.agz);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pitch, roll, yaw, vgx, vgy, vgz, temph, templ, tof, height, bat, baro, time, agx, agy, agz);
    }

    @Override
    public String toString() {
        return "State{" +
                "pitch=" + pitch +
                ", roll=" + roll +
                ", yaw=" + yaw +
                ", vgx=" + vgx +
                ", vgy=" + vgy +
                ", vgz=" + vgz +
                ", temph=" + temph +
                ", templ=" + templ +
                ", tof=" + tof +
                ", height=" + height +
                ", bat=" + bat +
                ", baro=" + baro +
                ", time=" + time +
                ", agx=" + agx +
                ", agy=" + agy +
                ", agz=" + agz +
                '}';
    }
}
