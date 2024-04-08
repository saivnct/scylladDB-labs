package studio.giangbb.scylladbdemo.models;

import com.datastax.oss.driver.api.mapper.annotations.Entity;

/**
 * Created by solgo on 06/04/2024
 */

@Entity
public class Trasportation {
    private int wheels;
    private boolean hasEngine;


    public Trasportation() {
    }

    public Trasportation(int wheels, boolean hasEngine) {
        this.wheels = wheels;
        this.hasEngine = hasEngine;
    }

    public int getWheels() {
        return wheels;
    }

    public void setWheels(int wheels) {
        this.wheels = wheels;
    }

    public boolean isHasEngine() {
        return hasEngine;
    }

    public void setHasEngine(boolean hasEngine) {
        this.hasEngine = hasEngine;
    }
}
