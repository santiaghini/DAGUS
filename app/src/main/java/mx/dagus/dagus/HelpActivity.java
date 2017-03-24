package mx.dagus.dagus;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class HelpActivity extends AppCompatActivity {

    Typeface gothambold;
    Typeface gothamlight;
    boolean tablet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        gothambold = Typeface.createFromAsset(getAssets() , "fonts/gotham_bold.ttf");
        gothamlight = Typeface.createFromAsset(getAssets() , "fonts/gotham_light.ttf");

        TextView title = (TextView) findViewById(R.id.help_titulo);
        TextView body1 = (TextView) findViewById(R.id.help_body1);
        TextView body2 = (TextView) findViewById(R.id.help_body2);

        title.setTextColor(getResources().getColor(R.color.white));
        title.setTypeface(gothambold);

        body1.setTypeface(gothamlight);
        body1.setTextColor(getResources().getColor(R.color.white));

        body2.setTypeface(gothamlight);
        body2.setTextColor(getResources().getColor(R.color.white));

        tablet = getResources().getBoolean(R.bool.tablet);
        if (tablet == true) {
            title.setTextSize(52);
        } else {
            title.setTextSize(36); //44
        }

        if (tablet == true) {
            body1.setTextSize(30);
        } else {
            body1.setTextSize(20); //44
        }

        if (tablet == true) {
            body2.setTextSize(26);
        } else {
            body2.setTextSize(18); //44
        }
    }
}
