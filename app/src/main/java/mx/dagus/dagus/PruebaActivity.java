package mx.dagus.dagus;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidentityprovider.AmazonCognitoIdentityProvider;
import com.amazonaws.services.cognitoidentityprovider.AmazonCognitoIdentityProviderClient;

import java.io.IOException;
import java.io.InputStream;

public class PruebaActivity extends AppCompatActivity {

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
    CognitoUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prueba);

        emailText = (EditText) findViewById(R.id.prueba_email);
        passwordText = (EditText) findViewById(R.id.prueba_password);
        confirmcodeText = (EditText) findViewById(R.id.prueba_code);



    }

    public void registrar (View view) {
        Context context = getApplicationContext();

        ClientConfiguration clientConfiguration = new ClientConfiguration();
        AmazonCognitoIdentityProvider cipClient = new AmazonCognitoIdentityProviderClient(new AnonymousAWSCredentials(), clientConfiguration);
        cipClient.setRegion(Region.getRegion(cognitoRegion));
        CognitoUserPool userPool = new CognitoUserPool(context, userPoolId, clientId, clientSecret, cipClient);



        // Create a CognitoUserAttributes object and add user attributes
        CognitoUserAttributes userAttributes = new CognitoUserAttributes();

        // Add the user attributes. Attributes are added as key-value pairs
        // Adding user's given name.
        // Note that the key is "given_name" which is the OIDC claim for given name
        //userAttributes.addAttribute("given_name", userGivenName);

        // Adding user's phone number
        //userAttributes.addAttribute("phone_number", phoneNumber);
        // Adding user's email address
        final String emailAddress = emailText.getText().toString();
        Log.d("email" , emailAddress);
        userAttributes.addAttribute("email", emailAddress);

        final String password = passwordText.getText().toString();



        SignUpHandler signupCallback = new SignUpHandler() {

            @Override
            public void onSuccess(CognitoUser cognitoUser, boolean userConfirmed, CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
                // Sign-up was successful

                // Check if this user (cognitoUser) needs to be confirmed
                if(!userConfirmed) {
                    // This user must be confirmed and a confirmation code was sent to the user
                    // cognitoUserCodeDeliveryDetails will indicate where the confirmation code was sent
                    // Get the confirmation code from user

                    String medium = cognitoUserCodeDeliveryDetails.getDeliveryMedium();
                    Log.d("medium" , medium);
                    String attribute = cognitoUserCodeDeliveryDetails.getAttributeName();
                    Log.d("attributename" , attribute);
                    String destination = cognitoUserCodeDeliveryDetails.getDestination();
                    Log.d("destination" , destination);

                    if (attribute.equals("email")){
                        Toast.makeText(getApplicationContext() , "Se ha enviado un correo de confirmación a " + emailAddress , Toast.LENGTH_LONG).show();
                    }

                }
                else {
                    // The user has already been confirmed
                }
            }

            @Override
            public void onFailure(Exception exception) {
                // Sign-up failed, check exception for the cause
                Log.d("exception" , exception.toString());
            }
        };


        userPool.signUpInBackground(emailAddress, password, userAttributes, null, signupCallback);


        cognitoUser = userPool.getCurrentUser();

        // Callback handler for confirmSignUp API
        confirmationCallback = new GenericHandler() {

            @Override
            public void onSuccess() {
                // User was successfully confirmed
            }

            @Override
            public void onFailure(Exception exception) {
                // User confirmation failed. Check exception for the cause.
            }
        };

        String code = confirmcodeText.getText().toString();

        cognitoUser.confirmSignUp(code, true, confirmationCallback);



        /* // create a callback handler for the confirmation code request
        GenericHandler handler = new GenericHandler() {

            @Override
            public void onSuccess() {
                // Confirmation code was successfully sent!
            }
            @Override
            public void onFailure(Exception exception) {
                // Confirmation code request failed, probe exception for details
            }
        };

        user.resendConfirmationCode(handler);
        */





        /*

        ForgotPasswordHandler handler = new ForgotPasswordHandler {
            @Override
            public void onSuccess() {
                // Forgot password process completed successfully, new password has been successfully set

            }

            @Override
            public void getResetCode(ForgotPasswordContinuation continuation) {
                // A code will be sent, use the "continuation" object to continue with the forgot password process

                // This will indicate where the code was sent
                String codeSentHere = continuation.getParameters();

                // Code to get the code from the user - user dialogs etc.

                // If the program control has to exit this method, take the "continuation" object.
                // "continuation" is the only possible way to continue with the process



                // When the code is available

                // Set the new password
                continuation.setPassword(newPassword);

                // Set the code to verify
                continuation.setVerificationCode(code);

                // Let the forgot password process proceed
                continuation.continueTask();

            }

            /**
             * This is called for all fatal errors encountered during the password reset process
             * Probe {@exception} for cause of this failure.
             * @param exception
             */
        /*public void onFailure(Exception exception) {
            // Forgot password processing failed, probe the exception for cause
        }
}

        user.forgotPassword(handler);






        */


        /*
        // Callback handler for the sign-in process
        AuthenticationHandler authenticationHandler = new AuthenticationHandler() {

            @Override
            public void onSuccess(CognitoUserSession cognitoUserSession) {
                // Sign-in was successful, cognitoUserSession will contain tokens for the user

            }

            @Override
            public void getAuthenticationDetails(final AuthenticationContinuation authenticationContinuation, final String userId) {
                // User authentication details, userId and password are required to continue.
                // Use the "continuation" object to pass the user authentication details

                // After the user authentication details are available, wrap them in an AuthenticationDetails class
                // Along with userId and password, parameters for user pools for Lambda can be passed here
                // The validation parameters "validationParameters" are passed in as a Map<String, String>
                AuthenticationDetails authenticationDetails = new AuthenticationDetails(userId, password, null);

                // Pass the user sign-in credentials to the continuation
                authenticationContinuation.setAuthenticationDetails(authenticationDetails);

                // Allow the sign-in to continue
                authenticationContinuation.continueTask();
            }

            @Override
            public void getMFACode(MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation) {


                // Multi-factor authentication is required; get the verification code from user
                String mfaVerificationCode = confirmcodeText.getText().toString();
                multiFactorAuthenticationContinuation.setMfaCode(mfaVerificationCode);
                // Allow the sign-in process to continue
                multiFactorAuthenticationContinuation.continueTask();
            }

            /*@Override
            public void authenticationChallenge(final ChallengeContinuation continuation) {
                // A custom challenge has to be solved to authenticate

                // Set the challenge responses

                // Call continueTask() method to respond to the challenge and continue with authentication.
            } */

            /*
            @Override
            public void onFailure(Exception exception) {
                // Sign-in failed, check exception for the cause
            }
        };

// Sign in the user
        cognitoUser.getSessionInBackground(authenticationHandler);

        */

    }


    // This has cleared all tokens and this user will have to go through the authentication process to get tokens.
    //user.signOut();


}