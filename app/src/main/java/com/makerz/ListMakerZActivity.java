package com.makerz;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
//import android.support.v4.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.makerz.adapter.MyArrayAdapter;
import com.makerz.model.MyDataModel;
import com.makerz.parser.JSONParser;
import com.makerz.util.InternetConnection;
import com.makerz.util.Keys;
import com.facebook.drawee.backends.pipeline.Fresco;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

// This activity is used to show payment status and name of each MakerZ member for each semester.
// The data can only be edited in Microsoft Excel sheet which exists at hwmakerz@gmail.com account.

public class ListMakerZActivity extends AppCompatActivity {

    private ArrayList<MyDataModel> list;

    private ListView listView;
    private MyArrayAdapter adapter;

    private Button refresh;
    private Button back;

    private  Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_maker_z);
        setupUIViews();

        /*** Array List for Binding Data from JSON to this list */
        list = new ArrayList<>();

        /*** Bind that list to MyArrayAdapter */
        adapter = new MyArrayAdapter(this, list);

        /*** Get and set list for MyArrayAdapter */
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Snackbar.make(findViewById(R.id.parentLayout), list.get(position).getName() + " => " + list.get(position).getFees(), Snackbar.LENGTH_LONG).show();
            }
        });

        /*** Just to know onClick and Printing Hello Toast in Center.*/
        toast = Toast.makeText(getApplicationContext(), "Click on refresh to load JSON.", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    finish();
            }
        });

        // Check internet connection.
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View view) {

                if (InternetConnection.checkConnection(getApplicationContext())) {
                    new GetDataTask().execute();

                } else {
                    Snackbar.make(view, "Internet Connection Not Available", Snackbar.LENGTH_LONG).show();
                }
            }


        });
    }

            /*** Creating Get Data Task for Getting Data From Web */

            class GetDataTask extends AsyncTask<Void, Void, Void> {

                ProgressDialog dialog;
                int jIndex;
                int x;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();

                    /*** Progress Dialog for User Interaction */

                    x = list.size();

                    if (x == 0)
                        jIndex = 0;
                    else
                        jIndex = x;

                    dialog = new ProgressDialog(ListMakerZActivity.this);
                    dialog.setTitle("Please wait..." + "(" +x+ ")");
                    dialog.setMessage("Loading JSON...");
                    dialog.show();
                }

                @Nullable
                @Override
                protected Void doInBackground(Void... params) {

                    /*** Get JSON Object from Web Using okHttp */
                    JSONObject jsonObject = JSONParser.getDataFromWeb();

                    try {

                        /*** Check if it is NULL??? */
                        if (jsonObject != null) {

                            /*** Check length... */
                            if (jsonObject.length() > 0) {

                                /*** Getting array named "contacts" From MAIN Json Object */
                                JSONArray array = jsonObject.getJSONArray(Keys.KEY_CONTACTS);

                                /*** Check Length of Array... */
                                int lenArray = array.length();
                                if (lenArray > 0) {
                                    for (; jIndex < lenArray; jIndex++) {

                                        /*** Create every time there is a new object* and * add to list */
                                        MyDataModel model = new MyDataModel();

                                        /*** Get Inner Object from contacts array...
                                         * and * From that We will get Name of that Contact **/
                                        JSONObject innerObject = array.getJSONObject(jIndex);
                                        String name = innerObject.getString(Keys.KEY_NAME);
                                        String fees = innerObject.getString(Keys.KEY_FEES);

                                        model.setName(name);
                                        model.setFees(fees);

                                        /*** Add name and phone concatenation in list. */
                                        list.add(model);
                                    }
                                }
                            }
                        }
                        else { }

                    } catch (JSONException je) {
                        Log.i(JSONParser.TAG, "" + je.getLocalizedMessage());
                    }
                    return null;
                }

            @Override
            protected void onPostExecute (Void aVoid){
                super.onPostExecute(aVoid);
                dialog.dismiss();

                /*** Check if list size if more than zero then * update ListView */
                if (list.size() > 0) {
                    adapter.notifyDataSetChanged();
                } else {
                    Snackbar.make(findViewById(R.id.parentLayout), "No Data Found", Snackbar.LENGTH_LONG).show();
                }
            }
    }

    // Declare elements
    private void setupUIViews() {
        refresh = findViewById(R.id.button_refresh_list);
        back = findViewById(R.id.button_backz2);
    }


}
