package com.sprout.clipcon.server;

/**
 * Created by heejeong on 2017. 9. 14..
 */

public class ServerInfo {
    private static final String PROTOCOL = "http://";
    //private static final String SERVER_ADDR = "113.198.84.53";
    private static final String SERVER_ADDR = "118.176.16.163";
    private static final String SERVER_PORT = "8080";
    private static final String CONTEXT_ROOT = "globalclipboard";

    public static final String SERVER_URL_PART = SERVER_ADDR + ":" + SERVER_PORT + "/" + CONTEXT_ROOT;
    public static final String SERVER_URL = PROTOCOL + SERVER_ADDR + ":" + SERVER_PORT + "/" + CONTEXT_ROOT;

    public static final String CLIPCON_VERSION = "1.1";

    public static final String DOWNLOAD_SERVLET = "/DownloadServlet";
    public static final String UPLOAD_SERVLET = "/UploadServlet";
    // public static final String BUGREPORT_SERVLET = "/BugReportServlet";
}
