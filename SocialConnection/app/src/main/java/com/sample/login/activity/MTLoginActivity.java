package com.sample.login.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.linkedin.platform.LISession;
import com.linkedin.platform.LISessionManager;
import com.linkedin.platform.errors.LIAuthError;
import com.linkedin.platform.listeners.AuthListener;
import com.linkedin.platform.utils.Scope;
import com.sample.login.MTAppConstants;
import com.sample.login.MTSharedPreferenceManager;
import com.sample.login.MTUtilities;
import com.sample.login.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class MTLoginActivity extends MTBaseActivity {
    private final String TAG = this.getClass().getSimpleName();
    private CallbackManager callbackManager;
    private Button linkedInBtn, facebookLogin;
    private ArrayList<String> titleValues = new ArrayList<>();
    private ArrayList<String> detailValues = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FacebookSdk.sdkInitialize(this.getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        linkedInBtn = (Button) findViewById(R.id.btn_linked_in);
        facebookLogin = (Button) findViewById(R.id.btn_facebook);

        facebookLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MTUtilities.checkNetworkConnection(MTLoginActivity.this)) {
                    if (isLoggedIn()) {
                        LoginManager.getInstance().logOut();
                    }
                    fbConnection();
                } else {
                    showAlertDialog(getString(R.string.info), getString(R.string.no_internet_connection));
                }
            }
        });

        linkedInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MTUtilities.checkNetworkConnection(MTLoginActivity.this)) {
                    linkedInConnection();
                } else {
                    showAlertDialog(getString(R.string.info), getString(R.string.no_internet_connection));
                }
            }
        });
    }

    /**
     * Method to valid the login state
     *
     * @return
     */
    public boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }

    private void linkedInConnection() {
        LISessionManager.getInstance(getApplicationContext()).init(MTLoginActivity.this, buildScope(), new AuthListener() {
            @Override
            public void onAuthSuccess() {
                titleValues.clear();
                detailValues.clear();
                Intent intent = new Intent(MTLoginActivity.this, MTDashboardActivity.class);
                intent.putExtra(MTAppConstants.IMAGE_URL, "");
                intent.putExtra(MTAppConstants.CONNECTION, "linkedIn");
                intent.putExtra(MTAppConstants.TITLE_VALUES, titleValues);
                intent.putExtra(MTAppConstants.DETAIL_VALUES, detailValues);
                startActivity(intent);
            }

            @Override
            public void onAuthError(LIAuthError error) {
                setUpdateState();
                showAlertDialog(getString(R.string.error), getString(R.string.login_error));
            }
        }, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        LISessionManager.getInstance(getApplicationContext()).onActivityResult(this, requestCode, resultCode, data);
    }

    private static Scope buildScope() {
        return Scope.build(Scope.R_BASICPROFILE, Scope.W_SHARE);
    }

    private void setUpdateState() {
        LISessionManager sessionManager = LISessionManager.getInstance(getApplicationContext());
        LISession session = sessionManager.getSession();
        boolean accessTokenValid = session.isValid();
    }

    // facebook action
    private void fbConnection() {
        try {
            // Set permissions
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("user_birthday", "email", "user_photos", "public_profile"));
            LoginManager.getInstance().registerCallback(callbackManager,
                    new FacebookCallback<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult loginResult) {
                            GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                                    new GraphRequest.GraphJSONObjectCallback() {
                                        @Override
                                        public void onCompleted(final JSONObject object, GraphResponse response) {
                                            System.out.println("JSON Result" + object);
                                            if (object != null) {
                                                Log.i("Logged In 2", "Logged in 2 ...");
                                                titleValues.clear();
                                                detailValues.clear();
                                                try {
                                                    String id = "";
                                                    if (object.has("id")) {
                                                        id = object.getString("id");
                                                        MTSharedPreferenceManager.setUserId(id, MTLoginActivity.this);
                                                    }
                                                    String first_name = "";
                                                    if (object.has("first_name")) {
                                                        first_name = object.getString("first_name");
                                                        titleValues.add("First name");
                                                        detailValues.add(first_name);
                                                    }
                                                    String last_name = "";
                                                    if (object.has("last_name")) {
                                                        last_name = object.getString("last_name");
                                                        titleValues.add("Last name");
                                                        detailValues.add(last_name);
                                                    }
                                                    String birthday = "";
                                                    if (object.has("birthday")) {
                                                        birthday = object.getString("birthday");
                                                    }
                                                    String email = "";
                                                    if (object.has("email")) {
                                                        email = object.getString("email");
                                                        titleValues.add("Email");
                                                        detailValues.add(email);
                                                    }
                                                    //request facebook user photo
                                                    String imageUrl = "https://graph.facebook.com/v2.1/" + object.getString("id") + "/picture?height=400&width=400";
                                                    imageUrl.replace("https", "http");

                                                    Intent intent = new Intent(MTLoginActivity.this, MTDashboardActivity.class);
                                                    intent.putExtra(MTAppConstants.IMAGE_URL, imageUrl);
                                                    intent.putExtra(MTAppConstants.CONNECTION, "facebook");
                                                    intent.putExtra(MTAppConstants.TITLE_VALUES, titleValues);
                                                    intent.putExtra(MTAppConstants.DETAIL_VALUES, detailValues);
                                                    startActivity(intent);
                                                } catch (Exception e) {
                                                    Log.e(TAG, "Error" + e.getLocalizedMessage());
                                                }
                                            } else {
                                                Log.d(TAG, "User problem");
                                            }
                                        }
                                    });
                            Bundle parameters = new Bundle();
                            parameters.putString("fields", "id,first_name,last_name,email,birthday,picture,gender");
                            request.setParameters(parameters);
                            request.executeAsync();
                        }

                        @Override
                        public void onCancel() {
                            Log.d(TAG, "On cancel");
                            showAlertDialog(getString(R.string.error), getString(R.string.login_error));
                        }

                        @Override
                        public void onError(FacebookException e) {
                            Log.d(TAG, e.toString());
                            showAlertDialog(getString(R.string.error), getString(R.string.login_error));
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error" + e.getLocalizedMessage());
            showAlertDialog(getString(R.string.error), getString(R.string.login_error));
        }
    }
}