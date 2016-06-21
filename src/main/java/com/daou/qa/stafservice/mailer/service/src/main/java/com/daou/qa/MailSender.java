package com.daou.qa;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.util.Properties;

/**
 * javax.mail 을 이용한 메일 전송 CLASS
 * Created by intern on 2016-05-25.
 */
public class MailSender extends MailerRepository {

    private static final String PROPS_HOST = "mail.host";
    private static final String PROPS_PROTOCOL = "mail.transport.protocol";
    private static final String PROTOCOL = "smtp";

    /**
     * mail을 보내기 위한 properties를 정의 하는 함수
     *
     * @param mailInfo {@link MailInfo}
     * @return 설정한 properties 정보
     */
    private Properties setProps(MailInfo mailInfo) {

        Properties props = System.getProperties();
        props.put(PROPS_HOST, mailInfo.getMailServer());
        props.put(PROPS_PROTOCOL, PROTOCOL);

        return props;
    }

    /**
     * javax.mail을 이용하여 메일을 전송 하는 부분
     *
     * @param mailInfo {@link MailInfo}
     * @return true / false
     */
    public boolean send(MailInfo mailInfo) {

        Session mailSession = Session.getDefaultInstance(setProps(mailInfo), null);

        try {
            InputStream source = new FileInputStream(mailInfo.getEmlFilePath());
            MimeMessage eml = new MimeMessage(mailSession, source);
            eml.setFrom(new InternetAddress(mailInfo.getFrom()));
            eml.setRecipients(Message.RecipientType.TO, mailInfo.getTo());
            Transport.send(eml);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * EML 파일을 서버에 생성하는 메서드
     */
    public void makeEMLFile(String fileName, String contents){

        try {
            BufferedWriter bufWriter = new BufferedWriter(new FileWriter(fileName, false));

            bufWriter.write(contents);
            bufWriter.flush();
            bufWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

