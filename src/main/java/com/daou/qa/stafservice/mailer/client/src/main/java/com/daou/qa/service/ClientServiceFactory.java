package com.daou.qa.service;

import com.daou.qa.exception.NoSuchServiceException;
import com.daou.qa.vo.MailServiceList;

/**
 * Client Factory 구현 CLASS
 * STAF DAOUMAIL 서비스를 사용하기 위한 함수 정의 CLASS
 * Created by intern on 2016-05-23.
 */
public class ClientServiceFactory {

    /**
     * service factory 호출 메서드
     * type 을 parameter 로 입력 받아 class 를 찍어 내는 역활을 한다.
     *
     * type list
     * MailSendService type = EML_MAIL_SEND_SERVICE  eml 파일 mail 전송 class
     *
     * @param type 호출할 class 명
     * @param stafServer DAOUMAIL External service 가 등록된 STAF Server ip 주소
     * @return type 에 다른 instance
     */
    public static MailService getService(MailServiceList type, String stafServer) throws NoSuchServiceException{
        if(type == MailServiceList.EML_MAIL_SEND_SERVICE){
            return new MailSendService(stafServer);
        } else {
            throw new NoSuchServiceException(type.toString());
        }
    }
}
