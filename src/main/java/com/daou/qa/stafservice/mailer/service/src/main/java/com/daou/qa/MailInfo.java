package com.daou.qa;

/**
 * Created by intern on 2016-05-24.
 */
public class MailInfo {

    private String mailServer;
    private String from;
    private String to;
    private String emlFilePath;

    public String getEmlFilePath() {
        return emlFilePath;
    }

    public void setEmlFilePath(String emlFilePath) {
        this.emlFilePath = emlFilePath;
    }

    public String getMailServer() {
        return mailServer;
    }

    public void setMailServer(String mailServer) {
        this.mailServer = mailServer;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

}
