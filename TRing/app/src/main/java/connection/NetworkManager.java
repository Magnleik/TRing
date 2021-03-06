
package connection;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;


import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Eirik on 15-Feb-18.
 */

public class NetworkManager {


    static private NetworkManager nm;
    private Client client;
    private OkHttpClient.Builder httpClient;
    private Retrofit.Builder builder;
    private Retrofit retrofit;


    private NetworkManager() {
        init();
    }

    public static NetworkManager getInstance() {
        if (nm == null) {
            nm = new NetworkManager();
        }

        return nm;
    }

    private void init() {

        httpClient = new OkHttpClient.Builder();
        String URL = "https://tring-tba4250.herokuapp.com";
        builder = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(
                        GsonConverterFactory.create()
                );

        retrofit = builder
                .client(
                        httpClient.build()
                )
                .build();

        client = retrofit.create(Client.class);

        //Just here for testing:

        //updatePointTest();
        //addPointsTest();
        //addEventTest();
        //eventsNearbyTest();

    }

    //region Testing methods

    private void updatePointTest() {
        Point testPoint1 = new Point(10.324, 50, "This is an updated test point");
        testPoint1._setId(80);

        updatePoint(testPoint1, new ICallbackAdapter<Point>() {
            @Override
            public void onResponse(Point object) {
                System.out.println(" ");
            }

            @Override
            public void onFailure(Throwable t) {
                System.out.println(" ");
            }
        });
    }

    private void addPointsTest() {

        Point testPoint1 = new Point(10.324, 20.420, "This is a test point");
        Point testPoint2 = new Point(123.321, 12.123, "Test point #2");
        Point testPoint3 = new Point(0.0, 0.0, "This is a starting point");

        addPoints(new ICallbackAdapter<List<Point>>() {
            @Override
            public void onResponse(List<Point> object) {
                System.out.println("Recieved points with the first being " + object.get(0).getId());
            }

            @Override
            public void onFailure(Throwable t) {
                System.out.println(t.getMessage());
            }
        }, testPoint1, testPoint2, testPoint3);

    }

