package com.daou.qa.vo;

/**
 * Created by intern on 2016-06-03.
 */
public class MailInfo {

    private String mailServer;
    private String from;
    private String to;

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    private String file;
    private String contents;

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
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

