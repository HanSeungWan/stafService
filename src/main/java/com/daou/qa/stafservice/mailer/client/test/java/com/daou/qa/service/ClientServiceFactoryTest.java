package com.daou.qa.service;

import com.daou.qa.vo.MailInfo;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by intern on 2016-06-07.
 */
public class ClientServiceFactoryTest {

    private MailInfo mailInfo;
    private ClientServiceFactory clientServiceFactory;

    @Before
    public void init() {

        mailInfo = new MailInfo();
        clientServiceFactory = new ClientServiceFactory();

        final String EMLFILE_1 = "C:\\Users\\intern\\Desktop\\SAMPLE_EML_FILE\\2016년 5월 마감안내.eml";
        final String ID_1 = "hanseungwan24@daou.co.kr";
        final String ID_2 = "test0048@terracetech.co.kr";
        final String MAILSERVER = "portal.daou.co.kr";
        final String SERVER = "local";

        // MailInfo User
        mailInfo.setMailServer(MAILSERVER);
        mailInfo.setServer(SERVER);
        mailInfo.setFrom(ID_2);
        mailInfo.setTo(ID_1);
        mailInfo.setEmlFilePath(EMLFILE_1);
    }

    @Test
    public void getMailService() throws Exception {

        final String type = "mailsendservice";
        boolean result = false;

        if (clientServiceFactory.getMailService(type) instanceof MailSendService) {

            result = true;

        } else {

            result = false;
        }

        assertTrue("service 생성 검사", result);
    }

    @Test
    public void notFoundType() throws Exception {

        final String type = "test";
        final String result = "NO SUCH TYPE : " + type;

        assertEquals(result, clientServiceFactory.notFoundType(type));
    }

}