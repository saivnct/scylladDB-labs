package studio.giangbb.scylladbdemo.entity;

import com.datastax.oss.driver.api.mapper.annotations.Entity;

/**
 * Created by Giangbb on 06/04/2024
 */

@Entity
public class Transportation {
    private int wheels;
    private boolean hasEngine;


    public Transportation() {
    }

    public Transportation(int wheels, boolean hasEngine) {
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


    @Override
    public String toString() {
        return "Transportation{" +
                "wheels=" + wheels +
                ", hasEngine=" + hasEngine +
                '}';
    }
}