    private void eventsNearbyTest() {

        getNearbyEvents(63.43, 10.50, 20000, new ICallbackAdapter<ArrayList<Event>>() {
            @Override
            public void onResponse(ArrayList<Event> object) {
                if (object != null) {
                    System.out.println("Got a list of " + object.size() + " events");
                } else {
                    System.out.println("Received NULL from eventsNearbyTest");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                System.out.println("EventsNearbyTest failed");
            }
        });

    }

    private void addEventTest() {

        Point testPoint1 = new Point(10.324, 20.420, "This is a test point");
        Point testPoint2 = new Point(123.321, 12.123, "Test point #2");
        Point testPoint3 = new Point(0.0, 0.0, "This is a starting point");
        ArrayList<Point> points = new ArrayList<>();
        points.add(testPoint1);
        points.add(testPoint2);
        Event testEvent = new Event();
        testEvent.addPosts(points);
        testEvent.setStartPoint(testPoint3);
        testEvent.addProperty("name", "test_property");
        testEvent.addProperty("name", "test_property2");
        testEvent.addProperty("avg_time", "00:00:00");

        addEvent(testEvent, new ICallbackAdapter<Event>() {
            @Override
            public void onResponse(Event object) {
                if (object != null) {
                    System.out.println("Recieved event with ID " + object.getId());
                } else {
                    System.out.println("Received response on addEventTest with NULL as response body");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                System.out.println("FAILED! : " + t.getMessage());
            }
        });

    }

    //endregion

    //region POST-methods

    /**
     * Adds points to the database, not affiliated with an Event
     *
     * @param points   The Points to be added, can be any number
     * @param callback The callback to handle results. Override its methods to get what you need. onResponse gets a Point
     */
    public void addPoints(final ICallbackAdapter<List<Point>> callback, Point... points) {
        Call<List<Point>> call = client.addPoints(points);

        call.enqueue(new Callback<List<Point>>() {
            @Override
            public void onResponse(@NonNull Call<List<Point>> call, @NonNull Response<List<Point>> response) {

                if (!response.isSuccessful()) {
                    Log.i("NETWORK", "addPoints got onResponse, without success. RESPONSE: " + response.toString());
                } else {
                    Log.i("NETWORK", "addPoints successful with response: " + response.toString());
                }

                callback.onResponse(response.body());

            }

            @Override
            public void onFailure(@NonNull Call<List<Point>> call, @NonNull Throwable t) {
                Log.e("NETWORK", t.getMessage(), t);
                callback.onFailure(t);
            }
        });
    }

    /**
     * ADDS an Event to the database, will not update existing events. Ignores set ID.
     *
     * @param event    The Event to add to the database
     * @param callback The callback to handle results. Override its methods to get what you need. onResponse gets the same Event, with updated information from the database - including its generated ID.
     */
    public void addEvent(Event event, final ICallbackAdapter<Event> callback) {
        Call<Event> call = client.addEvent(event);

        call.enqueue(new Callback<Event>() {
            @Override
            public void onResponse(@NonNull Call<Event> call, @NonNull Response<Event> response) {
                if (!response.isSuccessful()) {
                    Log.i("NETWORK", "addEvent got onResponse, without success. RESPONSE: " + response.toString());
                } else {
                    Log.i("NETWORK", "addEvent successful with response: " + response.toString());
                }

                callback.onResponse(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<Event> call, @NonNull Throwable t) {
                Log.e("NETWORK", t.getMessage(), t);
                callback.onFailure(t);
            }
        });

    }

    /**
     * Post the time taken to complete the event with the given ID. Used to update the avgTime.
     *
     * @param eventID  The int ID of the event you wish to post a score for.
     * @param time     The time used for the event, on the form "hh:mm:ss"
     * @param score    The score
     * @param callback The callback to handle results. onResponse gets passed the updated event - so time can be compared to the updated avgTime.
     */
    public void postResults(int eventID, String time, int score, ICallbackAdapter<Event> callback) {
        Call<Event> call = client.postResults(eventID, time, score);

        call.enqueue(new Callback<Event>() {
            @Override
            public void onResponse(@NonNull Call<Event> call, @NonNull Response<Event> response) {
                if (!response.isSuccessful()) {
                    Log.i("NETWORK", "postResults got onResponse, without success. RESPONSE: " + response.toString());
                } else {
                    Log.i("NETWORK", "postResults successful with response: " + response.toString());
                }

                callback.onResponse(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<Event> call, @NonNull Throwable t) {
                Log.e("NETWORK", t.getMessage(), t);
                callback.onFailure(t);
            }
        });
    }

    /**
     * subscribe to event
     *
     * @param eventID  The id of the event you wish to subscribe to
     * @param callback onResponse gets passed the full subscription list.
     */
    public void subscribeToEvent(int eventID, final ICallbackAdapter<List<Event>> callback) {
        Call<List<Event>> call = client.subscribeToEvent(eventID);

        call.enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(@NonNull Call<List<Event>> call, @NonNull Response<List<Event>> response) {
                if (!response.isSuccessful()) {
                    Log.i("NETWORK", "subscribeToEvent got onResponse, without success. RESPONSE: " + response.toString());
                } else {
                    Log.i("NETWORK", "subscribeToEvent successful with response: " + response.toString());
                }

                callback.onResponse(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<List<Event>> call, @NonNull Throwable t) {
                Log.e("NETWORK", t.getMessage(), t);
                callback.onFailure(t);
            }
        });
    }

    //endregion

    //region GET-methods

    /**
     * Finds all points within a circle with radius maxDist from the given location
     *
     * @param latitude  The latitude of given position
     * @param longitude The longitude of given position
     * @param maxDist   The radius of search in metres
     * @param callback  The callback to handle results. Override its methods to get what you need. onResponse gets an ArrayList of Points within the given circle
     */
    public void getNearbyPoints(double latitude, double longitude, double maxDist, final ICallbackAdapter<ArrayList<Point>> callback) {

        Point sendPoint = new Point(latitude, longitude, " ");
        sendPoint.addProperty("max_dist", String.valueOf(maxDist));
        Call<List<Point>> call = client.getNearbyPoints(latitude, longitude, maxDist);

        call.enqueue(new Callback<List<Point>>() {
            @Override
            public void onResponse(@NonNull Call<List<Point>> call, @NonNull Response<List<Point>> response) {
                if (!response.isSuccessful()) {
                    Log.i("NETWORK", "getNearbyPoints got onResponse, without success. RESPONSE: " + response.toString());
                } else {
                    Log.i("NETWORK", "getNearbyPoints successful with response: " + response.toString());
                }

                callback.onResponse((ArrayList<Point>) response.body());
            }

            @Override
            public void onFailure(@NonNull Call<List<Point>> call, @NonNull Throwable t) {
                Log.e("NETWORK", t.getMessage(), t);
                callback.onFailure(t);
            }
        });

    }


    /**
     * Finds and returns all events with starting location within a radius maxDist from the given location
     *
     * @param latitude  The latitude of given position
     * @param longitude The longitude of given position
     * @param maxDist   The radius of search in meters
     * @param callback  The callback to handle results. Override its methods to get what you need. onResponse gets all events starting within the given circle
     */
    public void getNearbyEvents(double latitude, double longitude, double maxDist, final ICallbackAdapter<ArrayList<Event>> callback) {
        Call<List<Event>> call = client.getNearbyEvents(latitude, longitude, maxDist);

        call.enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(@NonNull Call<List<Event>> call, @NonNull Response<List<Event>> response) {
                if (!response.isSuccessful()) {
                    Log.i("NETWORK", "getNearbyEvents got onResponse, without success. RESPONSE: " + response.toString());
                } else {
                    Log.i("NETWORK", "getNearbyEvents successful with response: " + response.toString());
                }

                callback.onResponse((ArrayList<Event>) response.body());
            }

            @Override
            public void onFailure(@NonNull Call<List<Event>> call, @NonNull Throwable t) {
                Log.e("NETWORK", t.getMessage(), t);
                callback.onFailure(t);
            }
        });

    }

    /**
     * Returns the event with the given ID if it exists, null otherwise.
     *
     * @param id       The ID of the Event you wish to have returned.
     * @param callback The callback to handle results. Override its methods to get what you need. onResponse gets the Event with the given ID.
     */
    public void getEventById(int id, final ICallbackAdapter<Event> callback) {
        Call<Event> call = client.getEventById(id);

        call.enqueue(new Callback<Event>() {
            @Override
            public void onResponse(@NonNull Call<Event> call, @NonNull Response<Event> response) {
                if (!response.isSuccessful()) {
                    Log.i("NETWORK", "getEventById got onResponse, without success. RESPONSE: " + response.toString());
                } else {
                    Log.i("NETWORK", "getEventById successful with response: " + response.toString());
                }

                callback.onResponse(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<Event> call, @NonNull Throwable t) {
                Log.e("NETWORK", t.getMessage(), t);
                callback.onFailure(t);
            }
        });

    }


    /**
     * Get all your subscribed events
     *
     * @param callback onResponse gets passed the full subscription list.
     */
    public void getSubscribedEvents(final ICallbackAdapter<List<Event>> callback) {
        Call<List<Event>> call = client.getSubscribedEvents();

        call.enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(@NonNull Call<List<Event>> call, @NonNull Response<List<Event>> response) {
                if (!response.isSuccessful()) {
                    Log.i("NETWORK", "getSubscribedEvents got onResponse, without success. RESPONSE: " + response.toString());
                } else {
                    Log.i("NETWORK", "getSubscribedEvents successful with response: " + response.toString());
                }

                callback.onResponse(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<List<Event>> call, @NonNull Throwable t) {
                Log.e("NETWORK", t.getMessage(), t);
                callback.onFailure(t);
            }
        });

    }

    //endregion

    //region PUT-methods

    /**
     * Updates the existing Event if it exists in the database, and returns the updated Event.
     *
     * @param event    The state the Event in the database should be updated to.
     * @param callback The callback to handle results. Override its methods to get what you need. onResponse gets the updated Event
     */
    public void updateEvent(Event event, final ICallbackAdapter<Event> callback) {

        Call<Event> call = client.updateEvent(event);

        call.enqueue(new Callback<Event>() {
            @Override
            public void onResponse(@NonNull Call<Event> call, @NonNull Response<Event> response) {
                if (!response.isSuccessful()) {
                    Log.i("NETWORK", "updateEvent got onResponse, without success. RESPONSE: " + response.toString());
                } else {
                    Log.i("NETWORK", "updateEvent successful with response: " + response.toString());
                }

                callback.onResponse(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<Event> call, @NonNull Throwable t) {
                Log.e("NETWORK", t.getMessage(), t);
                callback.onFailure(t);
            }
        });

    }

    /**
     * Updates the existing Point if it exists in the database, and returns the updated Point.
     *
     * @param point    The state the Point in the database should be updated to.
     * @param callback The callback to handle results. Override its methods to get what you need. onResponse gets the updated Point
     */
    public void updatePoint(Point point, final ICallbackAdapter<Point> callback) {

        Call<Point> call = client.updatePoint(point);

        call.enqueue(new Callback<Point>() {
            @Override
            public void onResponse(@NonNull Call<Point> call, @NonNull Response<Point> response) {
                if (!response.isSuccessful()) {
                    Log.i("NETWORK", "updatePoint got onResponse, without success. RESPONSE: " + response.toString());
                } else {
                    Log.i("NETWORK", "updatePoint successful with response: " + response.toString());
                }

                callback.onResponse(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<Point> call, @NonNull Throwable t) {
                Log.e("NETWORK", t.getMessage(), t);
                callback.onFailure(t);
            }
        });

    }

    //endregion

    //region DELETE-methods

    /**
     * Delete an Event from your subscriptions.
     *
     * @param eventID  The id of the event you want removed
     * @param callback onResponse gets passed the full subscription list.
     */
    public void unsubscribeFromEvent(int eventID, ICallbackAdapter<List<Event>> callback) {
        Call<List<Event>> call = client.unsubscribeFromEvent(eventID);

        call.enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(@NonNull Call<List<Event>> call, @NonNull Response<List<Event>> response) {
                if (!response.isSuccessful()) {
                    Log.i("NETWORK", "unsubscribeToEvent got onResponse, without success. RESPONSE: " + response.toString());
                } else {
                    Log.i("NETWORK", "unsubscribeToEvent successful with response: " + response.toString());
                }

                callback.onResponse(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<List<Event>> call, @NonNull Throwable t) {
                Log.e("NETWORK", t.getMessage(), t);
                callback.onFailure(t);
            }
        });
    }

    //endregion


    //region login-logic

    /**
     * Create a new user in the database, and authenticates this instance of the application (from the server perspective, logs you in).
     *
     * @param username The username chosen
     * @param password The password chosen
     * @param callback The callback to handle results. onResponse gets a Boolean - true if the registration was successful, false otherwise.
     */
    public void createUser(String username, String password, final ICallbackAdapter<Boolean> callback) {

        User myUser = new User(username, password);

        Call<Boolean> call = client.createNewUser(myUser);

        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                if (!response.isSuccessful()) {
                    Log.i("NETWORK", "createUser got onResponse, without success. RESPONSE: " + response.toString());
                } else {
                    Log.i("NETWORK", "createUser successful with response: " + response.toString());
                    if (response.body() != null && response.body()) {
                        addCredentials(username, password);
                    }
                }

                callback.onResponse(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                Log.e("NETWORK", t.getMessage(), t);
                callback.onFailure(t);
            }
        });
    }

