package mx.dagus.dagus;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class CreditsActivity extends AppCompatActivity {
    TextView titulo;
    Typeface gothamlight;
    Typeface gothambold;
    boolean tablet;
    TextView body;
    TextView bodytitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);

        gothambold = Typeface.createFromAsset(getAssets() , "fonts/gotham_bold.ttf");
        gothamlight = Typeface.createFromAsset(getAssets() , "fonts/gotham_light.ttf");

        titulo = (TextView) findViewById(R.id.credits_titulo);

        bodytitle = (TextView) findViewById(R.id.credits_bodytitle);
        bodytitle.setTextColor(getResources().getColor(R.color.white));
        bodytitle.setTypeface(gothambold);

        titulo.setTypeface(gothambold);
        titulo.setTextColor(getResources().getColor(R.color.white));

        tablet = getResources().getBoolean(R.bool.tablet);
        if (tablet == true) {
            titulo.setTextSize(52);
        } else {
            titulo.setTextSize(36); //44
        }

        if (tablet == true) {
            bodytitle.setTextSize(30);
        } else {
            bodytitle.setTextSize(20); //44
        }

        body = (TextView) findViewById(R.id.credits_body);
        body.setTypeface(gothamlight);
        body.setTextColor(getResources().getColor(R.color.white));
        body.setMovementMethod(new ScrollingMovementMethod());

        }

}
