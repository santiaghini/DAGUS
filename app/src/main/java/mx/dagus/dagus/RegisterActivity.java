package mx.dagus.dagus;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Base64;
import android.provider.Settings.Secure;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidentityprovider.AmazonCognitoIdentityProvider;
import com.amazonaws.services.cognitoidentityprovider.AmazonCognitoIdentityProviderClient;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;


public class RegisterActivity extends AppCompatActivity {
    private LoginButton loginButton;
    private CallbackManager callbackManager;

    String email;

    RequestQueue requestQueue;
    Typeface gothambold;
    Typeface gothamlight;
    EditText editemail;
    EditText editpass;
    EditText editconfirm;
    EditText editname;
    String name;
    String password;

    private ProgressDialog waitDialog;
    private AlertDialog userDialog;

    /**
     * Add your pool id here
     */
    private static final String userPoolId = "us-west-2_9By8jo8pl";

    /**
     * Add you app id
     */
    private static final String clientId = "168nupr8vi67qcujrvm4qamba0";

    /**
     * App secret associated with your app id - if the App id does not have an associated App secret,
     * set the App secret to null.
     * e.g. clientSecret = null;
     */
    private static final String clientSecret = "vsnrvghep9qrucnfcg6truq51dlb1jvu3c49dchm0v2677ii0fo";

    /**
     * Set Your User Pools region.
     * e.g. if your user pools are in US East (N Virginia) then set cognitoRegion = Regions.US_EAST_1.
     */
    private static final Regions cognitoRegion = Regions.US_WEST_2;

    // User details from the service
    private static CognitoUserSession currSession;
    private static CognitoUserDetails userDetails;


    EditText emailText;
    EditText passwordText;

    EditText confirmcodeText;

    CognitoUser cognitoUser;
    GenericHandler confirmationCallback;



