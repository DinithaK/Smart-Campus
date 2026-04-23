package com.smartcampus.smartcampusapi.models;

public class SensorReading {

    private String id;        // UUID for this reading event
    private long timestamp;   // Epoch time (ms) when reading was captured
    private double value;     // The actual metric value

    public SensorReading() {}

    public SensorReading(String id, long timestamp, double value) {
        this.id = id;
        this.timestamp = timestamp;
        this.value = value;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
}