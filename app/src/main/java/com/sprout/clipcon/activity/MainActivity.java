package com.sprout.clipcon.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v13.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.messaging.FirebaseMessaging;
import com.sprout.clipcon.R;
import com.sprout.clipcon.model.Message;
import com.sprout.clipcon.server.BackgroundTaskHandler;
import com.sprout.clipcon.server.MessageHandler;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 1000;
    private String groupKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button createBtn = (Button) findViewById(R.id.main_create);
        Button joinBtn = (Button) findViewById(R.id.main_join);

        // create group
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BackgroundTaskHandler.GcBackgroundCallback result = new BackgroundTaskHandler.GcBackgroundCallback() {
                    @Override
                    public void onSuccess(Message response) {
                        try {
                            MessageHandler.getInstance().setUser(response);
                            startGroupActivity(response.getJson());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                MessageHandler.getInstance().setBackgroundCallback(result).request(Message.REQUEST_CREATE_GROUP, null);
                // new BackgroundTaskHandler(result).execute(Message.REQUEST_CREATE_GROUP);
            }
        });
        // join group
        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showJoinDialog();
            }
        });

        writeExternalStoragePrmissionCheck();
    }

    public void showJoinDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.inputKey)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME)
                .positiveText(R.string.joinEn)
                .input(R.string.empty, R.string.empty, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, final CharSequence inputGroupKey) {
                        final BackgroundTaskHandler.GcBackgroundCallback result = new BackgroundTaskHandler.GcBackgroundCallback() {
                            @Override
                            public void onSuccess(Message response) {
                                try {
                                    if (response.get(Message.RESULT).equals(Message.CONFIRM)) {
                                        MessageHandler.getInstance().setUser(response);
                                        response.add(Message.GROUP_NAME, inputGroupKey.toString());
                                        startGroupActivity(response.getJson());
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
                        MessageHandler.getInstance().setBackgroundCallback(result).request(Message.REQUEST_JOIN_GROUP, inputGroupKey.toString());
                        // new BackgroundTaskHandler(result).execute(Message.REQUEST_JOIN_GROUP, inputGroupKey.toString());
                    }
                }).show();
    }

    @Override
    public void onDestroy() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(groupKey);
        MessageHandler.getInstance().request(Message.REQUEST_EXIT_GROUP, null);
        super.onDestroy();
    }

    private void startGroupActivity(JSONObject response) throws JSONException {
        Intent intent = new Intent(MainActivity.this, GroupActivity.class);
        groupKey = response.get(Message.GROUP_PK).toString();
        FirebaseMessaging.getInstance().subscribeToTopic(groupKey);
        intent.putExtra("response", response.toString()); // send response to GroupActivity
        startActivity(intent);
    }

    public void alertDialog() {
        new MaterialDialog.Builder(this)
                .content(R.string.checkKey)
                .positiveText(R.string.confirm)
                .show();
    }

    public void writeExternalStoragePrmissionCheck() {
        /* 사용자의 OS 버전이 마시멜로우 이상인지 체크한다. */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            /* 사용자 단말기의 권한 중 "저장소 쓰기" 권한이 허용되어 있는지 체크한다.
             */
            int permissionResult = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            /* WRITE_EXTERNAL_STORAGE의 권한이 없을 때 */
            if (permissionResult == PackageManager.PERMISSION_DENIED) {
                /* 사용자가 WRITE_EXTERNAL_STORAGE의 권한을 한번이라도 거부한 적이 있는 지 조사한다.
                 * 거부한 이력이 한번이라도 있다면, true를 리턴한다.
                 */
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_STORAGE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    ActivityCompat.finishAffinity(this);
                    System.exit(0);
                }
                return;
            }
        }
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }
}
