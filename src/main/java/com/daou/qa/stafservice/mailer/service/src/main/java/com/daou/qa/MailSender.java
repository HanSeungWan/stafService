package com.daou.qa;

import java.io.*;
import java.net.Socket;

/**
 * SMTP를  이용한 메일 전송 CLASS
 * SMTP STEP 따른 검사를 통하여 Result code, message 리턴
 * Created by intern on 2016-05-25.
 */
public class MailSender extends MailerRepository {

    /**
     * MailerRepository에 정의 되어 있는 send 메서드 정의
     * send 메서드는 Mailer에서 제공 하는 메일 전송 서비스 이다.
     * 사용자는 send 메서드를 통하여 메일을 전송하다.
     * STMP Step 에 따라 검사하여 오류를 검사 한다.
     * @param mailInfo {@link MailInfo}
     * @return SendMailResult {@link SendMailResult}
     */
    public SendMailResult send(MailInfo mailInfo) {

        try {
            Socket socket = null;
            try {
                socket = new Socket(mailInfo.getMailServer(), 25);
            } catch (Exception e){
                return new SendMailResult(-1, "SERVER IS NOT VALUED : " + mailInfo.getMailServer());
            }

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);

            String line = bufferedReader.readLine();
            if (!line.startsWith("220")) return new SendMailResult(getSMTPCode(line), line);

            printWriter.println("HELO mydomain.name");
            line = bufferedReader.readLine();
            if (!line.startsWith("250")) return new SendMailResult(getSMTPCode(line), line);

            printWriter.println("MAIL FROM:" + mailInfo.getFrom());
            line = bufferedReader.readLine();
            if (!line.startsWith("250")) return new SendMailResult(getSMTPCode(line), line);

            printWriter.println("RCPT TO:" + mailInfo.getTo());
            line = bufferedReader.readLine();
            if (!line.startsWith("250")) return new SendMailResult(getSMTPCode(line), line);

            printWriter.println("DATA");
            line = bufferedReader.readLine();
            if (!line.startsWith("354")) return new SendMailResult(getSMTPCode(line), line);

            printWriter.println(mailInfo.getContents());
            printWriter.println(".");
            line = bufferedReader.readLine();
            if (!line.startsWith("250")) return new SendMailResult(getSMTPCode(line), line);

            printWriter.println("quit");

            bufferedReader.close();
            printWriter.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return new SendMailResult(0, "OK");
    }

    private int getSMTPCode(String stmpMsg) {

        return Integer.parseInt(stmpMsg.substring(0, 3));
    }
}
