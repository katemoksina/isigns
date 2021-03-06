package com.example.kate.table;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class TableActivity extends AppCompatActivity {
    static String eduroamForum = "10.173.19.58";
    static String eduroamHarrison = "10.173.19.71";
    static String homeNetwork = "192.168.0.17";
    public static String currentIP = homeNetwork;
    String timeSlot;
    JSONObject jsonline;
    Map<String, String> result;
    String[] times = new String[]{"mo1", "mo2", "mo3", "mo4", "mo5", "mo6", "mo7", "mo8", "mo9", "mo10", "tu1", "tu2", "tu3", "tu4", "tu5", "tu6", "tu7", "tu8", "tu9", "tu10", "we1", "we2", "we3", "we4", "we5", "we6", "we7", "we8", "we9", "we10", "th1", "th2", "th3", "th4", "th5", "th6", "th7", "th8", "th9", "th10", "fr1", "fr2", "fr3", "fr4", "fr5", "fr6", "fr7", "fr8", "fr9", "fr10"};


    private SoundPool mSoundPool;
    int clickID;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
        setContentView(R.layout.activity_table);

        mSoundPool = new SoundPool.Builder().build();
        clickID = mSoundPool.load(this, R.raw.click,1);

        //make GET request
        new GetDataTask().execute("http://"+currentIP+":3000/api/booking");
        handler.postDelayed(runnable, 60*30*1000);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            recreate();
            handler.postDelayed(this, 60*30*1000);
        }
    };

    public void seeDetails(View view){
        mSoundPool.play(clickID,1,1,1,0,1);
        Intent i = new Intent(TableActivity.this, DetailsActivity.class);
        timeSlot = view.getTag().toString();
        i.putExtra(DetailsActivity.EXTRA_TIME_SLOT, timeSlot);
        startActivity(i);
    }

    public void makeBooking(View view){
        mSoundPool.play(clickID,1,1,1,0,1);
        Intent i = new Intent(TableActivity.this, BookingActivity.class);
        timeSlot = view.getTag().toString();
        i.putExtra(BookingActivity.EXTRA_TIME_SLOT, timeSlot);
        startActivity(i);
    }

    class GetDataTask extends AsyncTask<String, Void, String> {
        ProgressDialog mProgressDialog;


        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            mProgressDialog = new ProgressDialog(TableActivity.this);
            mProgressDialog.setMessage("Loading data...");
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                getData(params[0]);
                return null;
            } catch (IOException e){
                return "network error!";
            }

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try{
            //set data response to textView
            for (String time : times){
                ViewGroup v = (ViewGroup)findViewById(R.id.table);
                Button b = (Button)v.findViewWithTag(time);
                if (TableActivity.this.result.keySet().contains(time)){
                    b.setBackgroundColor(Color.parseColor("#ff6666"));
                    String stringTime = TableActivity.this.result.get(time);
                    if (stringTime.length()>10){
                        stringTime = stringTime.substring(0,9)+"...";
                    }
                    b.setText(stringTime);
                } else {
                    b.setText("Available");
                    b.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            makeBooking(v);
                        }
                    });
                }
            }
            } catch (NullPointerException e){
                e.printStackTrace();
            }

            //remove progrss dialog
            if(mProgressDialog != null){
                mProgressDialog.dismiss();
            }

        }

        private Map<String, String> getData(String urlPath) throws IOException {
            Map<String, String> result = new HashMap<String, String>();
            BufferedReader bufferedReader = null;

            try {
                //connecting to server
                URL url = new URL(urlPath);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(10000);
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.connect();

                //reading data
                InputStream inputStream = urlConnection.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while((line = bufferedReader.readLine()) != null) {
                    try {
                        JSONArray jsonline = new JSONArray(line);

                        for (int i=0; i < jsonline.length(); i++){
                            JSONObject jsonObject = jsonline.getJSONObject(i);
                            result.put((String)jsonObject.get("tag"), (String)jsonObject.get("Act_title"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();

                    }
                }

            } finally {
                if (bufferedReader != null)
                    bufferedReader.close();
            }
            TableActivity.this.result = result;
            return result;
        }

    }
}
