package com.daou.qa.service;

import com.daou.qa.vo.MailInfo;
import com.daou.qa.vo.MailServiceList;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by intern on 2016-06-07.
 */
public class MailSendServiceTest {

    private MailInfo mailInfo;
    private MailService mailService;

    @Before
    public void init(){

        MailInfo mailInfo = new MailInfo();

        mailInfo.setTo("test0045@terracetech.co.kr");
        mailInfo.setFrom("test0047@terracetech.co.kr");
        mailInfo.setMailServer("dotest.terracetech.co.kr");
        mailInfo.setServer("172.21.22.19");
        mailInfo.setEmlFilePath("C:\\Users\\intern\\Desktop\\SAMPLE_EML_FILE\\첨부 파일 메일 테스트.eml");
        mailInfo.setFileName("첨부 파일 메일 테스트.eml");

        MailService mailService = ClientServiceFactory.getMailService(MailServiceList.SEND_MAIL_SERVICE.getService());

    }

    @Test
    public void sendMail() throws Exception {

        assertTrue("메일 전송 검사",mailService.service(mailInfo));
    }
}