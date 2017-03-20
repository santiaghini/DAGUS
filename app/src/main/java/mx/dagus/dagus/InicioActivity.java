package mx.dagus.dagus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;
import mx.dagus.dagus.TextureVideoView;
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

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;


public class InicioActivity extends AppCompatActivity {
    TextureVideoView videoView;
    Typeface gothamlight;
    Typeface gothambold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_inicio);

        gothamlight = Typeface.createFromAsset(getAssets() , "fonts/gotham_light.ttf");
        gothambold = Typeface.createFromAsset(getAssets() , "fonts/gotham_bold.ttf");

        Button loginbutton = (Button) findViewById(R.id.inicio_botoninicio);
        Button registerbutton = (Button) findViewById(R.id.inicio_register);

        registerbutton.setTypeface(gothambold);
        registerbutton.setTextColor(getResources().getColor(R.color.white));

        loginbutton.setTypeface(gothambold);
        loginbutton.setTextColor(getResources().getColor(R.color.almostTransAzul));

        Uri url = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.loginvideo);
        //String url = "https://dagus.mx/login-video.mp4";

        videoView = (TextureVideoView) findViewById(R.id.inicio_video);

        videoView.setDataSource(getApplicationContext() , url);
        /* videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });*/
        videoView.setScaleType(TextureVideoView.ScaleType.CENTER_CROP);
        videoView.setLooping(true);
        videoView.play();

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
        return String.format("%0" + (data.length * 2) + "x", new BigInteger(1, data));
    }

    public void registro(View view) {
        Intent intent = new Intent(InicioActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    public void iniciosesion(View view) {
        Intent intent = new Intent(InicioActivity.this, LoginActivity.class);
        startActivity(intent);
    }

}

