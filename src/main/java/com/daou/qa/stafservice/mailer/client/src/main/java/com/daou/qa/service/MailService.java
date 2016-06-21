package com.daou.qa.service;

import com.daou.qa.vo.MailInfo;

/**
 * Created by intern on 2016-05-24.
 */
public interface MailService {

    /**
     * service 정의 함수
     * 사용자 에게 제공할 service 를 정의 하는 함수이다.
     * service 구현시 implements 를 받아 구현 하여 제공 해야 한다.
     *
     * @return service 성공/실패
     */
    boolean service(MailInfo mailInfo);
}
