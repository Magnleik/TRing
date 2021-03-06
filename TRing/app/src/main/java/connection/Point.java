package connection;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Eirik on 15-Feb-18.
 */

public class Point implements Serializable, ClusterItem {

    private int id = -1;
    private Map<String, String> properties;
    private Geometry geometry;
    private boolean isVisited;

    /**
     * Constructor to use when creating a new point on the app - when ID is unknown.
     *
     * @param latitude    Latitude of the point, in WGS84
     * @param longitude   Longitude of the point, in WGS84
     * @param description Description of the point (optional)
     */
    public Point(double latitude, double longitude, String description) {
        this(latitude,longitude,description,null);
    }

    /**
     * Constructor ONLY for the server calls. NO NOT use this constructor from the app.
     */
    public Point() {
        geometry = new Geometry();
        properties = new HashMap<>();
        isVisited = false;
    }

    public Point(double latitude, double longitude, String description, String name) {
        geometry = new Geometry();
        geometry.coordinates = new double[]{longitude, latitude};
        properties = new HashMap<>();
        if (description != null) {
            properties.put("description", description);
        }else{
            properties.put("description", " ");
        }
        if (properties != null) {
            properties.put("name", name);
        }else{
            properties.put("name", " ");
        }
        isVisited = false;
    }

    public int getId() {
        return id;
    }

    public double getLatitude() {
        return geometry.coordinates[1];
    }

    public double getLongitude() {
        return geometry.coordinates[0];
    }

    /**
     * The description CAN be NULL, must be handled by the application if this is the case.
     *
     * @return Returns the String representing this point's description, if there is one. Null otherwise.
     */
    public String getDescription() {
        return properties.get("description");
    }

    public void setLatitude(double latitude) {
        geometry.coordinates[1] = latitude;
    }

    public void setLongitude(double longitude) {
        geometry.coordinates[0] = longitude;
    }

    public void setDescription(String description) {
        properties.put("description", description);
    }


    /**
     * Adds any property to this Point. DO NOT set ID, coordinates or description through this method. Will most likely NOT be saved on the server, though it will be sent.
     *
     * @param key   The property name, i.e. "point_title". Use lowercase letters and underscores.
     * @param value The value to save to your parameter. Must be a String, and can be retrieved through getProperty.
     */
    public void addProperty(String key, String value) {
        properties.put(key, value);
    }

    /**
     * Retrieve a property value saved with the key. DO NOT use this to get ID, coordinates or description.
     *
     * @param key A String key used to save a property.
     * @return The object saved as a property
     */
    public String getProperty(String key) {
        return properties.get(key);
    }

    /**
     * Determines the distance between this point and a given position in meters.
     *
     * @param position The position to find the distance to
     * @return The distance between point and position
     */
    public float getDistanceFromPoint(LatLng position) {
        float[] result = new float[1];
        Location.distanceBetween(getLatitude(), getLongitude(), position.latitude, position.longitude, result);
        return result[0];

    }

    public void _setId(int id) {
        this.id = id;
    }

    public Map<String, String> _getAllProperties() {
        return properties;
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(getLatitude(), getLongitude());
    }

    @Override
    public String getTitle() {

        if (properties.get("name") == null) {
            return " ";
        }

        return properties.get("name");
    }

    @Override
    public String getSnippet() {
        return properties.get("description");
    }

    private class Geometry implements Serializable {
        String type = "Point";
        double[] coordinates;
    }

    /**
     * Tags whether the point has been visited by the user
     */
    public void setVisited(boolean visited) {
        isVisited = visited;
    }

    /**
     * @return Whether the point has been visited
     */
    public boolean isVisited() {
        return isVisited;
    }
}