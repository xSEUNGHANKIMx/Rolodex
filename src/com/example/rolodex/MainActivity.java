package com.example.rolodex;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static String mUrl = "https://s3-us-west-2.amazonaws.com/udacity-mobile-interview/CardData.json";
    private static final String LASTNAME = "lastName";
    private static final String FIRSTNAME = "firstName";
    private static final String EMAIL = "email";
    private static final String COMPANY = "company";
    private static final String STARTDATE = "startDate";
    private static final String BIO = "bio";
    private static final String AVATAR = "avatar";

    ArrayList<HashMap<String, String>> mJsonList = new ArrayList<HashMap<String, String>>();
    ListView mListView;
    LayoutInflater mInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.listview);
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        new ProgressTask(MainActivity.this).execute();
    }

    private Bitmap downloadBitmap(String url) {
        HttpURLConnection urlConnection = null;
        try {
            URL uri = new URL(url);
            urlConnection = (HttpURLConnection) uri.openConnection();
            int statusCode = urlConnection.getResponseCode();
            if (statusCode != HttpStatus.SC_OK) {
                return null;
            }

            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            }
        } catch (Exception e) {
            urlConnection.disconnect();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }

    private class ProgressTask extends AsyncTask<String, Void, Boolean> {
        private ProgressDialog dialog;

        public ProgressTask(Activity activity) {
            context = activity;
            dialog = new ProgressDialog(context);
        }

        private Context context;

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Progress start");
            this.dialog.show();
        }

        @Override
        protected Boolean doInBackground(final String... args) {
            JSONParser jParser = new JSONParser();
            JSONArray json = jParser.getJSONFromUrl(mUrl);
            for (int i = 0; i < json.length(); i++) {
                try {
                    JSONObject c = json.getJSONObject(i);
                    String lastName = c.getString(LASTNAME);
                    String firstName = c.getString(FIRSTNAME);
                    String email = c.getString(EMAIL);
                    String startDate = c.getString(STARTDATE);
                    String company = c.getString(COMPANY);
                    String bio = c.getString(BIO);
                    String avatar = c.getString(AVATAR);

                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put(LASTNAME, lastName);
                    map.put(FIRSTNAME, firstName);
                    map.put(EMAIL, email);
                    map.put(STARTDATE, startDate);
                    map.put(COMPANY, company);
                    map.put(BIO, bio);
                    map.put(AVATAR, avatar);
                    mJsonList.add(map);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            RolodexAdapter myAdapter = new RolodexAdapter();
            mListView.setAdapter(myAdapter);
        }

    }

    class RolodexAdapter extends BaseAdapter {

        RolodexAdapter() {

        }

        @Override
        public int getCount() {
            return mJsonList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public class Holder {
            TextView tvLName;
            TextView tvFName;
            TextView tvEmail;
            TextView tvCompany;
            TextView tvSDate;
            TextView tvBio;
            ImageView tvAvatar;
        }

        @SuppressLint({ "ViewHolder", "InflateParams" })
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder = new Holder();
            View rowView;
            rowView = mInflater.inflate(R.layout.list_item, null);
            holder.tvLName = (TextView) rowView.findViewById(R.id.tvLastName);
            holder.tvFName = (TextView) rowView.findViewById(R.id.tvFirstName);
            holder.tvEmail = (TextView) rowView.findViewById(R.id.tvEmail);
            holder.tvCompany = (TextView) rowView.findViewById(R.id.tvCompany);
            holder.tvSDate = (TextView) rowView.findViewById(R.id.tvStartDate);
            holder.tvBio = (TextView) rowView.findViewById(R.id.tvBio);
            holder.tvAvatar = (ImageView) rowView.findViewById(R.id.ivAvatar);

            holder.tvLName.setText(mJsonList.get(position).get(LASTNAME));
            holder.tvFName.setText(mJsonList.get(position).get(FIRSTNAME));
            holder.tvEmail.setText(mJsonList.get(position).get(EMAIL));
            holder.tvCompany.setText(mJsonList.get(position).get(COMPANY));
            holder.tvSDate.setText(mJsonList.get(position).get(STARTDATE));
            holder.tvBio.setText(mJsonList.get(position).get(BIO));
            if (holder.tvAvatar != null) {
                new ImageDownloaderTask(holder.tvAvatar).execute(mJsonList.get(position).get(AVATAR));
            }
            return rowView;
        }

    }

    class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;

        public ImageDownloaderTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            return downloadBitmap(params[0]);
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    } else {
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));
                    }
                }
            }
        }
    }


}
