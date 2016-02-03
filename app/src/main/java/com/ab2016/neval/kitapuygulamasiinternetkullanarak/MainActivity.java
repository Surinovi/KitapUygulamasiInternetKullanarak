package com.ab2016.neval.kitapuygulamasiinternetkullanarak;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends Activity {

    private static String url = "http://ctis.bilkent.edu.tr/ctis487/jsonBook/books.json";

    // JSON Node names
    public static final String TAG_BOOKS = "books";

    public static final String TAG_ROWID = "_id";
    public static final String TAG_NAME = "name";
    public static final String TAG_AUT = "author";
    public static final String TAG_YEAR = "year";
    public static final String TAG_IMG = "img";

    private JSONArray allBooksJSON = null;

    private ListView lvNewBooks;

    private ArrayList<String> names;

    // HashMap for ListView
    ArrayList<HashMap<String, String>> bookList;
    private DBAdapter db;

    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        bookList = new ArrayList<HashMap<String, String>>();

        lvNewBooks = (ListView) findViewById(R.id.lvNewBooks);

        // If the database already exist no need to copy it from the assets
        // folder
        try {
            String destPath = "/data/data/" + getPackageName()
                    + "/databases/kitap";
            File file = new File(destPath);
            File path = new File("/data/data/" + getPackageName()
                    + "/databases/");
            if (!file.exists()) {
                path.mkdirs();
                CopyDB(getBaseContext().getAssets().open("kitap"),
                        new FileOutputStream(destPath));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        db = new DBAdapter(this);

        // Calling async task to get json
        new GetBooks().execute();

    }

    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.btnSave:
                insertJSONDataToDatabase();
                break;
            case R.id.btnContinue:
                intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                break;

        }
    }

    // Async task class to get json by making HTTP call
    private class GetBooks extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandler mServiceHandler = new ServiceHandler();

            // Making a request to url and getting response
            String jsonStr = mServiceHandler.makeServiceCall(url,
                    ServiceHandler.GET);

            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {

                    names = new ArrayList<String>();
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    allBooksJSON = jsonObj.getJSONArray(TAG_BOOKS);

                    // looping through All Contacts
                    for (int i = 0; i < allBooksJSON.length(); i++) {
                        JSONObject c = allBooksJSON.getJSONObject(i);

                        String name = c.getString(TAG_NAME);
                        names.add(name);
                        String aut = c.getString(TAG_AUT);
                        String year = c.getString(TAG_YEAR);
                        String imgname = c.getString(TAG_IMG);

                        // tmp hashmap for single contact
                        HashMap<String, String> oneRecord = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        oneRecord.put(TAG_NAME, name);
                        oneRecord.put(TAG_AUT, aut);
                        oneRecord.put(TAG_YEAR, year);
                        oneRecord.put(TAG_IMG, imgname);

                        // adding contact to contact list
                        bookList.add(oneRecord);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            ArrayAdapter<String> mListAdapter = new ArrayAdapter<String>(MainActivity.this,
                    android.R.layout.simple_list_item_1, names);

            lvNewBooks.setAdapter(mListAdapter);
        }

    }

    public void insertJSONDataToDatabase() {
        for (int i = 0; i < bookList.size(); i++) {
            HashMap<String, String> arecipe = bookList.get(i);
            String name = arecipe.get(TAG_NAME);
            Log.d(TAG_NAME, name);

            String aut = arecipe.get(TAG_AUT);
            Log.d(TAG_AUT, aut);

            String year = arecipe.get(TAG_YEAR);
            Log.d(TAG_YEAR, year);

            String imagename = arecipe.get(TAG_IMG);
            Log.d(TAG_IMG, imagename);

            db.open();
            db.insertBook(name, aut, year, imagename);
            db.close();
        }
        Toast.makeText(getBaseContext(), "Saved", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }

    public void CopyDB(InputStream inputStream, OutputStream outputStream)
            throws IOException {
        // Copy 1K bytes at a time
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        inputStream.close();
        outputStream.close();
    }
}
