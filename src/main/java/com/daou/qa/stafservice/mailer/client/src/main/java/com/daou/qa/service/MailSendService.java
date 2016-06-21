package com.daou.qa.service;

import com.daou.qa.vo.MailInfo;
import com.ibm.staf.STAFException;
import com.ibm.staf.STAFHandle;
import com.ibm.staf.STAFResult;
import com.ibm.staf.STAFUtil;

import java.io.*;

/**
 * Client 가 사용하는 실제 서비스 구현 CLASS
 * Created by intern on 2016-05-23.
 */
public class MailSendService implements MailService {

    private STAFHandle handle;
    private MailInfo mailInfo;
    private String stafServer;

    public MailSendService(String stafServer) {

        String HANDLENAME = "sendMail";

        this.stafServer = stafServer;
        this.handle = createSTAFHandle(HANDLENAME);
    }

    /**
     * STAF SERVICE 를 실행 시킬 COMMAND 준비
     *
     * @return STAF COMMAND
     */
    private String prepareRequest() {

        return "SEND MAILSERVER " + STAFUtil.wrapData(mailInfo.getMailServer()) +
                " FROM " + STAFUtil.wrapData(mailInfo.getFrom()) +
                " TO " + STAFUtil.wrapData(mailInfo.getTo()) +
                " CONTENTS " + STAFUtil.wrapData(mailInfo.getContents());
    }

    /**
     * DAOUMAMIL External Service 에 Request 를 전송
     * @return Request 실행 후 결과
     */
    private STAFResult submitRequest() {

        getEMLContents();

        final String SERVICE = "DAOUMAIL";

        return handle.submit2(this.stafServer, SERVICE, prepareRequest());
    }

    /**
     * STAF HANDLE 생성
     *
     * @param name HANDLE 이름
     * @return handle
     */
    private STAFHandle createSTAFHandle(String name) {

        try {
            handle = new STAFHandle(name);
        } catch (STAFException e) {
            System.out.println("ERROR: CREATE HANDLE Result failed RC: " + e.rc);
        }
        return handle;
    }

    /**
     * STAF DAOUMAIL SERVICE 를 이용한 메일 전송
     * * @return STAF RESULT CODE -> http://staf.sourceforge.net/current/STAFRC.htm (참고)
     */
    public boolean sendMail(MailInfo mailInfo) {

        this.mailInfo = mailInfo;
        STAFResult result = submitRequest();

        if (result.rc != 0) {
            System.out.println("ERROR: MAIL SEND Result failed RC: " + result.rc + ", Result: " + result.result);
            return false;
        } else {
            System.out.println("MAIL SEND Result: " + result.result);
            return true;
        }
    }

    /**
     * EML 파일을 불러와 내용을 EML 파일 내용을 저장하는 메서드
     *  mailInfo vo에 contents 부분에 내용 저장
     * @return true / false
     */
    private boolean getEMLContents() {

        String contents = "";

        try {
            File file = new File("target\\test-classes\\" + mailInfo.getFile());
            FileInputStream in = new FileInputStream(file.getAbsolutePath());
            InputStreamReader inReader = new InputStreamReader(in, "UTF-8");
            BufferedReader bufReader = new BufferedReader(inReader);

            StringBuffer stringBuffer = new StringBuffer();

            while ((contents = bufReader.readLine()) != null){

                stringBuffer.append(contents + "\n");
            }

            mailInfo.setContents(stringBuffer.toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Service 호출 함수
     *
     * @return sndMail service 실행 결과
     */
    public boolean service(MailInfo mailInfo) {

        return sendMail(mailInfo);
    }
}
