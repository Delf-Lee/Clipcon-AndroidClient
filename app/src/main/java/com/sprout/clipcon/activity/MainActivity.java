package com.sprout.clipcon.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.messaging.FirebaseMessaging;
import com.sprout.clipcon.R;
import com.sprout.clipcon.model.Message;
import com.sprout.clipcon.server.EndpointInBackGround;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseMessaging.getInstance().subscribeToTopic("notice");

        setContentView(R.layout.activity_main);

        Button createBtn = (Button) findViewById(R.id.main_create);
        Button joinBtn = (Button) findViewById(R.id.main_join);

        new EndpointInBackGround().execute(Message.CONNECT); // connect

        // create group
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("delf", "[SYSTEM] create group button clicked");
                final EndpointInBackGround.BackgroundCallback result = new EndpointInBackGround.BackgroundCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        try {
                            startGroupActivity(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                new EndpointInBackGround(result).execute(Message.REQUEST_CREATE_GROUP);
            }
        });
        // join group
        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showJoinDialog();
            }
        });
    }

    public void showJoinDialog() {
        Log.d("delf", "join group button clicked");
        new MaterialDialog.Builder(this)
                .title(R.string.inputKey)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME)
                .positiveText(R.string.joinEn)
                .input(R.string.empty, R.string.empty, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, final CharSequence inputGroupKey) {
                        final EndpointInBackGround.BackgroundCallback result = new EndpointInBackGround.BackgroundCallback() {
                            @Override
                            public void onSuccess(JSONObject response) {
                                try {
                                    if (response.get(Message.RESULT).equals(Message.CONFIRM)) {
                                        response.put(Message.GROUP_NAME, inputGroupKey.toString());
                                        startGroupActivity(response);
                                    } else { // reject
                                        MainActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                alertDialog();
                                            }
                                        });
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        new EndpointInBackGround(result).execute(Message.REQUEST_JOIN_GROUP, inputGroupKey.toString());
                    }
                }).show();
    }

    private void startGroupActivity(JSONObject response) throws JSONException {
        Intent intent = new Intent(MainActivity.this, GroupActivity.class);

        intent.putExtra("response", response.toString()); // send response to GroupActivity
        startActivity(intent);
    }

    public void alertDialog() {
        new MaterialDialog.Builder(this)
                .content(R.string.checkKey)
                .positiveText(R.string.confirm)
                .show();
    }
}
