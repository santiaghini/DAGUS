package mx.dagus.dagus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Base64;
import android.provider.Settings.Secure;
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
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;


public class InicioActivity extends AppCompatActivity {
    private LoginButton loginButton;
    private CallbackManager callbackManager;

    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_inicio);

        Log.d("hola" , "hola");

        loginButton = (LoginButton) findViewById(R.id.login_button);
        callbackManager = CallbackManager.Factory.create();

        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024);
        Network network = new BasicNetwork(new HurlStack());

        requestQueue = new RequestQueue(cache, network);
        requestQueue.start();

        loginButton.registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo info = manager.getActiveNetworkInfo();
                        if (info != null && info.isConnected()) {
                            final JSONObject facebook = new JSONObject();
                            try {
                                facebook.put("expirationDate", loginResult.getAccessToken().getExpires().getTime());
                                facebook.put("token", loginResult.getAccessToken().getToken());
                                facebook.put("userId", loginResult.getAccessToken().getUserId());

                                final String password = "pass";

                                //final String password = bin2hex(getHash(loginResult.getAccessToken().getUserId()));

                                Log.d("mx.dagus.dagus", password);

                                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {
                                        final String email;
                                        final String name;
                                        String gender;
                                        try {
                                            email = object.getString("email");
                                            name = object.getString("name");
                                            gender = object.getString("gender");

                                            JSONObject user = new JSONObject();
                                            user.put("email", email);
                                            user.put("password", password);
                                            user.put("facebook", facebook);
                                            user.put("name", name);
                                            user.put("gender", gender);

                                            String url = "";
                                            final String finalEmail = email;

                                           /* final String token = bin2hex(getHash(finalEmail + password));

                                            final JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, user, new Response.Listener<JSONObject>() {
                                                @Override
                                                public void onResponse(JSONObject response) {
                                                    SharedPreferences preferences = getApplicationContext().getSharedPreferences("Dagus", Context.MODE_PRIVATE);
                                                    SharedPreferences.Editor editor = preferences.edit();
                                                    editor.putString("token", token);
                                                    editor.putString("email", email);
                                                    editor.putString("name", name);
                                                    editor.apply();

                                                }
                                            }, new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    Log.d("mx.dagus.dagus", error.toString());
                                                }
                                            }) {
                                                @Override
                                                public Map<String, String> getHeaders() throws AuthFailureError {
                                                    Map<String, String> params = new HashMap<>();
                                                    params.put("Authorization", token);
                                                    params.put("Content-Type", "application/json");
                                                    return params;
                                                }
                                            };
                                            requestQueue.add(jsonRequest); */
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
}

class bin2hex {
    private static int checksum = 0;

    private static byte[] readBinaryFile(String filename) throws Exception {
        File file = new File(filename);
        int size = (int) file.length();
        byte[] data = new byte[size];
        FileInputStream in = new FileInputStream(file);
        in.read(data);
        in.close();
        return data;
    }

    private static String hexByte(int b) {
        checksum += b;
        String result = Integer.toHexString(b & 0xff);
        if (result.length() < 2) {
            result = "0" + result;
        }
        return result;
    }

    private static void saveIntelHex(String filename, byte[] memory) throws Exception {
        int start = 0;
        int end = memory.length;
        PrintWriter out = new PrintWriter(new File(filename));

        // Intel Hex format:
        // : byte-count, address (16 bit), record type (00=data, 01=eof), data, checksum (2's complement of sum of data)

        // content
        int size = end - start;
        int blocks = (size + 31) / 32;
        for (int block = 0; block < blocks; block++) {
            // calculate current memory region, 32 bytes max. per line
            int currentStart = block * 32 + start;
            int currentEnd = currentStart + 32;
            if (currentEnd > end) {
                currentEnd = end;
            }
            int currentSize = currentEnd - currentStart;
            checksum = 0;

            // create one line of data
            String line = ":" + hexByte(currentSize) + hexByte((currentStart >> 8) & 0xff) + hexByte(currentStart & 0xff) + "00";
            for (int i = currentStart; i < currentEnd; i++) {
                line += hexByte(memory[i]);
            }

            // add checksum and write line
            line += hexByte((checksum ^ 0xff) + 1);
            out.println(line);
        }

        // eof
        out.println(":00000001FF");
        out.close();
    }

    public static void main(String args[]) throws Exception {
        if (args.length != 2) {
            System.out.println("usage: java bin2hex input.bin output.hex");
            return;
        }
        String inputFilename = args[0];
        String outputFilename = args[1];
        byte[] data = readBinaryFile(inputFilename);
        saveIntelHex(outputFilename, data);
    }
}