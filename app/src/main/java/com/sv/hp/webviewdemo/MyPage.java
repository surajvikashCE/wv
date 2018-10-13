package com.sv.hp.webviewdemo;

import android.app.ProgressDialog;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Iterator;

public class MyPage extends AppCompatActivity {
    WebView webView;
    EditText site;
    Button load;

    ProgressBar progressBar;
    Handler handler = new Handler();
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_activity);

        progressDialog = new ProgressDialog(this);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        site = (EditText) findViewById(R.id.ed);
        load = (Button) findViewById(R.id.but);
        webView = (WebView) findViewById(R.id.wb1);

        FirebaseMessaging.getInstance().subscribeToTopic("notifications");
        /*progressDialog.setMessage("load...");
        progressDialog.show();*//*
        webView.loadUrl("http://onibus.in/user/webapp/");
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, final int newProgress) {
                setProgress(newProgress);
                startprogress(newProgress);
            }
        });

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
       // progressDialog.dismiss();*/
        load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               //loadfunc();


                String token = FirebaseInstanceId.getInstance().getToken();
                Log.e("MAin Token--> ", token);
               // Toast.makeText(MyPage.this, "Main token--> "+token, Toast.LENGTH_SHORT).show();
                String str = site.getText().toString();

                DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("demo");
                mRef.child("name").setValue(str);
                mRef.child("roll").setValue("47").addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MyPage.this, "Data sent", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MyPage.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

   private void loadfunc() {

        String str = site.getText().toString();

        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, final int newProgress) {
                setProgress(newProgress);
                startprogress(newProgress);
            }
        });
        webView.loadUrl("http://"+str);

    }

    private void startprogress(final int newProgress) {

        progressBar.setVisibility(View.VISIBLE);
        progressDialog.setMessage("Loading... "+newProgress+"%");
        new Thread(new Runnable() {
            @Override
            public void run() {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(newProgress);
                        }
                    });
                }
        }).start();

        //progressDialog.show();
        if (newProgress==100){
            progressBar.setVisibility(View.GONE);
            progressDialog.dismiss();
        }

    }

    @Override
    public void onBackPressed() {
        if(webView.canGoBack())
        {
            webView.goBack();
        }
        else
        {
            finish();;
        }
    }
        //modify();


    private void modify() {

        try {


            JSONObject obj1 = new JSONObject(readJson("completedetails.json"));
            JSONObject obj2 = new JSONObject(readJson("user.json"));

            JSONArray names = null;
            Iterator<String> keys1 = obj1.keys();
            Iterator<String> keys2 = obj2.keys();



            JSONArray maleArray = new JSONArray();
            JSONArray femaleArray = new JSONArray();

            int i=0;
            while(keys2.hasNext()){
                String key = keys2.next();

                i++;
                JSONObject rollObj1 = obj1.getJSONObject(key);
                JSONObject rollObj2 = obj2.getJSONObject(key);

                JSONObject maleObject = new JSONObject();
                JSONObject femaleObject = new JSONObject();

                String email = rollObj1.getString("Email id");
                String name = rollObj1.getString("Name");
                String gender = rollObj1.getString("Gender");
                String mobile = rollObj1.getString("Mobile No");
                String Game_Count = rollObj2.getString("Game_Count");
                String REGISTERED_GAMES = rollObj2.getString("REGISTERED_GAMES");

                if(gender.equals("male")){
                    maleObject.put("Roll",key);
                    maleObject.put("Name", name);
                    maleObject.put("Gender",gender);
                    maleObject.put("Email Id", email);
                    maleObject.put("Mobile", mobile);
                    maleObject.put("Game_Count",Game_Count);
                    maleObject.put("REGISTERED_GAMES",REGISTERED_GAMES);
                    maleArray.put(maleObject);
                    Log.i("MAle--> "+i,maleObject.toString());
                }
                else if(gender.equals("female")){
                    femaleObject.put("Roll",key);
                    femaleObject.put("Name", name);
                    femaleObject.put("Gender",gender);
                    femaleObject.put("Email Id", email);
                    femaleObject.put("Mobile", mobile);
                    femaleObject.put("Game_Count",Game_Count);
                    femaleObject.put("REGISTERED_GAMES",REGISTERED_GAMES);
                    femaleArray.put(femaleObject);
                    Log.i("Female "+i, femaleObject.toString());
                }

            }

            JSONObject object = new JSONObject();
            object.put("Male",maleArray);
            JSONObject object2 = new JSONObject();
            object2.put("Female",femaleArray);
            writeFile(object, object2);

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void writeFile(JSONObject object, JSONObject object2) {
        try {
            Writer output = null;
            Writer output2 = null;
            File path = Environment.getExternalStorageDirectory();
            File file = new File(path, "male.json");
            File file2 = new File(path,"female.json");
            output = new BufferedWriter(new FileWriter(file));
            output2 = new BufferedWriter(new FileWriter(file2));
            output.write(object.toString());
            Log.i("MAle-->",object.toString());
            Log.i("Female", object2.toString());
            output.close();
            output2.write(object2.toString());
            output2.close();
            Toast.makeText(getApplicationContext(), "Composition saved", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }


    private String readJson(String file) {
        String json = null;
        try {
            InputStream is1 = getAssets().open(file);
            int size = is1.available();
            byte[] buffer = new byte[size];
            is1.read(buffer);
            is1.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }



}
