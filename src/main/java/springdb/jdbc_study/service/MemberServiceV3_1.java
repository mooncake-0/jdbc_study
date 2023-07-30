package springdb.jdbc_study.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import springdb.jdbc_study.domain.Member;
import springdb.jdbc_study.repository.MemberRepositoryV3;

import java.sql.SQLException;

/*
  트랜젝션 - 트랜젝션 매니저를 사용해서 제어
*/
@Slf4j
public class MemberServiceV3_1 {

    private final MemberRepositoryV3 memberRepositoryV3;
    private final PlatformTransactionManager txManager; // TxManager 에서 getConnection 을 진행해준다 (DataSource 필요없음)
    // 물론 구현체를 주입해줄 때는 TxManager 가 사용할 DataSource 당연히 같이 넣어줘야 한다...!!!!! (알아서 생성하는게 아님.. ㅋㅋ)

    public MemberServiceV3_1(PlatformTransactionManager txManager, MemberRepositoryV3 memberRepositoryV3) {
        this.txManager = txManager;
        this.memberRepositoryV3 = memberRepositoryV3;
    }

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

//        Connection con = dataSource.getConnection();
        // TxManager 를 통해서 가져온다
        // Connection 도 알아서 가져와 주고, setautocommit(false) 도 알아서 진행해줌
        TransactionStatus txStatus = txManager.getTransaction(new DefaultTransactionDefinition());

        try {
            // 순수 비즈니스 로직을 넣을 부분
            bizLogic(fromId, toId, money);

            txManager.commit(txStatus);
        } catch (Exception e) {
            txManager.rollback(txStatus);
            throw new IllegalStateException(e);

        }
        // TX Manager 에서 해제도 다 진행해주므로, release 안해줘도 된다.
//        finally {
//            releaseConnection(con);
//        }
    }

    public void bizLogic(String fromId, String toId, int money) throws Exception {

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
