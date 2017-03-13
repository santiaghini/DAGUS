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
    ArrayList<String> elementos;
    Typeface gothambold;
    TextView textView;
    ContenidoAdapter contenidoAdapter;
    String id;
    String tipo;
    boolean tablet;
    String nombre;
    ArrayList<Guia> arrayguia = new ArrayList<Guia>();
    ArrayList<Materia> arraymateria = new ArrayList<Materia>();

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

        if (tipo.equals("materias")){
            titu.setText(nombre + "_");
        } else {
            titu.setText("MATERIAS DE " + nombre + "_");
        }
        titu.setTypeface(gothambold);
        titu.setTextColor(getResources().getColor(R.color.titulo_contenido));
        tablet = getResources().getBoolean(R.bool.tablet);
        if (tablet == true) {
            titu.setTextSize(52);
        } else {
            titu.setTextSize(36); //44
        }

        elementos = new ArrayList<String>();

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
                if (tipo.equals("grados")) {
                    try {
                        JSONArray materias = response.getJSONArray("materias");
                        if (materias.length() == 0) {
                            Toast nomaterias = Toast.makeText(getApplicationContext() , "No hay materias disponibles" , Toast.LENGTH_LONG);
                            nomaterias.show();
                        } else {
                            contenidoAdapter.clear();
                            elementos.clear();
                            Log.d("grados_length" , String.valueOf(response.length()));
                            for (int contador = 0 ; contador < materias.length() ; contador ++) {
                                try {
                                    JSONObject object = materias.getJSONObject(contador);
                                    String nombre = object.getString("nombre");
                                    contenidoAdapter.add(nombre);
                                    String color = object.getString("color");
                                    String colegio = object.getString("colegio");
                                    JSONObject id = object.getJSONObject("_id");
                                    String clave = object.getString("clave");
                                    String oid = id.getString("$oid");
                                    Materia materia = new Materia(nombre , color, oid , clave , colegio);
                                    arraymateria.add(materia);

                                } catch (JSONException exception) {
                                    Log.e("exception", "Hubo una excepción");
                                }
                            }

                            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                    Materia materia = arraymateria.get(position);

                                    Intent intent = new Intent(ContenidoActivity.this , ContenidoActivity.class);
                                    intent.putExtra("nombre", materia.nombre);
                                    intent.putExtra("tipo" , "materias");
                                    intent.putExtra("id" , materia.id);
                                    startActivity(intent);
                                }
                            });

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (tipo.equals("materias")) {
                    try {
                        JSONArray guias = response.getJSONArray("guias");
                        if (guias.length() == 0) {
                            Toast noguias = Toast.makeText(getApplicationContext() , "No hay guías disponibles" , Toast.LENGTH_LONG);
                            noguias.show();
                        } else {
                            contenidoAdapter.clear();
                            elementos.clear();
                            arrayguia.clear();
                            Log.d("response" , String.valueOf(response.length()));
                            for (int contador = 0 ; contador < guias.length() ; contador ++) {
                                try {
                                    JSONObject object = guias.getJSONObject(contador);
                                    String nombre = object.getString("nombre");
                                    contenidoAdapter.add(nombre);
                                    String archivo = object.getString("archivo");
                                    JSONObject id = object.getJSONObject("_id");
                                    String oid = id.getString("$oid");
                                    Guia guia = new Guia(nombre, archivo, oid);
                                    arrayguia.add(guia);

                                } catch (JSONException exception) {
                                    Log.e("exception", "Hubo una excepción");
                                }
                            }

                            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Guia guia = arrayguia.get(position);

                                    Intent intent = new Intent(ContenidoActivity.this , GuiaActivity.class);
                                    intent.putExtra("archivo", guia.archivo);
                                    startActivity(intent);
                                }
                            });

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast noguias = Toast.makeText(getApplicationContext() , "No hay guías disponibles" , Toast.LENGTH_LONG);
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
            return elementos.size();
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Nullable
        @Override
        public String getItem(int position) {
            return elementos.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null){
                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.cell, null);
            }

            TextView textView = (TextView) convertView.findViewById(R.id.cell_text_view);
            textView.setText(elementos.get(position).toUpperCase());
            textView.setTypeface(gothambold);
            if (tablet == true) {
                textView.setTextSize(30);
            } else {
                textView.setTextSize(24);//28
            }

            return convertView;
        }
    }

    private class ViewHolder {
        TextView textView;

    }

}

class Guia {
    String nombre;
    String archivo;
    String id;

    Guia (String nombre , String archivo , String id) {
        this.nombre = nombre;
        this.archivo = archivo;
        this.id = id;
    }
}

class Materia {
    String nombre;
    String color;
    String id;
    String clave;
    String colegio;

    Materia (String nombre , String color , String id , String clave , String colegio) {
        this.nombre = nombre;
        this.color = color;
        this.id = id;
        this.clave = clave;
        this.colegio = colegio;
    }
}

