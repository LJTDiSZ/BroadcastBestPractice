package com.jcc.broadcastbestpractice;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private EditText edit1;
    private MyDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button forceOffline = (Button)findViewById(R.id.force_offline);
        forceOffline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.jcc.broadcastbestpractice.FORCE_OFFLINE");
                sendBroadcast(intent);
            }
        });

        edit1 = (EditText)findViewById(R.id.edit1);

        String inputText = load();
        if (!TextUtils.isEmpty(inputText)){
            edit1.setText(inputText);
            edit1.setSelection(inputText.length());
            Toast.makeText(this, "Restoring succeeded", Toast.LENGTH_SHORT).show();
        }

        Button saveData = (Button)findViewById(R.id.save_data);
        saveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = getSharedPreferences("data2", MODE_PRIVATE).edit();
                editor.putString("name", "Tom");
                editor.putInt("age", 28);
                editor.putBoolean("married", false);
                editor.commit();
            }
        });
        Button restoreData = (Button)findViewById(R.id.restore_data);
        restoreData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences pref = getSharedPreferences("data2", MODE_PRIVATE);
                String name = pref.getString("name", "");
                int age = pref.getInt("age", 0);
                boolean married = pref.getBoolean("married", false);
                Log.d("MainActivity", "name is " + name);
                Log.d("MainActivity", "age is " + age);
                Log.d("MainActivity", "married is " + married);
            }
        });

        dbHelper = new MyDatabaseHelper(this, "BookStore.db", null, 2);
        Button createDb = (Button)findViewById(R.id.create_db);
        createDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbHelper.getWritableDatabase();
            }
        });
        Button addData = (Button)findViewById(R.id.add_data);
        addData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("name", "The Da Vinci Code");
                values.put("author", "Dan Brown");
                values.put("pages", 454);
                values.put("price", 16.96);
                db.insert("book", null, values);
                values.clear();
                values.put("name", "The Lost Symbol");
                values.put("author", "Dan Brown");
                values.put("pages", 510);
                values.put("price", 19.95);
                db.insert("book", null, values);
            }
        });
        Button replaceData = (Button)findViewById(R.id.replace_data);
        replaceData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.beginTransaction();
                try{
                    db.delete("book", null, null);
                    if (true){
                        throw new NullPointerException();
                    }
                    ContentValues values = new ContentValues();
                    values.put("name", "Game of Thrones");
                    values.put("author", "George Martin");
                    values.put("pages", 720);
                    values.put("price", 20.85);
                    db.insert("Book", null, values);
                    db.setTransactionSuccessful();
                } catch (Exception e){
                    e.printStackTrace();
                } finally {
                    db.endTransaction();
                }
            }
        });

        Button startNotify = (Button)findViewById(R.id.startNotification);
        startNotify.setOnClickListener(this);
        Button startPhoto = (Button)findViewById(R.id.startPhoto);
        startPhoto.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        String inputText = edit1.getText().toString();
        save(inputText);
    }

    public void save(String inputText){
        FileOutputStream out = null;
        BufferedWriter writer = null;
        try{
            out = openFileOutput("data1", Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(inputText);
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            try{
                if (writer != null){
                    writer.close();
                }
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public String load(){
        FileInputStream in = null;
        BufferedReader reader = null;
        StringBuilder content = new StringBuilder();
        try{
            in = openFileInput("data1");
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null){
                content.append(line);
            }
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            if (reader != null){
                try{
                    reader.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        return content.toString();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.startNotification:
                Log.d("StartNotification", "Send a notice");
                Intent intent = new Intent(this, com.jcc.broadcastbestpractice.Notification.class);
                PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                android.app.Notification notification = new android.app.Notification.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(pi)
                        .setContentText("This is ticker text")
                        .setWhen(System.currentTimeMillis())
                        .build();
                manager.notify(2, notification);
                break;
            case R.id.startPhoto:
                Log.d("startPhoto", "Send a startPhoto");
                Intent intent1 = new Intent(this, ChoosePicTest.class);
                startActivity(intent1);
            default:
                break;
        }
    }
}
