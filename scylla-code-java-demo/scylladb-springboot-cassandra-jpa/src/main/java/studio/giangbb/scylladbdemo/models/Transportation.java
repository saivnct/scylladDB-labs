package studio.giangbb.scylladbdemo.models;

import org.springframework.data.cassandra.core.mapping.Column;

import java.util.Objects;

/**
 * Created by solgo on 06/04/2024
 */

public class Transportation {
    private int wheels;

    @Column("has_engine")
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
