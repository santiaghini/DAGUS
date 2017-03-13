package mx.dagus.dagus;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

public class PruebaActivity extends AppCompatActivity {
TextView textView;
    String str;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prueba);
        //AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        String[] countries = getResources().getStringArray(R.array.materias_array);
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, countries);
        textView = (TextView) findViewById(R.id.prueba_view);

        try {
            InputStream stream = getAssets().open("Derecho.html");
            int size = stream.available();

            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();

            str = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        textView.setText(Html.fromHtml(str));

        // textView.setAdapter(adapter);
    }
}
   /* String texto = buscador.getText().toString();
    String url = "https://dagus.mx/api/?query=" + texto;
queue = Volley.newRequestQueue(getApplicationContext());
        JSONObject busqueda = new JSONObject();
        try {
        busqueda.put("busca", buscador.getText().toString());
        request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
@Override
public void onResponse(JSONArray response) {
        elementos.clear();
        for (int contador = 0; contador < response.length(); contador++) {
        try {
        JSONObject object = response.getJSONObject(contador);
        String nombre = object.getString("nombre");
        elementos.add(nombre);
        Log.d("elemento", response.get(contador).toString());
        } catch (JSONException exception) {
        Log.e("exception", "Hubo una excepción");
        }

        } arrayAdapter.notifyDataSetChanged();

        }

        }
        , new Response.ErrorListener() {
@Override
public void onErrorResponse(VolleyError error) {
        Log.d("error", error.toString());
        }
        })

        {
@Override
public Map<String , String> getHeaders() throws AuthFailureError {
        Map <String, String> headers = new HashMap<>();
        headers.put("Content-Type" , "application/json");
        headers.put("Authorization" , "6ac0e21ec8de7bd72e58aa2bca1983aa9cfe534ff4b2df1255307e75c714a664");
        return headers;}
        };

        } catch (JSONException exception) {
        Log.e("exception", "Hubo una excepción");
        }

        queue.add(request); */
