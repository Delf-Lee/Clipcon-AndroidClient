package com.sprout.clipcon.model;

import java.util.HashMap;
import java.util.Map;

public class History {

    private Map<String, Contents> contentsMap = new HashMap<String, Contents>();

    public void addContents(Contents contents) {
        contentsMap.put(contents.getContentsPKName(), contents);
    }

    public Contents getContentsByPK(String contentsPKName) {
        return contentsMap.get(contentsPKName);
    }
}






