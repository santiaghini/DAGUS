package mx.dagus.dagus;

import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

public class VideoActivity extends AppCompatActivity {
    Typeface gothambold;
    TextView titu;
    boolean tablet;
    ProgressDialog pDialog;
    VideoView videoview;
    String name;

    // Insert your Video URL
    String videoURL;
    String archivo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        name = getIntent().getStringExtra("name").toUpperCase();
        archivo = getIntent().getStringExtra("archivo");

        titu = (TextView) findViewById(R.id.video_titulo);

        titu.setText(name);
        titu.setTypeface(gothambold);
        titu.setTextColor(getResources().getColor(R.color.white));
        tablet = getResources().getBoolean(R.bool.tablet);
        if (tablet == true) {
            titu.setTextSize(26);
        } else {
            titu.setTextSize(16); //44
        }

        videoview = (VideoView) findViewById(R.id.videoView);
        // Execute StreamVideo AsyncTask

        /*// Create a progressbar
        pDialog = new ProgressDialog(VideoActivity.this);
        // Set progressbar title
        pDialog.setTitle(name);
        // Set progressbar message
        pDialog.setMessage("Cargando...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        // Show progressbar
        pDialog.show();*/

        videoURL = "https://s3.amazonaws.com/dagus/" + archivo;

        try {
            // Start the MediaController
            MediaController mediacontroller = new MediaController(
                    VideoActivity.this);
            mediacontroller.setAnchorView(videoview);
            // Get the URL from String VideoURL
            Uri video = Uri.parse(videoURL);
            videoview.setMediaController(mediacontroller);
            videoview.setVideoURI(video);

        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }

        videoview.requestFocus();

        /*videoview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            // Close the progress bar and play the video
            public void onPrepared(MediaPlayer mp) {
                //pDialog.dismiss();
                videoview.start();
            }
        });*/
        videoview.start();
    }
}
