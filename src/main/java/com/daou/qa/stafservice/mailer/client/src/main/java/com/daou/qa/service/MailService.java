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
     * @return mailer send service 리턴 code SMTP 응답 코드표 참고
     *
     * [SMTP 응답코드표]
     * 211 : 시스템 상태 또는 시스템 도움말 답장
     * 214 : 도움말 메시지 220 : 도메인 서비스가 준비됨
     * 221 : 도메인 서비스가 전송 채널을 폐쇄하고 있음
     * 250 : 요청된 우편행위 OK, 완료되었음
     * 251 : 사용자가 Local이 아님 < 전진경로>로 배달
     * 354 : 우편입력을 시작함 : .로 종료됨
     * 421 : 도메인 서비스가 수행되지 않음, 전송 채널을 폐쇄하고 있음
     * 451 : 요청된 행위가 강제 종료되었음 : 처리중 오류 발생
     * 452 : 요청된 행위가 이루어지지 않았음 : 시스템 저장 장치가 충분하지 않음
     * 500 : 구문오류, 명령이 인식되지 않음
     * 501 : 매개변수나 인수에서 구문오류
     * 502 : 명령이 구현되지 않았음 503 : 명령들의 순서가 잘못되었음
     * 504 : 명령 매개변수가 구현되지 않았음
     * 550 : 요청된 행위가 이루어지지 않았음 : 우편함이 이용될 수 없음
     * 551 : 사용자가 로컬이 아님
     * 552 : 요청된 우편 행위가 강제 종료됨 : 저장 장치 할당을 초과
     * 553 : 요청된 행위가 이루어지지 않았음 : 우편함 이름 사용 불가
     * 554 : 거래가 실패하였음
     *
     */
    int service(MailInfo mailInfo);
}
