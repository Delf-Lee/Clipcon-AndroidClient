package com.sprout.clipcon.server;

import com.sprout.clipcon.model.Message;
import com.sprout.clipcon.transfer.RetrofitCommonRequest;

/**
 * Created by delf on 17-10-09.
 */

public class MessageHandler {
    private RetrofitCommonRequest requester = new RetrofitCommonRequest();
    public RetrofitCommonRequest getRequester() {
        return requester;
    }
    public void handleMessage(Message message) {
        String requestType = message.getType();

        switch (requestType) {
            case Message.REQUEST_CREATE_GROUP:
                requester.commonRequest(message.toString());
                break;
        }
    }
}
