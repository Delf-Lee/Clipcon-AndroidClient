package com.sprout.clipcon.server;

import android.graphics.Bitmap;
import android.util.Log;

import com.sprout.clipcon.model.Group;
import com.sprout.clipcon.model.Message;
import com.sprout.clipcon.model.User;
import com.sprout.clipcon.transfer.RetrofitDownloadData;
import com.sprout.clipcon.transfer.RetrofitUploadData;

import org.json.JSONException;

/**
 * Created by delf on 17-10-09.
 */

public class MessageHandler {

    private static User user;
    private static MessageHandler uniqueInstance;
    private static RetrofitUploadData uniqueUploader;
    private static RetrofitDownloadData uniqueDownloader;

    private String uploadText;
    private Bitmap uploadBitmapImage;
    private String uploadFilePath;

    private BackgroundTaskHandler.BackgroundCallback backgroundCallback;
    private RetrofitUploadData.UploadCallback uploadCallback;



    private MessageHandler() {
    }
    public static MessageHandler getInstance() {
        if(uniqueInstance == null) {
            uniqueInstance = new MessageHandler();
        }
        return uniqueInstance;
    }

    public static RetrofitUploadData getUploader() {
        if (uniqueUploader == null) {
            Log.d("delf", "[Endpoint] uploader is create. the name is " + user.getName() + " and group key is " + user.getGroup().getPrimaryKey());
            uniqueUploader = new RetrofitUploadData(user.getName(), user.getGroup().getPrimaryKey());
        }
        return uniqueUploader;
    }

    public static RetrofitDownloadData getDownloader() {
       /* if (uniqueDownloader == null) {
            uniqueDownloader = new RetrofitDownloadData(user.getName(), user.getGroup().getPrimaryKey());
        }*/
        return uniqueDownloader;
    }


    private ActivityCallback activityCallback;
    public interface ActivityCallback {
        void onSucess(Message message);
    }

    public void request(String req, String msg) {
        Log.d("delf", "req: " + req);
        Log.d("delf", "msg: " + msg);

        switch (req) {
            case Message.REQUEST_CREATE_GROUP:
                new BackgroundTaskHandler().setBackgroundCallback(backgroundCallback).execute(req);
                break;
            case Message.REQUEST_JOIN_GROUP:
                new BackgroundTaskHandler().setBackgroundCallback(backgroundCallback).execute(req, msg);
                break;
            case Message.UPLOAD:
                switch (msg) {
                    case "text":
                    new BackgroundTaskHandler().setUploadCallback(uploadCallback).setSendText(uploadText).execute(req, msg);
                        break;
                    case "image":
                        new BackgroundTaskHandler().setUploadCallback(uploadCallback).setSendBitmapImage(uploadBitmapImage).execute(req, msg);
                        break;
                    case "file":
                        new BackgroundTaskHandler().setUploadCallback(uploadCallback).setFilePath(uploadFilePath).execute(req, msg);
                        break;
                }
                break;
        }
    }

    public MessageHandler setSendBitmap(Bitmap sendBitmapImage) {
        this.uploadBitmapImage = sendBitmapImage;
        return this;
    }

    public MessageHandler setSendText(String sendText) {
        this.uploadText = sendText;
        return this;
    }

    public MessageHandler setFilePath(String filePath) {
        this.uploadFilePath = filePath;
        return this;
    }

    public MessageHandler setBackgroundCallback(BackgroundTaskHandler.BackgroundCallback backgroundCallback) {
        this.backgroundCallback = backgroundCallback;
        return this;
    }

    public MessageHandler setUploadCallback(RetrofitUploadData.UploadCallback uploadCallback) {
        this.uploadCallback = uploadCallback;
        return this;
    }

    public void setUser(Message message) {
        try {
            user = new User(message.get(Message.NAME), new Group(message.get(Message.GROUP_PK)));
        } catch (JSONException e) {
            Log.e("delf", "JSONException " + e.getMessage() + " " + this.getClass().toString());
        }
    }
}
