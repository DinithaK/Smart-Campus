package com.smartcampus.smartcampusapi.data;

import com.smartcampus.smartcampusapi.models.Room;
import com.smartcampus.smartcampusapi.models.Sensor;
import com.smartcampus.smartcampusapi.models.SensorReading;
 
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
 
/**
 * Singleton in-memory data store.
 *
 * Because JAX-RS creates a new resource class instance per request by default,
 * shared state MUST live outside the resource class. We use a singleton with
 * ConcurrentHashMap to be thread-safe — multiple simultaneous requests can
 * read/write without causing race conditions or data loss.
 */
public class DataStore {
 
    private static final DataStore INSTANCE = new DataStore();
 
    // Thread-safe maps for all data
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    // readings keyed by sensorId
    private final Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();
 
    private DataStore() {
        // Seed some sample data
        Room r1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room r2 = new Room("LAB-101", "Computer Science Lab", 30);
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);
 
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-001", "CO2", "ACTIVE", 410.0, "LAB-101");
        Sensor s3 = new Sensor("OCC-001", "Occupancy", "MAINTENANCE", 0.0, "LIB-301");
        sensors.put(s1.getId(), s1);
        sensors.put(s2.getId(), s2);
        sensors.put(s3.getId(), s3);
 
        r1.getSensorIds().add(s1.getId());
        r1.getSensorIds().add(s3.getId());
        r2.getSensorIds().add(s2.getId());
 
        sensorReadings.put(s1.getId(), new ArrayList<>());
        sensorReadings.put(s2.getId(), new ArrayList<>());
        sensorReadings.put(s3.getId(), new ArrayList<>());
    }
 
    public static DataStore getInstance() {
        return INSTANCE;
    }
 
    // --- Rooms ---
    public Map<String, Room> getRooms() { return rooms; }
 
    public Room getRoom(String id) { return rooms.get(id); }
 
    public void putRoom(Room room) { rooms.put(room.getId(), room); }
 
    public boolean deleteRoom(String id) {
        return rooms.remove(id) != null;
    }
 
    // --- Sensors ---
    public Map<String, Sensor> getSensors() { return sensors; }
 
    public Sensor getSensor(String id) { return sensors.get(id); }
 
    public void putSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
        sensorReadings.putIfAbsent(sensor.getId(), new ArrayList<>());
    }
 
    public boolean deleteSensor(String id) {
        sensorReadings.remove(id);
        return sensors.remove(id) != null;
    }
 
    // --- Sensor Readings ---
    public List<SensorReading> getReadings(String sensorId) {
        return sensorReadings.getOrDefault(sensorId, new ArrayList<>());
    }
 
    public synchronized void addReading(String sensorId, SensorReading reading) {
        sensorReadings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
    }
}