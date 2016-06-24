package com.daou.qa;

import java.util.Properties;

/**
 * Created by intern on 2016-05-25.
 */
public abstract class MailerRepository {

    SendMailResult sendMail(MailInfo mailInfo) { return new SendMailResult(0, "OK"); }
}
