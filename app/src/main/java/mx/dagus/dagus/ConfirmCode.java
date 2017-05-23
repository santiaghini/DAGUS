package mx.dagus.dagus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.VerificationHandler;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidentityprovider.AmazonCognitoIdentityProvider;
import com.amazonaws.services.cognitoidentityprovider.AmazonCognitoIdentityProviderClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.util.HashMap;
import java.util.Map;

public class ConfirmCode extends AppCompatActivity {

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

    private static final String identityPoolID = "us-west-2:2c2c8ee7-413e-468a-8844-ed24aac783ee";

    String email;

    CognitoUser user;
    GenericHandler confirmationCallback;

    EditText confCode;
    EditText username;
    private Button confirm;
    private TextView reqCode;
    private String name;
    private AlertDialog userDialog;
    CognitoUserPool userPool;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_code);

        confCode = (EditText) findViewById(R.id.code_code);

        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");

        Bundle extras = getIntent().getExtras();
        if(extras.containsKey("name")) {
            name = extras.getString("name");
            username = (EditText) findViewById(R.id.code_user);
            username.setText(email);

            confCode = (EditText) findViewById(R.id.code_code);
            confCode.requestFocus();

            if(extras.containsKey("destination")) {
                String dest = extras.getString("destination");
                String delMed = extras.getString("deliveryMed");

                TextView screenSubtext = (TextView) findViewById(R.id.code_text);
                if(dest != null && delMed != null && dest.length() > 0 && delMed.length() > 0) {
                    screenSubtext.setText("A confirmation code was sent to "+dest+" via "+delMed);
                }
                else {
                    screenSubtext.setText("A confirmation code was sent");
                }
            }
        }
        else {
            TextView screenSubtext = (TextView) findViewById(R.id.code_text);
            screenSubtext.setText("Request for a confirmation code or confirm with the code you already have.");
        }

        userPool = RegisterActivity.getPool();
        if (userPool!=null) {
            Log.d("UserPool" , userPool.toString());
        } else {
            Log.d("NoPool" , "No hay pool");
    }

        username = (EditText) findViewById(R.id.code_user);
        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(s.length() == 0) {
                    TextView label = (TextView) findViewById(R.id.code_idlabel);
                    label.setText(username.getHint());
                    //username.setBackground(getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = (TextView) findViewById(R.id.code_idmessage);
                label.setText(" ");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 0) {
                    TextView label = (TextView) findViewById(R.id.code_idlabel);
                    label.setText("");
                }
            }
        });

        confCode = (EditText) findViewById(R.id.code_code);
        confCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(s.length() == 0) {
                    TextView label = (TextView) findViewById(R.id.code_codelabel);
                    label.setText(confCode.getHint());
                    //confCode.setBackground(getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = (TextView) findViewById(R.id.code_confirmcodemessage);
                label.setText(" ");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 0) {
                    TextView label = (TextView) findViewById(R.id.code_codelabel);
                    label.setText("");
                }
            }
        });

        confirm = (Button) findViewById(R.id.code_button);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCode();
            }
        });

        reqCode = (TextView) findViewById(R.id.code_resend);
        reqCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reqConfCode();
            }
        });

    }

    public void getCode () {

        email = username.getText().toString();
        String confirmCode = confCode.getText().toString();

        if(email == null || email.length() < 1) {
            TextView label = (TextView) findViewById(R.id.code_idmessage);
            label.setText(username.getHint()+" cannot be empty");
            //username.setBackground(getDrawable(R.drawable.text_border_error));
            return;
        }

        if(confirmCode == null || confirmCode.length() < 1) {
            TextView label = (TextView) findViewById(R.id.code_confirmcodemessage);
            label.setText(confCode.getHint()+" cannot be empty");
            //confCode.setBackground(getDrawable(R.drawable.text_border_error));
            return;
        }

       userPool.getUser(email).confirmSignUpInBackground(confirmCode, true, confHandler);


    }

    private void reqConfCode() {
        email = username.getText().toString();
        if(email == null || email.length() < 1) {
            TextView label = (TextView) findViewById(R.id.code_idmessage);
            label.setText(username.getHint()+" cannot be empty");
            //username.setBackground(getDrawable(R.drawable.text_border_error));
            return;
        }
        userPool.getUser(email).resendConfirmationCodeInBackground(resendConfCodeHandler);

    }




    GenericHandler confHandler = new GenericHandler() {
        @Override
        public void onSuccess() {
            showDialogMessage("Success!",email+" has been confirmed!", true);
            //auth();
        }

        @Override
        public void onFailure(Exception exception) {
            TextView label = (TextView) findViewById(R.id.code_idmessage);
            label.setText("Confirmation failed!");
            //username.setBackground(getDrawable(R.drawable.text_border_error));

            label = (TextView) findViewById(R.id.code_confirmcodemessage);
            label.setText("Confirmation failed!");
            //confCode.setBackground(getDrawable(R.drawable.text_border_error));

            showDialogMessage("Confirmation failed", exception.toString() , false);
        }
    };




    VerificationHandler resendConfCodeHandler = new VerificationHandler() {
        @Override
        public void onSuccess(CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
            TextView mainTitle = (TextView) findViewById(R.id.code_idmessage);
            mainTitle.setText("Confirm your account");
            confCode = (EditText) findViewById(R.id.code_code);
            confCode.requestFocus();
            showDialogMessage("Confirmation code sent.","Code sent to "+cognitoUserCodeDeliveryDetails.getDestination()+" via "+cognitoUserCodeDeliveryDetails.getDeliveryMedium()+".", false);
        }

        @Override
        public void onFailure(Exception exception) {
            TextView label = (TextView) findViewById(R.id.code_idmessage);
            label.setText("Confirmation code resend failed");
            //username.setBackground(getDrawable(R.drawable.text_border_error));
            showDialogMessage("Confirmation code request has failed", exception.toString() , false);
        }
    };



    private void showDialogMessage(String title, String body, final boolean exitActivity) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(body).setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    userDialog.dismiss();
                    if(exitActivity) {
                        exit();
                    }
                } catch (Exception e) {
                    exit();
                }
            }
        });
        userDialog = builder.create();
        userDialog.show();
    }

    private void exit () {
        Intent intent = new Intent(ConfirmCode.this , InicioActivity.class);
        startActivity(intent);
    }

    public void auth (View v) {
        EditText pass = (EditText) findViewById(R.id.code_pass);
        EditText user2 = (EditText) findViewById(R.id.code_user);
        final String password1 = pass.getText().toString();
        final String email1 = user2.getText().toString();
        final String email2 = "santi.hergut@gmail.com";
        final String pass1 = "CoDmw3bo2.";

        AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
            @Override
            public void onSuccess(CognitoUserSession userSession, CognitoDevice device) {
                //Sync User pool login in Cognito
                Log.d("UserSession" , userSession.toString());
                syncCognitoIdentityLogin(userSession);
            }
            @Override
            public void onFailure(Exception e) {
                // handle error
                showDialogMessage("Authentication failed", e.toString() , false);
            }

            @Override
            public void getMFACode(MultiFactorAuthenticationContinuation arg0) {
            }

            @Override
            public void getAuthenticationDetails(AuthenticationContinuation continuation,
                                                 String userName) {
                //set authentication details

                AuthenticationDetails authenticationDetails = new AuthenticationDetails(email2, pass1 , null);
                continuation.setAuthenticationDetails(authenticationDetails);
                continuation.continueTask();
                Log.d("Got Auth" , "Got auth details");
            }
            @Override
            public void authenticationChallenge(ChallengeContinuation arg0) {
            }
        };

        // Sign-in the user
        userPool.getUser(email2).getSessionInBackground(authenticationHandler);
    }

    private void syncCognitoIdentityLogin (final CognitoUserSession userSession) {
        // Get id token from CognitoUserSession.
        String idToken = userSession.getIdToken().getJWTToken();
        Log.d("idToken" , idToken);

        // Create a credentials provider, or use the existing provider.
        final CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(getApplicationContext() , identityPoolID , cognitoRegion);

        // Set up as a credentials provider.
        Map<String, String> logins = new HashMap<String, String>();
        logins.put("cognito-idp." + Regions.US_WEST_2 + ".amazonaws.com/" + userPoolId , userSession.getIdToken().getJWTToken());
        credentialsProvider.setLogins(logins);
        Log.i("TAG", " Cognito Login sync successfully for session " + userSession.getIdToken().getJWTToken());

        /*
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                Map<String, String> logins = credentialsProvider.getLogins();

                if(logins == null) {
                    logins = new HashMap<String, String>();
                }
                logins.put("cognito-idp." + Regions.US_WEST_2 + ".amazonaws.com/" + userPoolId , userSession.getIdToken().getJWTToken());
                credentialsProvider.setLogins(logins);
                credentialsProvider.refresh();


                // to get Identity ID
                String identityID = credentialsProvider.getIdentityId();


                Log.i("Identity ID=", "Identity ID=" + identityID );

                return null;
            }
        }.execute();
        Log.i("TAG", " Cognito Login sync successfully for session " + userSession.getIdToken().getJWTToken());
        */



    }


}

    //AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
