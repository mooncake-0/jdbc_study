package springdb.jdbc_study.service;

import org.springframework.transaction.annotation.Transactional;
import springdb.jdbc_study.domain.Member;
import springdb.jdbc_study.repository.MemberRepositoryV3;

import java.sql.SQLException;

/*
 트랜젝션 - @Transactional AOP 사용 > Spring 의 Proxy 지원
 */
public class MemberServiceV3_Final {

    /*
     매우 깔끔
     순수한 JAVA 코드, 별도의 기술 없이 Service 를 짤 수 있다
     MEMO : 이게 진짜 Service 영역에서 중요한 부분!!!
     */
    private final MemberRepositoryV3 memberRepositoryV3;

    public MemberServiceV3_Final(MemberRepositoryV3 memberRepositoryV3) {
        this.memberRepositoryV3 = memberRepositoryV3;
    }

    // Spring 에선 @Tx 달린 클래스에 대해선 Proxy 를 만들어야 한다고 인지한다
    @Transactional
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        bizLogic(fromId, toId, money);
    }

    public void bizLogic(String fromId, String toId, int money) throws SQLException {

        Member fromMember = memberRepositoryV3.findById(fromId);
        Member toMember = memberRepositoryV3.findById(toId);

        memberRepositoryV3.update(fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepositoryV3.update(toId, toMember.getMoney() + money);
    }


    private void validation(Member member) {
        if (member.getMemberId().equals("FOR_ERROR")) {
            throw new IllegalStateException("이체중 예외 발생 : 그냥 상황 종료시켜버리기");
        }
    }

}
