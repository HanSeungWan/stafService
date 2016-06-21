package com.daou.qa.exception;

/**
 * Created by intern on 2016-06-16.
 */
public class NoSuchServiceException extends Exception {

    public NoSuchServiceException(String service) {

        super(service);

        String msg = "NO SUCH SERVICE : " + service;
    }
}
