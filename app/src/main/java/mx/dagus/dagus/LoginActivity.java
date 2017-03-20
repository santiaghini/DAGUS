package mx.dagus.dagus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;


public class LoginActivity extends AppCompatActivity {
    private LoginButton loginButton;
    private CallbackManager callbackManager;

    RequestQueue requestQueue;
    Typeface gothamlight;
    Typeface gothambold;

    EditText editemail;
    EditText editpass;
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        LoginManager.getInstance().logOut();

        gothamlight = Typeface.createFromAsset(getAssets() , "fonts/gotham_light.ttf");
        gothambold = Typeface.createFromAsset(getAssets() , "fonts/gotham_bold.ttf");

        Button loginbutton = (Button) findViewById(R.id.login_botoninicio);
        Button olvidocontra = (Button) findViewById(R.id.login_olvidocontra);

        olvidocontra.setTypeface(gothamlight);
        olvidocontra.setTextColor(getResources().getColor(R.color.almostwhite));

        loginbutton.setTypeface(gothambold);
        loginbutton.setTextColor(getResources().getColor(R.color.almostwhite));

        TextView titu = (TextView)  findViewById(R.id.login_titulo);
        titu.setTypeface(gothamlight);
        titu.setTextColor(getResources().getColor(R.color.white));

        EditText editemail = (EditText) findViewById(R.id.login_email);
        EditText editpass = (EditText) findViewById(R.id.login_contra);