    static CognitoUserPool userPool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_register);

        LoginManager.getInstance().logOut();

        gothamlight = Typeface.createFromAsset(getAssets() , "fonts/gotham_light.ttf");
        gothambold = Typeface.createFromAsset(getAssets() , "fonts/gotham_bold.ttf");

        final Button loginbutton = (Button) findViewById(R.id.register_registrarse);

        loginbutton.setTypeface(gothambold);
        loginbutton.setTextColor(getResources().getColor(R.color.almostwhite));

        TextView titu = (TextView)  findViewById(R.id.register_titulo);
        titu.setTypeface(gothamlight);
        titu.setTextColor(getResources().getColor(R.color.white));

        editemail = (EditText) findViewById(R.id.register_email);
        editpass = (EditText) findViewById(R.id.register_contra);
        editname = (EditText) findViewById(R.id.register_nombre);
        editconfirm = (EditText) findViewById(R.id.register_confirma);


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

        editconfirm.setTypeface(gothamlight);
        editconfirm.setTextColor(getResources().getColor(R.color.white));
        editconfirm.setHintTextColor(getResources().getColor(R.color.fondogrisclaro));
        editconfirm.setBackground(background);

        editname.setTypeface(gothamlight);
        editname.setTextColor(getResources().getColor(R.color.white));
        editname.setHintTextColor(getResources().getColor(R.color.fondogrisclaro));
        editname.setBackground(background);

        Log.d("hola" , "hola");




        ClientConfiguration clientConfiguration = new ClientConfiguration();
        AmazonCognitoIdentityProvider cipClient = new AmazonCognitoIdentityProviderClient(new AnonymousAWSCredentials(), clientConfiguration);
        cipClient.setRegion(Region.getRegion(cognitoRegion));
        userPool = new CognitoUserPool(getApplicationContext(), userPoolId, clientId, clientSecret, cipClient);





        loginButton = (LoginButton) findViewById(R.id.register_login);
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
                                                    Intent intent = new Intent(RegisterActivity.this , LandingActivity.class);
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
                                                                editor.apply();

                                                                Intent intent = new Intent(RegisterActivity.this, LandingActivity.class);
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
                                            Toast.makeText(RegisterActivity.this, "Hubo un error", Toast.LENGTH_SHORT).show();
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

    /* public void registrar (View view) {
        final String name = editname.getText().toString();
        String email = editemail.getText().toString();
        String password = editpass.getText().toString();
        String confirm = editconfirm.getText().toString();

        if (!(password.equals(confirm))) {

            Toast notequals = Toast.makeText(getApplicationContext(), getString(R.string.contraNoIgual) , Toast.LENGTH_SHORT);
            notequals.show();

        } else {

            final JSONObject newuser = new JSONObject();
            try {
                newuser.put("email", email);
                newuser.put("password", password);
                newuser.put("name", name);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        requestQueue = Volley.newRequestQueue(this);

        final String url = "https://dagus.mx/api/users";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, newuser, new Response.Listener<JSONObject>() {
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
                editor.putString("name", name);
                editor.apply();

                Toast exito = Toast.makeText(getApplicationContext(), getString(R.string.exito) , Toast.LENGTH_SHORT);
                exito.show();
                Intent intent = new Intent(RegisterActivity.this, LandingActivity.class);
                startActivity(intent);

            }

        }
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error", error.toString());
                if (error.networkResponse.statusCode == 409) {

                    Toast yaexiste = Toast.makeText(getApplicationContext(), getString(R.string.yaexiste) , Toast.LENGTH_SHORT);
                    yaexiste.show();

                    final String url = "https://dagus.mx/api/login";
                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, newuser, new Response.Listener<JSONObject>() {
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

                            Intent intent = new Intent(RegisterActivity.this, LandingActivity.class);
                            startActivity(intent);

                        }

                    }
                            , new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("error", error.toString());
                            Toast incorrect = Toast.makeText(getApplicationContext(), getString(R.string.loginincorrecto) , Toast.LENGTH_SHORT);
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
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "c1b79fc950276536c1fdb0d3f2dc4d18a15872671143465a743398da1eb0fcd4");
                return headers;
            }
        };

        requestQueue.add(request);
    }

    }
    */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void registrar (View view) {
        Context context = getApplicationContext();

        email = editemail.getText().toString();
        password = editpass.getText().toString();
        String confirm = editconfirm.getText().toString();
        name = editname.getText().toString();

        // Create a CognitoUserAttributes object and add user attributes
        final CognitoUserAttributes userAttributes = new CognitoUserAttributes();


        ClientConfiguration clientConfiguration = new ClientConfiguration();
        AmazonCognitoIdentityProvider cipClient = new AmazonCognitoIdentityProviderClient(new AnonymousAWSCredentials(), clientConfiguration);
        cipClient.setRegion(Region.getRegion(cognitoRegion));
        userPool = new CognitoUserPool(context, userPoolId, clientId, clientSecret, cipClient);




        // Add the user attributes. Attributes are added as key-value pairs
        // Adding user's given name.
        // Note that the key is "given_name" which is the OIDC claim for given name
        //userAttributes.addAttribute("given_name", userGivenName);

        // Adding user's phone number
        //userAttributes.addAttribute("phone_number", phoneNumber);
        // Adding user's email address
        Log.d("email", email);

        if (!(password.equals(confirm))) {

            Toast notequals = Toast.makeText(getApplicationContext(), getString(R.string.contraNoIgual), Toast.LENGTH_SHORT);
            notequals.show();

        } else {
            if (password == null || password.matches("")) {
                TextView passview = (TextView) findViewById(R.id.textViewUserRegPasswordMessage);
                String passHint = editpass.getHint().toString();
                passview.setText(passHint + " cannot be empty");
                //password.setBackground(getDrawable(R.drawable.text_border_error));
                return;
            }

            if (email == null || email.matches("")) {
                TextView userview = (TextView) findViewById(R.id.textViewRegUserIdMessage);
                userview.setText(emailText.getHint() + " cannot be empty");
                //emailText.setBackground(getDrawable(R.drawable.text_border_error));
                return;
            } else {
                userAttributes.addAttribute("email", email);
            }


            if (name != null) {
                if (name.length() > 0) {
                    userAttributes.addAttribute("name", name);
                }
            }



            showWaitDialog("Signing up...");

            SignUpHandler signupCallback = new SignUpHandler() {

                @Override
                public void onSuccess(CognitoUser cognitoUser, boolean userConfirmed, CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
                    // Sign-up was successful
                    /* try {
                        Log.d("user", cognitoUser.getUserId());
                    } catch (Exception e) {
                        Log.d("Exx", e.toString());
                    } */

                    closeWaitDialog();

                    Log.d("user", cognitoUser.toString());
                    Log.d("userid", cognitoUser.getUserId());

                    if (userConfirmed) {
                        // User is already confirmed
                        showDialogMessage("Sign up successful!" , email + " has been Confirmed", true);
                    }
                    else {
                        // User is not confirmed


                        // Check if this user (cognitoUser) needs to be confirmed
                        // This user must be confirmed and a confirmation code was sent to the user
                        // cognitoUserCodeDeliveryDetails will indicate where the confirmation code was sent
                        // Get the confirmation code from user

                        String medium = cognitoUserCodeDeliveryDetails.getDeliveryMedium();
                        Log.d("medium", medium);
                        String attribute = cognitoUserCodeDeliveryDetails.getAttributeName();
                        Log.d("attributename", attribute);
                        String destination = cognitoUserCodeDeliveryDetails.getDestination();
                        Log.d("destination", destination);


                        if (attribute.equals("email")) {
                            Toast.makeText(getApplicationContext(), "Se ha enviado un correo de confirmación a " + email, Toast.LENGTH_LONG).show();
                        }

                        SharedPreferences preferences = getApplicationContext().getSharedPreferences("Dagus", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        //editor.putString("token", token);
                        editor.putString("name", name);
                        editor.apply();

                        Log.d("userPool", userPool.toString());

                        confirmSignUp(cognitoUserCodeDeliveryDetails);


                        /*Intent intent = new Intent(RegisterActivity.this, ConfirmCode.class);
                        intent.putExtra("email", email);
                        startActivity(intent); */
                    }


                }

                @Override
                public void onFailure(Exception exception) {
                    // Sign-up failed, check exception for the cause
                    Log.d("exception", exception.toString());
                    closeWaitDialog();
                    TextView label = (TextView) findViewById(R.id.textViewRegUserIdMessage);
                    label.setText("Sign up failed");
                    //emailText.setBackground(getDrawable(R.drawable.text_border_error));
                    showDialogMessage("Sign up failed", formatException(exception),false);
                }
            };


            userPool.signUpInBackground(email, password, userAttributes, null, signupCallback);
            //userPool.signUp(email, password, userAttributes, null, signupCallback);




            //Log.d("user" , cognitoUser.toString() + "papu pro");


            CognitoUser cognitoUser2 = userPool.getCurrentUser();
            //Log.d("userpro", cognitoUser2.getUserId().toString());



        }

    }

    public void confirm (View v) {

        //Log.d("user" , cognitoUser.getUserId() + " userpro");

        Intent intent = new Intent(RegisterActivity.this, ConfirmCode.class);
        intent.putExtra("email", email);
        startActivity(intent);
    }

    public static CognitoUserPool getPool() {
        return userPool;
    }


    private void confirmSignUp(CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
        Intent intent = new Intent(this, ConfirmCode.class);
        intent.putExtra("source", "signup");
        intent.putExtra("name", name);
        intent.putExtra("destination", cognitoUserCodeDeliveryDetails.getDestination());
        intent.putExtra("deliveryMed", cognitoUserCodeDeliveryDetails.getDeliveryMedium());
        intent.putExtra("attribute", cognitoUserCodeDeliveryDetails.getAttributeName());
        intent.putExtra("email", email);
        intent.putExtra("password" , password);
        startActivityForResult(intent, 10);

    }

    private void showDialogMessage(String title, String body, final boolean exit) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(body).setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    userDialog.dismiss();
                    if(exit) {
                        //exit(email);
                    }
                } catch (Exception e) {
                    if(exit) {
                        //exit(email);
                    }
                }
            }
        });
        userDialog = builder.create();
        userDialog.show();
    }

    private void showWaitDialog(String message) {
        closeWaitDialog();
        waitDialog = new ProgressDialog(this);
        waitDialog.setTitle(message);
        waitDialog.show();
    }

    private void closeWaitDialog() {
        try {
            waitDialog.dismiss();
        }
        catch (Exception e) {
            //
        }
    }

    public static String formatException(Exception exception) {
        String formattedString = "Internal Error";
        Log.e("App Error",exception.toString());
        Log.getStackTraceString(exception);

        String temp = exception.getMessage();

        if(temp != null && temp.length() > 0) {
            formattedString = temp.split("\\(")[0];
            if(temp != null && temp.length() > 0) {
                return formattedString;
            }
        }

        return  formattedString;
    }

}

