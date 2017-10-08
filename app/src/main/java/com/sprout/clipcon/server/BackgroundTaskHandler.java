package com.sprout.clipcon.server;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.sprout.clipcon.model.Message;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.websocket.EncodeException;

/**
 * Created by delf on 17-05-06.
 */

public class BackgroundTaskHandler extends AsyncTask<String, Void, String> {
    private final static int TYPE = 0;
    private final static int GROUP_PK = 1;
    private BackgroundCallback backgroundCallback;

    private Bitmap sendBitmapImage;
    private String sendText;
    private String filePath;
    private File uploadFile; // test

    public BackgroundTaskHandler setSendBitmapImage(Bitmap sendBitmapImage) {
        this.sendBitmapImage = sendBitmapImage;
        return this;
    }

    public BackgroundTaskHandler setSendText(String sendText) {
        this.sendText = sendText;
        return this;
    }

    public BackgroundTaskHandler setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public BackgroundTaskHandler setUploadFile(File uploadFile) {
        this.uploadFile = uploadFile;
        return this;
    }

    public interface BackgroundCallback {
        void onSuccess(JSONObject result);
    }

    public BackgroundTaskHandler() {
    }

    public BackgroundTaskHandler(BackgroundCallback callback) {
        this.backgroundCallback = callback;
    }

    @Override
    protected String doInBackground(String... msg) {
        switch (msg[TYPE]) {
            case Message.CONNECT:
                Log.d("BackgroundTaskHandler", "Connecting server...");
                Endpoint.getInstance();
                break;

            case Message.REQUEST_CREATE_GROUP:
                Log.d("BackgroundTaskHandler", "send group create request to server."); // XXX: caution!

                Message message = new Message().setType(Message.REQUEST_CREATE_GROUP);

                setCallBack();
                sendMessage(
                        new Message().setType(Message.REQUEST_CREATE_GROUP)
                );
                break;

            case Message.REQUEST_JOIN_GROUP:
                setCallBack();
                Log.d("BackgroundTaskHandler", "send group join request to server. group pk is \"" + msg[GROUP_PK] + "\"");
                sendMessage(
                        new Message().setType(Message.REQUEST_JOIN_GROUP)
                                .add(Message.GROUP_PK, msg[GROUP_PK]) // msg[1]: group key
                );
                break;

            case Message.UPLOAD:
                Log.d("BackgroundTaskHandler", "send upload request to server");
                switch (msg[1]) {
                    case "text":
                        Endpoint.getUploader().uploadStringData(sendText);
                        break;
                    case "image":
                        Endpoint.getUploader().uploadImageData(sendBitmapImage);
                        break;
                    case "file":
                        Endpoint.getUploader().uploadMultipartData(filePath);
                        break;
                }
                break;

            case Message.DOWNLOAD:
                Log.d("BackgroundTaskHandler", "Send download request to server. pk is " + Endpoint.lastContentsPK);
                try {
                    Endpoint.getDownloader().requestDataDownload(msg[1]); // for test
                } catch (MalformedURLException e) {
                    Log.e("delf", "Error at sending download request");
                    // e.printStackTrace();
                }
                break;

            case Message.REQUEST_EXIT_GROUP:
                Log.d("BackgroundTaskHandler", "Send exit request to server");
                sendMessage(
                        new Message().setType(Message.REQUEST_EXIT_GROUP)
                );
                break;

            case Message.REQUEST_CHANGE_NAME:
                sendMessage(
                        new Message().setType(Message.REQUEST_CHANGE_NAME)
                                .add(Message.CHANGE_NAME, msg[1])
                );
                break;

            default:
                Log.d("BackgroundTaskHandler", "Do nothing in doInBackground()");
                break;
        }
        return null;
    }

    private void sendMessage(Message message) {
        try {
            Endpoint.getInstance().sendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (EncodeException e) {
            e.printStackTrace();
        }
    }

    private void setCallBack() {
        final Endpoint.SecondCallback secondResult = new Endpoint.SecondCallback() {
            @Override
            // method name recommendation: onResponseAtEndpoint() // tmp
            public void onEndpointResponse(JSONObject responseFromServer) {
                backgroundCallback.onSuccess(responseFromServer); // call in MainActivity
            }
        };
        Endpoint.getInstance().setSecondCallback(secondResult);
    }

    @Override
    protected void onPostExecute(String s) {
        Log.d("delf", "End AsyncTask.");
    }
}