    /**
     * Check log in credentials with the database, and authenticates this instance of the application (from the server perspective, logs you in).
     *
     * @param username The username chosen
     * @param password The password chosen
     * @param callback The callback to handle results. onResponse gets a Boolean - true if the registration was successful, false otherwise.
     */
    public void logIn(String username, String password, final ICallbackAdapter<Boolean> callback) {

        logOut();

        addCredentials(username, password);

        Call<Boolean> call = client.logIn();

        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {

                if (response.isSuccessful()) {
                    Log.i("NETWORK", "logIn successful with response: " + response.toString());
                    callback.onResponse(true);
                } else if (response.code() == 401) {
                    Log.i("NETWORK", "logIn got 401 error");
                    callback.onResponse(false);
                    logOut();
                } else {
                    Log.i("NETWORK", "logIn got onResponse, without success. RESPONSE: " + response.toString());
                    callback.onResponse(null);
                    logOut();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                Log.e("NETWORK", t.getMessage(), t);
                callback.onFailure(t);
            }
        });
    }

    /**
     * Check log in credentials with the database, and authenticates this instance of the application (from the server perspective, logs you in).
     *
     * @param token    The authorization token
     * @param callback The callback to handle results. onResponse gets a Boolean - true if the registration was successful, false otherwise.
     */
    public void logInWithToken(String token, final ICallbackAdapter<Boolean> callback) {

        logOut();

        addCredentialsWithToken(token);

        Call<Boolean> call = client.logIn();

        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {

                if (response.isSuccessful()) {
                    Log.i("NETWORK", "logIn successful with response: " + response.toString());
                    callback.onResponse(true);
                } else if (response.code() == 401) {
                    Log.i("NETWORK", "logIn got 401 error");
                    callback.onResponse(false);
                    logOut();
                } else {
                    Log.i("NETWORK", "logIn got onResponse, without success. RESPONSE: " + response.toString());
                    callback.onResponse(null);
                    logOut();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                Log.e("NETWORK", t.getMessage(), t);
                callback.onFailure(t);
            }
        });
    }


    /**
     * When logging on, call this method with the username and password. Will add authentification to the HTTP calls.
     *
     * @param username The username
     * @param password The password
     */
    private void addCredentials(String username, String password) {

        String authToken = null;

        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
            authToken = Credentials.basic(username, password);
        }

        addCredentialsWithToken(authToken);
    }

    /**
     * When logging on, call this method with the authentication token. Will add authentication to the HTTP calls.
     *
     * @param authToken The username
     */
    private void addCredentialsWithToken(String authToken) {

        if (!TextUtils.isEmpty(authToken)) {
            AuthenticationInterceptor interceptor =
                    new AuthenticationInterceptor(authToken);

            if (!httpClient.interceptors().contains(interceptor)) {
                httpClient.addInterceptor(interceptor);

                builder.client(httpClient.build());
                retrofit = builder.build();
            }
        }

        client = retrofit.create(Client.class);

    }


    /**
     * Log out. Removes the authentication.
     */
    public void logOut() {

        if (isAuthenticated()) {
            for (int i = httpClient.interceptors().size() - 1; i >= 0; i--) {
                if (httpClient.interceptors().get(i) instanceof AuthenticationInterceptor) {
                    httpClient.interceptors().remove(i);
                    builder.client(httpClient.build());
                    retrofit = builder.build();
                }
            }
        }

        client = retrofit.create(Client.class);

    }

    /**
     * Get the authentication token of of the current login.
     *
     * @return Returns a string representing the current token. Returns null if no such token exists.
     */
    public String getToken() {
        if (!isAuthenticated()) {
            return null;
        }

        for (Interceptor i : httpClient.interceptors()) {
            if (i instanceof AuthenticationInterceptor) {
                return ((AuthenticationInterceptor) i).authToken;
            }
        }

        return null;
    }

    /**
     * Checks to see if the client is already authenticated. And thus does not need to log in.
     *
     * @return Returns true if authenticated.
     */
    public boolean isAuthenticated() {
        return httpClient.interceptors().size() > 0;
    }

    public class AuthenticationInterceptor implements Interceptor {

        private String authToken;

        AuthenticationInterceptor(String token) {
            this.authToken = token;
        }

        @Override
        public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {
            Request original = chain.request();

            Request.Builder builder = original.newBuilder()
                    .header("Authorization", authToken);

            Request request = builder.build();
            return chain.proceed(request);
        }
    }

    //endregion

}
