package mx.dagus.dagus;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class GuiaActivity extends AppCompatActivity {
String archivo;
    WebView guia;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guia);
        archivo = getIntent().getStringExtra("archivo");
        guia = (WebView) findViewById(R.id.guia_web);
        String url = "https://s3.amazonaws.com/dagus/" + archivo;
        //Toast url2 = Toast.makeText(getApplicationContext(), url , Toast.LENGTH_LONG);
        //url2.show();
        Log.d("url" , url);
        guia.loadUrl(url);
    }
}
