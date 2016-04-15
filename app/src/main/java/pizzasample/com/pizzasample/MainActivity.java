package pizzasample.com.pizzasample;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends Activity {

    private static final String TAG = "PizzaSample";
    private ProgressBar mSpinner;
    private TextView mSearchLable;
    private ArrayAdapter<String> mAdapter;
    private ListView mMainListView;
    private Map<Integer, Set<String>> mPizzaFinalList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSpinner = (ProgressBar) findViewById(R.id.spinner);
        mSearchLable = (TextView) findViewById(R.id.search_label);
        mSpinner.setVisibility(View.VISIBLE);
        mSpinner.setMax(1000);
        mSpinner.setIndeterminate(true);

        new SearchPopularPizzaTask().execute("");

        mMainListView = (ListView) findViewById(R.id.pizzalist);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private String getJsonString() {
        BufferedReader br = null;
        StringBuffer sb = null;
        try {
            br = new BufferedReader(new InputStreamReader(getAssets().open(
                    "pizzas.json")));
            sb = new StringBuffer();
            String temp;
            while ((temp = br.readLine()) != null) {
                sb.append(temp);
            }
        } catch (IOException e) {
            Log.e(TAG, " Error while reading json : " + e.getLocalizedMessage());
        } finally {
            try {
                br.close(); // stop reading
            } catch (IOException e) {
                Log.e(TAG, " Error while reading json : " + e.getLocalizedMessage());
            }
        }
        return (sb != null) ? sb.toString() : null;
    }

    private int countSet(Set<String> toppings, List<Set<String>> list) {
        int count = 0;

        for (Set<String> set : list) {
            if (set.equals(toppings)) {
                count++;
            }
        }
        return count;
    }

    private class SearchPopularPizzaTask extends AsyncTask<String, Void, String> {

        boolean searchDone = false;

        @Override
        protected String doInBackground(String... params) {
            try {
                String jsonString = getJsonString();

                // parsing json file.
                if (jsonString != null) {

                    JSONArray jsonArray = new JSONArray(jsonString);
                    if(jsonArray != null && jsonArray.length() > 0) {
                        // List<PizzaToppings> pizzaList = new ArrayList<>();
                        List<Set<String>> pizzaList = new ArrayList<>();

                        Map<Set<String>, Integer> favPizzaLookup = new HashMap<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject pizzaObject = jsonArray.getJSONObject(i); // toppings
                            // parse each toppings object
                            JSONArray toppingsArray = pizzaObject.getJSONArray("toppings");
                            Set<String> toppings = new HashSet<>();
                            for (int k = 0; k < toppingsArray.length(); k++) {
                                toppings.add(toppingsArray.getString(k));
                            }
                            pizzaList.add(toppings);
                            int count = countSet(toppings, pizzaList);
                            favPizzaLookup.put(toppings, count);
                        }

                        List<Integer> sortValues = new ArrayList<>(favPizzaLookup.values());
                        Collections.sort(sortValues);
                        Collections.reverse(sortValues);

                        if(sortValues.size() >= 20) {
                            List<Integer> top20 = sortValues.subList(0, 20);
                            mPizzaFinalList = new HashMap<>();

                            for (Map.Entry<Set<String>, Integer> entry : favPizzaLookup
                                    .entrySet()) {
                                boolean found = top20.contains(entry.getValue());
                                if (found) {
                                    mPizzaFinalList.put(entry.getValue(), entry.getKey());
                                }
                            }
                        }
                    }
                }
            } catch (JSONException exception) {
                Log.i(TAG, "Exception while parsing JSON : " + exception.getLocalizedMessage());
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {

            mSearchLable.setText("Search Done for First 20 Popular Pizza");
            Toast.makeText(getApplicationContext(), "Search Done for First 20 Popular Pizza",
                    Toast.LENGTH_LONG).show();
            mSpinner.setVisibility(View.INVISIBLE);
            List<String> adapterList = new ArrayList<>();

            StringBuilder sb = null;

            // populate date into array adapter

            List<Integer> finalPizzaList = new ArrayList<>(mPizzaFinalList.keySet());
            Collections.sort(finalPizzaList);
            Collections.reverse(finalPizzaList);

            for (Integer timesPizzaOrdered : finalPizzaList) {
                sb = new StringBuilder();
                sb.append(mPizzaFinalList.get(timesPizzaOrdered));
                sb.append(" : ordered ");
                sb.append(timesPizzaOrdered);
                sb.append(" times ");
                adapterList.add(sb.toString());

            }

            mAdapter = new ArrayAdapter<String>(getApplicationContext(),
                    android.R.layout.simple_dropdown_item_1line, adapterList);
            mMainListView.setVisibility(View.VISIBLE);
            mSearchLable.setVisibility(View.INVISIBLE);
            mMainListView.setAdapter(mAdapter);

        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            if (searchDone) {
                mSpinner.setVisibility(View.GONE);
            }
        }
    }


}
