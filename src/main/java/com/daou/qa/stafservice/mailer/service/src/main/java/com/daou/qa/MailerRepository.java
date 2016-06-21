package com.daou.qa;

import java.util.Properties;

/**
 * Created by intern on 2016-05-25.
 */
public abstract class MailerRepository {

    boolean send(MailInfo mailInfo) {
        return false;
    }

    private Properties setProps(MailInfo mailInfo) {
        return null;
    }

}
