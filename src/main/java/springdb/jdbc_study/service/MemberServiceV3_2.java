package springdb.jdbc_study.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import springdb.jdbc_study.domain.Member;
import springdb.jdbc_study.repository.MemberRepositoryV3;

import java.sql.SQLException;

/*
  TxManager 를 활용하니까 반복 코드가 너무 많음.. 이걸 Service 단 다~ 써야함..
  트랜젝션 - 트랜젝션 템플렛을 활용하여 중복 코드 제거
 */
@Slf4j
public class MemberServiceV3_2 {

    private final TransactionTemplate txTemplate;
    private final MemberRepositoryV3 memberRepositoryV3;

    // 이 방식으로 많이 사용. TxManager 는 Spring 에서 주입해주는 구조
    // TxTemplate 을 주입해줘도 되는데
    // 1) 관례적
    // 2) TxTemplate 은 intf 가 아닌 그냥 class 임 - 유연성에 대한 이점이 없음 - 그래서 차라리 TxManager 에 유동성을 주는게 낫다
    public MemberServiceV3_2(PlatformTransactionManager txManager, MemberRepositoryV3 memberRepositoryV3) {
        this.txTemplate = new TransactionTemplate(txManager);
        this.memberRepositoryV3 = memberRepositoryV3;
    }

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

        // 그냥 내가 bizLogic 만 콜백으로 주면 되지 않을까 생각했던 것...
        // 응답 값이 없을 때 사용
        txTemplate.executeWithoutResult(txStatus ->{
            try {
                bizLogic(fromId, toId, money);
            } catch (SQLException exception) { // 발생한 에러를 언체크 예외 IllegalStateException 으로 전환 -> 이유는 나중에 설명
                throw new IllegalStateException(exception);
            }
        });

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
