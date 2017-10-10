package com.sprout.clipcon.server;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.sprout.clipcon.model.Message;
import com.sprout.clipcon.transfer.RetrofitCommonRequest;
import com.sprout.clipcon.transfer.RetrofitUploadData;

import java.net.MalformedURLException;

/**
 * Created by delf on 17-05-06.
 */

public class BackgroundTaskHandler extends AsyncTask<String, Void, String> {
    private final static int TYPE = 0;
    private final static int GROUP_PK = 1;

    private Bitmap sendBitmapImage;
    private String sendText;
    private String filePath;

    private RetrofitCommonRequest requester;

    private GcBackgroundCallback backgroundCallback;
    public interface GcBackgroundCallback {
        void onSuccess(Message result);
    }
    public BackgroundTaskHandler() {requester = new RetrofitCommonRequest(backgroundCallback);}
    public BackgroundTaskHandler setBackgroundCallback(GcBackgroundCallback backgroundCallback) {
        requester = new RetrofitCommonRequest(backgroundCallback);
        this.backgroundCallback = backgroundCallback;
        return this;
    }

    public BackgroundTaskHandler setUploadCallback(RetrofitUploadData.UploadCallback uploadCallback) {
        this.backgroundCallback = backgroundCallback;
        return this;
    }



    @Override
    protected String doInBackground(String... msg) {
        switch (msg[TYPE]) {
            case Message.REQUEST_CREATE_GROUP:
                Log.d("delf", "send group create request to server.");
                requester.commonRequest(new Message().setType(Message.REQUEST_CREATE_GROUP).toString());
                break;

            case Message.REQUEST_JOIN_GROUP:
                Log.d("delf", "send group join request to server. group pk is \"" + msg[GROUP_PK] + "\"");
                Message requestMessage = new Message().setType(Message.REQUEST_JOIN_GROUP);
                requestMessage.add(Message.GROUP_PK, msg[GROUP_PK]);
                requester.commonRequest(requestMessage.toString());
                break;

            case Message.UPLOAD:
                Log.d("BackgroundTaskHandler", "send upload request to server");
                switch (msg[1]) {
                    case "text":
                        MessageHandler.getUploader().uploadStringData(sendText);
                        break;
                    case "image":
                        MessageHandler.getUploader().uploadImageData(sendBitmapImage);
                        // Endpoint.getUploader().uploadImageData(sendBitmapImage);
                        break;
                    case "file":
                        MessageHandler.getUploader().uploadMultipartData(filePath);
                        // Endpoint.getUploader().uploadMultipartData(filePath);
                        break;
                }
                break;
            case Message.DOWNLOAD:
                try {
                    MessageHandler.getDownloader().requestDataDownload(msg[1]); // for test
                } catch (MalformedURLException e) {
                    Log.e("delf", "Error at sending download request");
                }
                break;

            case Message.REQUEST_EXIT_GROUP:
                Log.d("BackgroundTaskHandler", "Send exit request to server");
                Message message = new Message().setType(Message.REQUEST_EXIT_GROUP);
                message.add(Message.GROUP_PK, msg[1]);
                message.add(Message.NAME, msg[2]);
                requester.commonRequest(message.toString());
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
        // 임시주석
        /*try {
            Endpoint.getInstance().sendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (EncodeException e) {
            e.printStackTrace();
        }*/
    }

    private void setCallBack2() {

    }

    private void setCallBack() {
        // 임시주석
        /*final Endpoint.SecondCallback secondResult = new Endpoint.SecondCallback() {
            @Override
            public void onEndpointResponse(Message responseFromServer) {
                backgroundCallback.onSuccess(responseFromServer); // call in MainActivity
            }
        };
        Endpoint.getInstance().setSecondCallback(secondResult);*/
    }

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

    @Override
    protected void onPostExecute(String s) {
        Log.d("delf", "End AsyncTask.");
    }
}