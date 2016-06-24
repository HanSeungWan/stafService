package com.daou.qa;

/**
 * Created by intern on 2016-06-23.
 */
public class SendMailResult {

    SendMailResult(int theRC, String theResult){

        this.rc = theRC;
        this.result = theResult;
    }

    public int rc; // Result Code
    public String result; // Result Message
}
