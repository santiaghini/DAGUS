package mx.dagus.dagus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LandingActivity extends AppCompatActivity {
    AutoCompleteTextView buscador;
    ArrayList<Resultado> elementos;
    RequestQueue queue;
    JsonArrayRequest request;
    Typeface gothambold;
    ContenidoAdapter contenidoAdapter;
    ArrayList<String> nombres;
    boolean tablet;
    Typeface gothamlight;
    String name;
    String token;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        Log.d("hola" , "hola");
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("Dagus" , Context.MODE_PRIVATE);
        token = preferences.getString("token", null);

        if (token == null) {
            Intent intent1 = new Intent(LandingActivity.this , InicioActivity.class);
            startActivity(intent1);
        } else {
            Log.d("token" , token);
        }

        String name = preferences.getString("name", null);
        if (name!=null) {
            Toast hola = Toast.makeText(getApplicationContext(), "¡Hola " + name + "!", Toast.LENGTH_LONG);
            hola.show();
        }

        ImageView userpicture = (ImageView)  findViewById(R.id.landing_profilepicture);

        if (AccessToken.getCurrentAccessToken()!=null) {
            String userID = AccessToken.getCurrentAccessToken().getUserId();

            Log.d("userid", userID);

            if (userID != null) {
                Picasso.with(getApplicationContext())
                        .load("https://graph.facebook.com/" + userID + "/picture?type=large")
                        .into(userpicture);
            } else {
                userpicture.setVisibility(View.INVISIBLE);
            }

        }

        buscador = (AutoCompleteTextView) findViewById(R.id.landing_buscador);
        gothambold = Typeface.createFromAsset(getAssets(), "fonts/gotham_bold.ttf");
        buscador.setTypeface(gothambold);
        buscador.setTextColor(getResources().getColor(R.color.white));
        buscador.setHintTextColor(getResources().getColor(R.color.whiteTrans));
        Drawable background = buscador.getBackground();
        background.mutate().setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.white), PorterDuff.Mode.SRC_ATOP);
        buscador.setBackground(background);

        gothamlight = Typeface.createFromAsset(getAssets() , "fonts/gotham_light.ttf");
        TextView cerrartext = (TextView) findViewById(R.id.landing_cerrarsesion);
        cerrartext.setTypeface(gothamlight);
        cerrartext.setTextColor(getResources().getColor(R.color.cerrarsesion));

        elementos = new ArrayList<Resultado>();
        tablet = getResources().getBoolean(R.bool.tablet);
        if (tablet == true) {
            buscador.setTextSize(24);
        } else {
            buscador.setTextSize(16);//18
        }
        nombres = new ArrayList<String>();
        contenidoAdapter = new ContenidoAdapter(getApplicationContext() , android.R.layout.simple_list_item_1 , nombres);
        buscador.setAdapter(contenidoAdapter);
        buscador.setDropDownBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.dagusAzul)));
        Cache cache = new DiskBasedCache(getCacheDir() , 1024 * 1024);
        Network network = new BasicNetwork(new HurlStack());
        queue = new RequestQueue(cache , network);
        queue.start();
        buscador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.d("afterText" , "TextChanged");
                String texto = buscador.getText().toString();
                boolean isready = false;
                for (int contador = 0; contador < elementos.size(); contador++) {
                    if (texto.equals(elementos.get(contador).nombre)) {
                        Intent intent = new Intent(LandingActivity.this , ContenidoActivity.class);
                        intent.putExtra("tipo" , elementos.get(contador).tipo );
                        intent.putExtra("id" , elementos.get(contador).id);
                        intent.putExtra("nombre" , elementos.get(contador).nombre);
                        startActivity(intent);

                    }
                }
                try {
                    String url = "https://dagus.mx/api/?query=" + URLEncoder.encode(texto, "utf-8");
                    Log.d("url", url);
                    Log.d("texto", texto);
                    JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            contenidoAdapter.clear();
                            elementos.clear();
                            nombres.clear();
                            Log.d("response" , response.toString());
                            for (int contador = 0; contador < response.length(); contador++) {
                                try {
                                    JSONObject object = response.getJSONObject(contador);
                                    String nombre = object.getString("name");
                                    JSONObject id = object.getJSONObject("_id");
                                    String oid = id.getString("$oid");
                                    String tipo = object.getString("tipo");
                                    String color = object.getString("color");
                                    Resultado resultado = new Resultado(oid , tipo, nombre, color);
                                    contenidoAdapter.add(resultado.nombre);
                                    elementos.add(resultado);
                                    nombres.add(resultado.nombre);
                                    Log.d("object" , object.toString());
                                    Log.d("elemento", response.get(contador).toString());
                                } catch (JSONException exception) {
                                    Log.e("exception", "Hubo una excepción");
                                }
                            }
                            Log.d("elementos", elementos.toString());
                            contenidoAdapter.notifyDataSetChanged();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("error", error.toString());
                            Toast noconexion = Toast.makeText(getApplicationContext(), "No hay conexión", Toast.LENGTH_SHORT);
                            noconexion.show();

                        }
                    }) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> headers = new HashMap<>();
                            headers.put("Content-Type", "application/json");
                            headers.put("Authorization", token);
                            return headers;
                        }
                    };
                    queue.add(request);
                } catch (UnsupportedEncodingException e) {}
            }

        });

        buscador.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected (AdapterView<?> parent, View view, int position, long id) {
                String papa = buscador.getOnItemSelectedListener().toString();
                Log.d("selected" , papa);
                Intent intent = new Intent(LandingActivity.this, ContenidoActivity.class);
                startActivity(intent);
            }
            @Override
            public void onNothingSelected (AdapterView<?> parent) {

            }
        });

    }

    private class ContenidoAdapter extends ArrayAdapter<String> {
        Context context;
        int resourceid;
        ArrayList<String> data;
        public ContenidoAdapter(Context context , int resourceid , ArrayList<String> data) {
            super(context , resourceid , data);
            this.context = context;
            this.resourceid = resourceid;
            this.data = data;
        }
        @Override
        public int getCount() {
            return nombres.size();
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Nullable
        @Override
        public String getItem(int position) {
            return nombres.get(position);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null){
                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.celldropdown, null);
            }

            TextView textView = (TextView) convertView.findViewById(R.id.celldropdowntext);
            textView.setText(nombres.get(position).toUpperCase());
            textView.setTypeface(gothambold);
            if (tablet == true) {
                textView.setTextSize(24);
            } else {
                textView.setTextSize(16);
            }
            textView.setTextColor(getResources().getColor(android.R.color.white));

            return convertView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null){
                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.celldropdown, null);
            }

            TextView textView = (TextView) convertView.findViewById(R.id.celldropdowntext);
            textView.setText(nombres.get(position).toUpperCase());
            textView.setTypeface(gothambold);
            if (tablet == true) {
                textView.setTextSize(24);
            } else {
                textView.setTextSize(16);
            }
            textView.setTextColor(getResources().getColor(android.R.color.white));

            return convertView;
        }
    }

    public void hola(View view) {
        Intent intent = new Intent(LandingActivity.this, PruebaActivity.class);
        intent.putExtra("name", "grados");
        intent.putExtra("tit", "GRADOS");
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("Dagus" , Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);

        if (token == null) {
            Intent intent1 = new Intent(LandingActivity.this , InicioActivity.class);
            startActivity(intent1);
        } else {
            Log.d("token" , token);
        }
    }

    public void cerrarSesion(View view) {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("Dagus", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("token");
        editor.apply();
        Intent intent = new Intent(LandingActivity.this, InicioActivity.class);
        startActivity(intent);
    }

    public void credits(View view) {
        Intent intent = new Intent(LandingActivity.this, CreditsActivity.class);
        startActivity(intent);
    }

}

class Resultado {
    String id;
    String tipo;
    String nombre;
    String color;

    Resultado (String id, String tipo, String nombre , String color) {
        this.id = id;
        this.tipo = tipo;
        this.color = color;
        this.nombre = nombre;
    }
}
