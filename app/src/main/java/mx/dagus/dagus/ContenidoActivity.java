package mx.dagus.dagus;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import android.content.Context;

public class ContenidoActivity extends AppCompatActivity {
    RequestQueue queue;
    String name;
    TextView titu;
    String tit;
    ArrayList<Shared> elementos;
    Typeface gothambold;
    TextView textView;
    ContenidoAdapter contenidoAdapter;
    String id;
    String tipo;
    String type;
    boolean tablet;
    String nombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contenido);
        gothambold = Typeface.createFromAsset(getAssets() , "fonts/gotham_bold.ttf");

        titu = (TextView) findViewById(R.id.contenido_titulo);

        name = getIntent().getStringExtra("name");
        tit = getIntent().getStringExtra("tit");
        tipo = getIntent().getStringExtra("tipo");
        id = getIntent().getStringExtra("id");
        nombre = getIntent().getStringExtra("nombre").toUpperCase();

        if (tipo.equals("subjects")){
            titu.setText(nombre + "_");
        } else {
            titu.setText("MATERIAS DE " + nombre + "_");
        }
        titu.setTypeface(gothambold);
        titu.setTextColor(getResources().getColor(R.color.white));
        tablet = getResources().getBoolean(R.bool.tablet);
        if (tablet == true) {
            titu.setTextSize(52);
        } else {
            titu.setTextSize(36); //44
        }

        elementos = new ArrayList<Shared>();

        queue = Volley.newRequestQueue(this);

        contenidoAdapter = new ContenidoAdapter(getApplicationContext(), R.layout.cell, elementos);
        final ListView list = (ListView) findViewById(R.id.contenido_list);
        list.setAdapter(contenidoAdapter);

        final String url = "https://dagus.mx/api/" + tipo + "/_id/" + id;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("response" , response.toString() );
                Log.d("url" , url);
                if (tipo.equals("grades")) {
                    try {
                        JSONArray materias = response.getJSONArray("subjects");
                        if (materias.length() == 0) {
                            Toast nomaterias = Toast.makeText(getApplicationContext() , getString(R.string.nohaymaterias) , Toast.LENGTH_LONG);
                            nomaterias.show();
                        } else {
                            contenidoAdapter.clear();
                            elementos.clear();
                            Log.d("grados_length" , String.valueOf(response.length()));
                            for (int contador = 0 ; contador < materias.length() ; contador ++) {
                                try {
                                    JSONObject object = materias.getJSONObject(contador);
                                    String nombre = object.getString("name");
                                    String color = object.getString("color");
                                    String colegio = object.getString("school");
                                    String image = object.getString("icon");
                                    JSONObject id = object.getJSONObject("_id");
                                    String clave = object.getString("key");
                                    String oid = id.getString("$oid");
                                    Materia materia = new Materia(nombre , color, oid , clave , colegio , image);
                                    contenidoAdapter.add(materia);

                                } catch (JSONException exception) {
                                    Log.e("exception", "Hubo una excepción");
                                }
                            }

                            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                    Materia materia = (Materia) elementos.get(position);

                                    Intent intent = new Intent(ContenidoActivity.this , ContenidoActivity.class);
                                    intent.putExtra("nombre", materia.nombre);
                                    intent.putExtra("tipo" , "subjects");
                                    intent.putExtra("id" , materia.id);
                                    startActivity(intent);
                                }
                            });

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (tipo.equals("subjects")) {
                    try {
                        JSONArray guias = response.getJSONArray("guides");
                        if (guias.length() == 0) {
                            Toast noguias = Toast.makeText(getApplicationContext() , getString(R.string.nohayguias) , Toast.LENGTH_LONG);
                            noguias.show();
                        } else {
                            contenidoAdapter.clear();
                            elementos.clear();
                            Log.d("response" , String.valueOf(response.length()));
                            for (int contador = 0 ; contador < guias.length() ; contador ++) {
                                try {
                                    JSONObject object = guias.getJSONObject(contador);
                                    String nombre = object.getString("name");
                                    String archivo = object.getString("file");
                                    JSONObject id = object.getJSONObject("_id");
                                    String oid = id.getString("$oid");
                                    String type = object.getString("type");
                                    String image = object.getString("icon");
                                    Log.d("type" , type);

                                    File file = new File(nombre , archivo , oid ,  type , image);
                                    contenidoAdapter.add(file);

                                } catch (JSONException exception) {
                                    Log.e("exception", "Hubo una excepción");
                                }
                            }

                            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    File file = (File) elementos.get(position);

                                    if (file.type.equals("markdown")) {
                                        Intent intent = new Intent(ContenidoActivity.this, GuiaActivity.class);
                                        intent.putExtra("archivo", file.archivo);
                                        startActivity(intent);
                                    } else {
                                        Intent intent = new Intent(ContenidoActivity.this, VideoActivity.class);
                                        intent.putExtra("archivo", file.archivo);
                                        intent.putExtra("name" , file.nombre);
                                        startActivity(intent);
                                    }


                                }
                            });

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast noguias = Toast.makeText(getApplicationContext() , getString(R.string.nohayguias) , Toast.LENGTH_LONG);
                        noguias.show();
                    }
                }

                contenidoAdapter.notifyDataSetChanged();
            }

        }
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error" , error.toString());
                Toast fallo = Toast.makeText(getApplicationContext() , getString(R.string.revisa) , Toast.LENGTH_LONG);
                fallo.show();
                Intent intent = new Intent(ContenidoActivity.this, LandingActivity.class);
                startActivity(intent);
            }
        })
            {

        @Override
        public Map<String , String> getHeaders() throws AuthFailureError {
            Map <String, String> headers = new HashMap<>();
            headers.put("Content-Type" , "application/json");
            headers.put("Authorization" , "c1b79fc950276536c1fdb0d3f2dc4d18a15872671143465a743398da1eb0fcd4");
            return headers;}
    };

        queue.add(request);

    }

    private class ContenidoAdapter extends ArrayAdapter<Shared> {
        Context context;
        int resourceid;
        ArrayList<Shared> data;
        public ContenidoAdapter(Context context , int resourceid , ArrayList<Shared> data) {
            super(context , resourceid , data);
            this.context = context;
            this.resourceid = resourceid;
            this.data = data;
        }
        @Override
        public int getCount() {
            return elementos.size();
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Nullable
        @Override
        public Shared getItem(int position) {
            return elementos.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null){
                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.cell, null);
            }

            TextView textView = (TextView) convertView.findViewById(R.id.cell_text_view);
            textView.setText(elementos.get(position).nombre.toUpperCase());
            textView.setTypeface(gothambold);
            if (tablet == true) {
                textView.setTextSize(30);
            } else {
                textView.setTextSize(24);//28
            }


            if (elementos.get(position).image != null) {
                String url = "https://s3.amazonaws.com/dagus/" + elementos.get(position).image;

                ImageView imageview = (ImageView) convertView.findViewById(R.id.cell_image);
                Picasso.with(getApplicationContext())
                        .load(url).centerCrop().resize(96,96)
                        .into(imageview);
                LinearLayout.LayoutParams parameters = new LinearLayout.LayoutParams(96,96);
                imageview.setLayoutParams(parameters);
            }

            return convertView;
        }
    }

    private class ViewHolder {
        TextView textView;

    }

}

class Shared {
    String nombre;
    String id;
    String image;
}

class File extends Shared {
    String archivo;
    String type;

    File (String nombre , String archivo , String id , String type , String image) {
        this.nombre = nombre;
        this.archivo = archivo;
        this.id = id;
        this.type = type;
        this.image = image;

    }
}

class Materia extends Shared {
    String color;
    String clave;
    String colegio;

    Materia (String nombre , String color , String id , String clave , String colegio , String image) {
        this.nombre = nombre;
        this.color = color;
        this.id = id;
        this.clave = clave;
        this.colegio = colegio;
        this.image = image;
    }
}

