package com.sprout.clipcon.server;

import android.os.Handler;
import android.util.Log;

import com.sprout.clipcon.model.Contents;
import com.sprout.clipcon.model.Group;
import com.sprout.clipcon.model.Message;
import com.sprout.clipcon.model.MessageDecoder;
import com.sprout.clipcon.model.MessageEncoder;
import com.sprout.clipcon.model.MessageParser;
import com.sprout.clipcon.model.User;
import com.sprout.clipcon.transfer.RetrofitDownloadData;
import com.sprout.clipcon.transfer.RetrofitUploadData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

@ClientEndpoint(decoders = {MessageDecoder.class}, encoders = {MessageEncoder.class})
public class Endpoint {
    private String uri = "ws://" + ServerInfo.SERVER_URL_PART + "/ServerEndpoint";
    /* singleton instance */
    private static Endpoint uniqueEndpoint;

    private Session session;
    private static User user;
    public static String lastContentsPK;

    private static RetrofitUploadData uniqueUploader;
    private static RetrofitDownloadData uniqueDownloader;

    /* callback interface */
    private SecondCallback secondCallback; // callback to MainActivity
    private ParticipantCallback participantCallback; // callback to MainActivity-InfoFragment
    private NameChangeCallback nameChangeCallback; // callback to MainActivity-InfoFragment
    private ContentsCallback contentsCallback; // callback to MainActivity-HistoryFragment

    private Handler handler;

    public static Endpoint getInstance() {
        try {
            if (uniqueEndpoint == null) {
                uniqueEndpoint = new Endpoint();
            }
        } catch (DeploymentException | IOException | URISyntaxException e) {
            e.printStackTrace();
            Log.d("delf", "occurred exception");
        }
        return uniqueEndpoint;
    }

    public static RetrofitUploadData getUploader() {
        if (uniqueUploader == null) {
            Log.d("delf", "[Endpoint] uploader is create. the name is " + user.getName() + " and group key is " + user.getGroup().getPrimaryKey());
            uniqueUploader = new RetrofitUploadData(user.getName(), user.getGroup().getPrimaryKey());
        }
        return uniqueUploader;
    }

    public static RetrofitDownloadData getDownloader() {
        if (uniqueDownloader == null) {
            uniqueDownloader = new RetrofitDownloadData(user.getName(), user.getGroup().getPrimaryKey());
        }
        return uniqueDownloader;
    }

    public interface SecondCallback {
        void onEndpointResponse(JSONObject result); // define at EndpointInBackground
    }
    public void setSecondCallback(SecondCallback callback) {
        secondCallback = callback;
    }

    public interface ParticipantCallback {
        void onParticipantStatus(String newMember);
    }
    public void setParticipantCallback(ParticipantCallback callback) {
        participantCallback = callback;
    }

    public interface NameChangeCallback {
        void onSuccess(String origin, String changed);
    }
    public void setNameChangeCallback(NameChangeCallback callback) {
        nameChangeCallback = callback;
    }

    public interface ContentsCallback {
        void onContentsUpdate(Contents contents);
    }
    public void setContentsCallback(ContentsCallback callback) {
        contentsCallback = callback;
    }


    private Endpoint() throws DeploymentException, IOException, URISyntaxException {
        URI uRI = new URI(uri);
        Log.d("Endpoint", "connecting server...");
        ContainerProvider.getWebSocketContainer().connectToServer(this, uRI);
        new PingPong().start();
    }

    @OnOpen
    public void onOpen(Session session) {
        Log.d("delf", "[Endpoint] server connected. session open success.");
        this.session = session;
    }

    @OnMessage
    public void onMessage(Message message) {
        Log.d("delf", "[Endpoint] get message from server: " + message.toString());

        try {
            switch (message.get(Message.TYPE)) {
                case Message.RESPONSE_CREATE_GROUP:
                    switch (message.get(Message.RESULT)) {
                        case Message.CONFIRM:
                            secondCallback.onEndpointResponse(message.getJson());
                            break;

                        case Message.REJECT:
                            break;
                    }
                    user = new User(message.get(Message.NAME), new Group(message.get(Message.GROUP_PK)));
                    break;

                case Message.RESPONSE_JOIN_GROUP:
                    secondCallback.onEndpointResponse(message.getJson());
                    switch (message.get(Message.RESULT)) {
                        case Message.CONFIRM:
                            break;

                        case Message.REJECT:
                            break;
                    }
                    user = new User(message.get(Message.NAME), new Group(message.get(Message.GROUP_PK)));
                    break;



                case Message.RESPONSE_EXIT_GROUP:
                    Log.d("delf", "[Endpoint] exit the group");
                    break;

                case Message.NOTI_ADD_PARTICIPANT:
                    participantCallback.onParticipantStatus(message.get(Message.PARTICIPANT_NAME));
                    Log.d("delf", "[Endpoint] \"" + message.get(Message.PARTICIPANT_NAME) + "\" is join in ths group");
                    break;

                case Message.NOTI_EXIT_PARTICIPANT:
                    participantCallback.onParticipantStatus(message.get(Message.PARTICIPANT_NAME));
                    Log.d("delf", "[Endpoint] \"" + message.get(Message.PARTICIPANT_NAME) + "\" exit the group");
                    break;

                case Message.NOTI_UPLOAD_DATA:
                    Log.d("delf", "[Endpoint] \"" + message.get("uploadUserName") + "\" is upload the data");
                    lastContentsPK  = message.get("contentsPKName");
                    Contents contents = MessageParser.getContentsbyMessage(message);
                    user.getGroup().addContents(contents);

                    contentsCallback.onContentsUpdate(contents);
                    if(!message.get("uploadUserName").equals(user.getName())) {
                        handler.sendEmptyMessage(0);
                        handler.notify();
                    }
                    break;
                case Message.NOTI_CHANGE_NAME:
                    nameChangeCallback.onSuccess(message.get(Message.NAME), message.get(Message.CHANGE_NAME));

                    break;

                case Message.RESPONSE_CHANGE_NAME:
                    switch (message.get(Message.RESULT)) {
                        case Message.CONFIRM:
                            nameChangeCallback.onSuccess(user.getName(), message.get(Message.CHANGE_NAME));
                            user.setName(message.get(Message.CHANGE_NAME));
                            Log.d("Endpoint", "change nickname confirm");
                            break;

                        case Message.REJECT:
                            Log.d("Endpoint", "change nickname reject");
                            break;
                    }
                    break;
                case Message.PONG:
                    break;

                default:
                    Log.d("delf", "unknown message");
                    System.out.println("default");
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void sendMessage(Message message) throws IOException, EncodeException {
        if (session == null) {
            System.out.println("debuger_delf: session is null");
        }
        session.getBasicRemote().sendObject(message);
        Log.d("delf", "send message to server: " + message.toString());
    }

    @OnClose
    public void onClose() {
        this.session = null;
        Log.d("delf", "session closed.");
    }

    public static User getUser() {
        return user;
    }

    // Thread for retain Websocket connection
    // prevent timeout disconnection
    class PingPong extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(3 * 60 * 1000);
                    sendMessage(new Message().setType(Message.PING));
                } catch (InterruptedException e) {
                    System.err.println("[ERROR] Pingping thread - InterruptedException");
                } catch (EncodeException e) {
                    System.err.println("[ERROR] Pingping thread - EncodeException");
                    // e.printStackTrace();
                } catch (IOException e) {
                    System.err.println("[ERROR] Pingping thread - IOException");
                    // e.printStackTrace();
                }
            }
        }
    }
}


