package com.sample.login.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.linkedin.platform.APIHelper;
import com.linkedin.platform.LISessionManager;
import com.linkedin.platform.errors.LIApiError;
import com.linkedin.platform.listeners.ApiListener;
import com.linkedin.platform.listeners.ApiResponse;
import com.sample.login.MTAppConstants;
import com.sample.login.MTPreferenceManager;
import com.sample.login.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MTDashboardActivity extends MTBaseActivity {
    private static final String host = "api.linkedin.com";
    private static final String topCardUrl = "https://" + host + "/v1/people/~:" +
            "(id,first-name,last-name,public-profile-url,picture-url,email-address,picture-urls::(original))";
    private ProgressDialog progress;
    private String connection, imageUrl;
    private ListView list_view;
    private ListViewAdapter adapter;
    private ArrayList<String> titleValues = new ArrayList<>();
    private ArrayList<String> detailValues = new ArrayList<>();
    private final String TAG = this.getClass().getSimpleName();
    private TextView tvLogout;
    private ImageView profile_picture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        connection = getIntent().getStringExtra(MTAppConstants.CONNECTION);

        progress = new ProgressDialog(this);
        progress.setMessage(getString(R.string.retrieve_data));
        progress.setCanceledOnTouchOutside(false);
        progress.show();

        titleValues = (ArrayList<String>) getIntent().getSerializableExtra(MTAppConstants.TITLE_VALUES);
        detailValues = (ArrayList<String>) getIntent().getSerializableExtra(MTAppConstants.DETAIL_VALUES);
        profile_picture = (ImageView) findViewById(R.id.profile_picture);
        list_view = (ListView) findViewById(R.id.list_view);
        tvLogout = (TextView) findViewById(R.id.text_logout);

        if (connection.equalsIgnoreCase("facebook")) {
            imageUrl = getIntent().getStringExtra(MTAppConstants.IMAGE_URL);
            progress.dismiss();
            setAdapter(titleValues, detailValues, imageUrl);
        } else {
            linkedInApiHelper();
        }

        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (connection.equalsIgnoreCase("facebook")) {
                    LoginManager.getInstance().logOut();
                } else {
                    LISessionManager.getInstance(MTDashboardActivity.this).clearSession();
                }
                MTPreferenceManager.setUserId("", MTDashboardActivity.this);
                Intent intent = new Intent(MTDashboardActivity.this, MTLoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * @param titleValues  in a title on arrayList
     * @param detailValues in a value on arrayList
     * @param imageUrl     in a login user image ur;
     */
    private void setAdapter(ArrayList<String> titleValues, ArrayList<String> detailValues, String imageUrl) {
        Picasso.with(this).load(imageUrl).into(profile_picture);
        if (titleValues != null && detailValues != null && detailValues.size() > 0 && titleValues.size() > 0) {
            adapter = new ListViewAdapter(MTDashboardActivity.this, titleValues, detailValues);
            list_view.setAdapter(adapter);
        }
    }

    /*Once User's can authenticated,
      It make an HTTP GET request to LinkedIn's REST API using the currently authenticated user's credentials.
      If successful, A LinkedIn ApiResponse object containing all of the relevant aspects of the server's response will be returned.
     */
    public void linkedInApiHelper() {
        APIHelper apiHelper = APIHelper.getInstance(getApplicationContext());
        apiHelper.getRequest(MTDashboardActivity.this, topCardUrl, new ApiListener() {
            @Override
            public void onApiSuccess(ApiResponse result) {
                try {
                    setProfile(result.getResponseDataAsJson());
                    progress.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onApiError(LIApiError error) {
                showAlertDialog(getString(R.string.error), getString(R.string.login_error));
            }
        });
    }

    /**
     * Method call to LinedIn login user response
     *
     * @param response from JSONObject type
     */
    public void setProfile(JSONObject response) {
        try {
            if(response!=null) {
                titleValues.clear();
                detailValues.clear();
                if (response.has("id")) {
                    String id = response.getString("id");
                    MTPreferenceManager.setUserId(id, MTDashboardActivity.this);
                }
                if (response.has("firstName")) {
                    String firstName = response.getString("firstName");
                    titleValues.add("First Name");
                    detailValues.add(firstName);
                }
                if (response.has("lastName")) {
                    String lastName = response.getString("lastName");
                    titleValues.add("Last Name");
                    detailValues.add(lastName);
                }
                if (response.has("pictureUrls")) {
                    JSONObject jsonObject = response.getJSONObject("pictureUrls");
                    if (jsonObject.has("values")){
                        JSONArray jsonArray = jsonObject.getJSONArray("values");
                        for (int i = 0; i <jsonArray.length() ; i++) {
                            imageUrl = jsonArray.getString(0);
                        }
                    }
                }
                setAdapter(titleValues, detailValues, imageUrl);
            }
        } catch (Exception exception) {
            Log.e(TAG, "Exception: " + exception.getLocalizedMessage(), exception);
        }
    }

    /**
     * Adapter class
     */
    class ListViewAdapter extends BaseAdapter {
        ArrayList<String> titleValues;
        ArrayList<String> detailValues;
        Activity activity;

        public ListViewAdapter(Activity activity, ArrayList<String> titleValues, ArrayList<String> detailValues) {
            this.activity = activity;
            this.titleValues = titleValues;
            this.detailValues = detailValues;
        }

        @Override
        public int getCount() {
            return titleValues.size();
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            try {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.list_item, parent, false);
                    holder = new ViewHolder();
                    //UI Initialise
                    holder.tvTitle = (TextView) convertView.findViewById(R.id.title_text);
                    holder.tvValue = (TextView) convertView.findViewById(R.id.value_text);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                holder.tvTitle.setText(titleValues.get(position));
                holder.tvValue.setText(detailValues.get(position));
            } catch (Exception exception) {
                Log.e(TAG, "Exception: " + exception.getLocalizedMessage(), exception);
            }
            return convertView;
        }

        /**
         * View holder class for list items
         */
        private class ViewHolder {
            TextView tvTitle, tvValue;
        }
    }

    @Override
    public void onBackPressed() {
        if (connection.equalsIgnoreCase("facebook")) {
            LoginManager.getInstance().logOut();
        } else {
            LISessionManager.getInstance(MTDashboardActivity.this).clearSession();
        }
        super.onBackPressed();
    }
}