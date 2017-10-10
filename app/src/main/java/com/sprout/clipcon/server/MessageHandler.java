package com.sprout.clipcon.server;

import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import com.sprout.clipcon.model.Contents;
import com.sprout.clipcon.model.Group;
import com.sprout.clipcon.model.Message;
import com.sprout.clipcon.model.MessageParser;
import com.sprout.clipcon.model.User;
import com.sprout.clipcon.transfer.RetrofitDownloadData;
import com.sprout.clipcon.transfer.RetrofitUploadData;

import org.json.JSONException;

/**
 * Created by delf on 17-10-09.
 */

public class MessageHandler {

    private static User user;
    private  String lastContentsPK;

    private static MessageHandler uniqueInstance;
    private static RetrofitUploadData uniqueUploader;
    private static RetrofitDownloadData uniqueDownloader;

    private String uploadText;
    private Bitmap uploadBitmapImage;
    private String uploadFilePath;

    private BackgroundTaskHandler.GcBackgroundCallback backgroundCallback;
    private RetrofitUploadData.UploadCallback uploadCallback;

    private ContentsCallback contentsCallback;
    private ParticipantCallback participantCallback;
    private NameChangeCallback nameChangeCallback;

    private Handler handler;

    public interface ContentsCallback {
        void onContentsUpdate(Contents contents);
    }
    public interface ParticipantCallback {
        void onParticipantStatus(String newMember);
    }
    public interface NameChangeCallback {
        void onSuccess(String origin, String changed);
    }

    public void setContentsCallback(ContentsCallback callback) {
        contentsCallback = callback;
    }
    public void setParticipantCallback(ParticipantCallback callback) {
        participantCallback = callback;
    }
    public void setNameChangeCallback(NameChangeCallback callback) {
        nameChangeCallback = callback;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    private MessageHandler() {
    }
    public static MessageHandler getInstance() {
        if(uniqueInstance == null) {
            uniqueInstance = new MessageHandler();
        }
        return uniqueInstance;
    }

    public User getUser(){
        return user;
    }

    public static RetrofitUploadData getUploader() {
        if (uniqueUploader == null) {
            Log.d("delf", "[Endpoint] uploader is create. the name is " + user.getName() + " and group key is " + user.getGroup().getPrimaryKey());
            uniqueUploader = new RetrofitUploadData(user.getName(), user.getGroup().getPrimaryKey());
        }
        uniqueUploader.setUserName(user.getName());
        uniqueUploader.setGroupPK(user.getGroup().getPrimaryKey());
        return uniqueUploader;
    }

    public void setUser(User user){
        this.user = user;
    }

    public static RetrofitDownloadData getDownloader() {
        if (uniqueDownloader == null) {
            uniqueDownloader = new RetrofitDownloadData(user.getName(), user.getGroup().getPrimaryKey());
        }
        uniqueDownloader.setUserName(user.getName());
        uniqueDownloader.setGroupPK(user.getGroup().getPrimaryKey());
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
            case Message.REQUEST_EXIT_GROUP:
                new BackgroundTaskHandler().execute(req, user.getGroup().getPrimaryKey(), user.getName());

                break;
        }
    }

    public synchronized void handleMessage(Message message) throws JSONException {
        switch (message.getType()) {
            case Message.NOTI_UPLOAD_DATA:
                lastContentsPK  = message.get("contentsPKName");
                Contents contents = MessageParser.getContentsbyMessage(message);
                user.getGroup().addContents(contents);

                contentsCallback.onContentsUpdate(contents);
                if(!message.get("uploadUserName").equals(user.getName())) {
                    handler.sendEmptyMessage(0);
                   // handler.notify();
                }
                break;

            case Message.NOTI_ADD_PARTICIPANT:
                Log.d("delf", "username: " + user.getName());
                Log.d("delf", "parti: " + message.get(Message.PARTICIPANT_NAME));
                if(!message.get(Message.PARTICIPANT_NAME).equals(user.getName())) {
                    participantCallback.onParticipantStatus(message.get(Message.PARTICIPANT_NAME));
                }
                break;
            case Message.NOTI_EXIT_PARTICIPANT:
                Log.d("delf", "username: " + user.getName());
                Log.d("delf", "parti: " + message.get(Message.PARTICIPANT_NAME));
                if(!message.get(Message.PARTICIPANT_NAME).equals(user.getName())) {
                    participantCallback.onParticipantStatus(message.get(Message.PARTICIPANT_NAME));
                }
                break;

            case Message.NOTI_CHANGE_NAME:
                nameChangeCallback.onSuccess(message.get(Message.NAME), message.get(Message.CHANGE_NAME));
                break;

            default:
                Log.e("MessageHandler","type error");
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

    public MessageHandler setBackgroundCallback(BackgroundTaskHandler.GcBackgroundCallback backgroundCallback) {
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