        editemail.setTypeface(gothamlight);
        editemail.setTextColor(getResources().getColor(R.color.white));
        editemail.setHintTextColor(getResources().getColor(R.color.fondogrisclaro));
        Drawable background = editpass.getBackground();
        background.mutate().setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.fondogrisclaro), PorterDuff.Mode.SRC_ATOP);
        editemail.setBackground(background);

        editpass.setTypeface(gothamlight);
        editpass.setTextColor(getResources().getColor(R.color.white));
        editpass.setHintTextColor(getResources().getColor(R.color.fondogrisclaro));
        editpass.setBackground(background);

        Log.d("hola" , "hola");

        loginButton = (LoginButton) findViewById(R.id.login_login);
        loginButton.setReadPermissions(Arrays.asList(
                "public_profile", "email", "user_birthday", "user_friends"));

        callbackManager = CallbackManager.Factory.create();

        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024);
        Network network = new BasicNetwork(new HurlStack());

        requestQueue = new RequestQueue(cache, network);
        requestQueue.start();

        loginButton.registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        Log.d("success" , loginResult.getAccessToken().getUserId());

                        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo info = manager.getActiveNetworkInfo();
                        if (info != null && info.isConnected()) {
                            final JSONObject facebook = new JSONObject();
                            try {
                                facebook.put("expirationDate", loginResult.getAccessToken().getExpires().getTime());
                                facebook.put("token", loginResult.getAccessToken().getToken());
                                facebook.put("userId", loginResult.getAccessToken().getUserId());

                                //final String password = bin2hex(getHash(loginResult.getAccessToken().getUserId()));
                                final String password = loginResult.getAccessToken().getUserId();

                                Log.d("password", password);

                                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {

                                        Log.d("objectface" , object.toString());

                                        final String email;
                                        final String name;
                                        String gender;
                                        try {
                                            email = object.getString("email");
                                            name = object.getString("name");
                                            gender = object.getString("gender");

                                            final JSONObject user = new JSONObject();
                                            user.put("email", email);
                                            user.put("password", password);
                                            user.put("facebook", facebook);
                                            user.put("name", name);
                                            user.put("gender", gender);

                                            String url = "https://dagus.mx/api/users";
                                            final String finalEmail = email;


                                            final JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, user, new Response.Listener<JSONObject>() {
                                                @Override
                                                public void onResponse(JSONObject response) {

                                                    String token = null;
                                                    try {
                                                        token = response.getString("token");
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }

                                                    SharedPreferences preferences = getApplicationContext().getSharedPreferences("Dagus", Context.MODE_PRIVATE);
                                                    SharedPreferences.Editor editor = preferences.edit();
                                                    editor.putString("token", token);
                                                    editor.putString("email", email);
                                                    editor.putString("name", name);
                                                    editor.apply();
                                                    Intent intent = new Intent(LoginActivity.this , LandingActivity.class);
                                                    startActivity(intent);

                                                }
                                            }, new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    Log.d("error", error.toString());
                                                    if (error.networkResponse.statusCode == 409) {

                                                        final String url = "https://dagus.mx/api/login";
                                                        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, user, new Response.Listener<JSONObject>() {
                                                            @Override
                                                            public void onResponse(JSONObject response) {
                                                                Log.d("response", response.toString());
                                                                String token = null;
                                                                try {
                                                                    token = response.getString("token");
                                                                } catch (JSONException e) {
                                                                    e.printStackTrace();
                                                                }
                                                                SharedPreferences preferences = getApplicationContext().getSharedPreferences("Dagus", Context.MODE_PRIVATE);
                                                                SharedPreferences.Editor editor = preferences.edit();
                                                                editor.putString("token", token);
                                                                editor.putString("name" , name);
                                                                editor.apply();

                                                                Intent intent = new Intent(LoginActivity.this, LandingActivity.class);
                                                                startActivity(intent);

                                                            }

                                                        }
                                                                , new Response.ErrorListener() {
                                                            @Override
                                                            public void onErrorResponse(VolleyError error) {
                                                                Log.d("error", error.toString());
                                                                Toast incorrect = Toast.makeText(getApplicationContext(), "Error" , Toast.LENGTH_SHORT);
                                                                incorrect.show();
                                                            }
                                                        }) {

                                                            @Override
                                                            public Map<String, String> getHeaders() throws AuthFailureError {
                                                                Map<String, String> headers = new HashMap<>();
                                                                headers.put("Content-Type", "application/json");
                                                                headers.put("Authorization", "c1b79fc950276536c1fdb0d3f2dc4d18a15872671143465a743398da1eb0fcd4");
                                                                return headers;
                                                            }
                                                        };

                                                        requestQueue.add(request);
                                                    }
                                                }
                                            }) {
                                                @Override
                                                public Map<String, String> getHeaders() throws AuthFailureError {
                                                    Map<String, String> params = new HashMap<>();
                                                    params.put("Authorization", "c1b79fc950276536c1fdb0d3f2dc4d18a15872671143465a743398da1eb0fcd4");
                                                    params.put("Content-Type", "application/json");
                                                    return params;
                                                }
                                            };
                                            requestQueue.add(jsonRequest);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                                Bundle parameters = new Bundle();
                                parameters.putString("fields", "name,email,gender");
                                request.setParameters(parameters);
                                request.executeAsync();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.d("mx.dagus.dagus", "App is not connected to the network.");
                        }
                    }

                    @Override
                    public void onCancel() {
                        Log.d("cancel" , "cancel");
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d("error" , error.getLocalizedMessage());
                    }
                });

    }

    public byte[] getHash(String password) {
        MessageDigest digest = null;

        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }

        digest.reset();

        return digest.digest(password.getBytes());
    }

    static String bin2hex(byte[] data) {
        return String.format("%0" + (data.length*2) + "x", new BigInteger(1, data));
    }


    public void login(View view) {
        EditText editemail = (EditText) findViewById(R.id.login_email);
        EditText editpass = (EditText) findViewById(R.id.login_contra);
            String email = editemail.getText().toString();
            String password = editpass.getText().toString();

            JSONObject user = new JSONObject();
            try {
                user.put("email", email);
                user.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            requestQueue = Volley.newRequestQueue(this);

            final String url = "https://dagus.mx/api/login";
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, user, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("response", response.toString());
                    //_id, name, password, token, type (android), email
                    Log.d("response", response.toString());
                    String token = null;
                    try {
                        token = response.getString("token");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        name = response.getString("name");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    SharedPreferences preferences = getApplicationContext().getSharedPreferences("Dagus", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("token", token);
                    editor.putString("name", name);
                    editor.apply();

                    try {
                        name = response.getString("name");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Toast exito = Toast.makeText(getApplicationContext(), getString(R.string.exito) , Toast.LENGTH_SHORT);
                    exito.show();

                    Intent intent = new Intent(LoginActivity.this, LandingActivity.class);
                    startActivity(intent);

                }

            }
                    , new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("error", error.toString());
                    Toast incorrect = Toast.makeText(getApplicationContext(), getString(R.string.loginincorrecto) , Toast.LENGTH_LONG);
                    incorrect.show();
                }
            }) {

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "c1b79fc950276536c1fdb0d3f2dc4d18a15872671143465a743398da1eb0fcd4");
                    return headers;
                }
            };

            requestQueue.add(request);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}