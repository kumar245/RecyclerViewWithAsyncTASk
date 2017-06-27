package com.kumar.user.recyclerview;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "RecyclerViewExample";
    private List<FeedItem> feedItemList;
    private RecyclerView recyclerView;
    private MyRecyclerViewAdapter myRecyclerViewAdapter;
    private ProgressBar progressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView= (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        String url = "http://stacktips.com/?json=get_category_posts&slug=news&count=30";

        new  DownloadTask().execute(url);

    }

    public class DownloadTask extends AsyncTask<String, Void, Integer>{

        @Override
        protected void onPreExecute() {

           progressBar.setVisibility(View.VISIBLE);

        }

        @Override
        protected Integer doInBackground(String... params) {

            Integer result=0;
            HttpURLConnection urlConnection;

            try {
                URL url=new URL(params[0]);

                urlConnection= (HttpURLConnection) url.openConnection();
                int statuscode=urlConnection.getResponseCode();

                //200 represents OK

                if (statuscode==200){
                    BufferedReader reader =new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                    StringBuilder response=new StringBuilder();
                    String line;
                    while ((line= reader.readLine())!=null){

                        response.append(line);
                    }
                    parseResult(response.toString());
                    result=1;//successful
                }else {
                    result=0;//failed to fetch data
                }


            }  catch (Exception e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {

            progressBar.setVisibility(View.GONE);

            if (result == 1) {
                myRecyclerViewAdapter = new MyRecyclerViewAdapter(MainActivity.this, feedItemList);
                recyclerView.setAdapter(myRecyclerViewAdapter);
                myRecyclerViewAdapter.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(FeedItem item) {
                        Toast.makeText(MainActivity.this, item.getTitle(), Toast.LENGTH_LONG).show();

                    }
                });

            } else {
                Toast.makeText(MainActivity.this, "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void parseResult(String result) {

        try {
            JSONObject response=new JSONObject(result);
            JSONArray posts=response.optJSONArray("posts");
            feedItemList=new ArrayList<>();

            for (int i=0;i<posts.length();i++){
                JSONObject post= posts.optJSONObject(i);
                FeedItem item=new FeedItem();
                item.setTitle(post.optString("title"));
                item.setThumbnail(post.optString("thumbnail"));
                feedItemList.add(item);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
