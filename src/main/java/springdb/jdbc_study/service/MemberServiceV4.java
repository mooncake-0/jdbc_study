package springdb.jdbc_study.service;

import org.springframework.transaction.annotation.Transactional;
import springdb.jdbc_study.domain.Member;
import springdb.jdbc_study.repository.MemberRepository;
import springdb.jdbc_study.repository.MemberRepositoryV3;

import java.sql.SQLException;

/*
 예외 누수 문제까지 해결해보자
 SQLException 을 제거해보자
 = 순수한 Java 코드의 완성
 Member Repository 에 의존하자
 */
public class MemberServiceV4 {

    /*
     매우 깔끔
     순수한 JAVA 코드, 별도의 기술 없이 Service 를 짤 수 있다
     MEMO : 이게 진짜 Service 영역에서 중요한 부분!!!
     */

    private final MemberRepository memberRepository;

    public MemberServiceV4(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // Spring 에선 @Tx 달린 클래스에 대해선 Proxy 를 만들어야 한다고 인지한다
    @Transactional
    public void accountTransfer(String fromId, String toId, int money) {
        bizLogic(fromId, toId, money);
    }

    public void bizLogic(String fromId, String toId, int money) {

        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(toId, toMember.getMoney() + money);
    }


    private void validation(Member member) {
        if (member.getMemberId().equals("FOR_ERROR")) {
            throw new IllegalStateException("이체중 예외 발생 : 그냥 상황 종료시켜버리기");
        }
    }

}
