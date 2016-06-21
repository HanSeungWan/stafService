package com.daou.qa.stafservice;


/**
 * Instance Handler Exception
 * Created by byungshik on 2016. 6. 20..
 */
public class InstanceHandlerException extends Exception {
    public InstanceHandlerException(Exception e, String message) {
        super(message, e);
    }
}
