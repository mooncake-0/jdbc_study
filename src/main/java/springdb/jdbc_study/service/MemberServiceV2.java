package springdb.jdbc_study.service;

import lombok.extern.slf4j.Slf4j;
import springdb.jdbc_study.domain.Member;
import springdb.jdbc_study.repository.MemberRepositoryV1;
import springdb.jdbc_study.repository.MemberRepositoryV2;

import javax.sql.DataSource;
import java.sql.*;

/*
  트랜젝션을 사용해서 V1에서 발생한 에러를 방지해보자
 */
@Slf4j
public class MemberServiceV2 {

    private final MemberRepositoryV2 memberRepositoryV2;
    private final DataSource dataSource; // 이 앱은 현재 어떤 방식으로 Connection 을 가져올 수 있는가?

    public MemberServiceV2(MemberRepositoryV2 memberRepositoryV2, DataSource dataSource) {
        this.memberRepositoryV2 = memberRepositoryV2;
        this.dataSource = dataSource;
    }

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

        // 비즈니스 로직 시작 부분에서 Tx 시작을 위해, Service 에서 Connection 을 형성한다
        Connection con = dataSource.getConnection();

        try {
            con.setAutoCommit(false); // 트랜젝션을 시작하는 부분

            // 순수 비즈니스 로직을 넣을 부분
            bizLogic(con, fromId, toId, money);

            con.commit(); // 세션이 이 트랜젝션을 commit 하고 종료시킨다

        } catch (Exception e) {

            con.rollback(); //이 트랜젝션 전 상태로 되돌린다
            throw new IllegalStateException(e);

        } finally {
            // MEMO :: 항상 Resource 를 시작하면, 해제까지 생각해줘야한다
            //         특정 기술을 사용할때도, Resource 를 어떻게 관리하는지 알아야 한다
            releaseConnection(con);
        }
    }

    // Connection 을 넣도록 바뀌었다
    public void bizLogic(Connection con, String fromId, String toId, int money) throws Exception {

        // Connection 을 모두 같은 Connection 을 사용하도록 제어한다
        Member fromMember = memberRepositoryV2.findById(con, fromId);
        Member toMember = memberRepositoryV2.findById(con, toId);

        memberRepositoryV2.update(con, fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepositoryV2.update(con, toId, toMember.getMoney() + money);
    }


    private void validation(Member member) {
        if (member.getMemberId().equals("FOR_ERROR")) {
            throw new IllegalStateException("이체중 예외 발생 : 그냥 상황 종료시켜버리기");
        }
    }

    private void releaseConnection(Connection con) {
        // 자원 해제해야함
        if (con != null) {
            try {
                // 만약에 Connection Pool 을 쓰는경우다 (현재는 일단 DataSource 로 받아와서 뭘쓰는진 모르겠으나)
                // 그러면 돌아가야함 Pool 로.. 따라서 사용하기 전 상태로 돌려놓아야 한다
                // CP 를 사용하지 않더라도 DEFAULT 로 내가 한 제어는 풀어주는게 좋음
                con.setAutoCommit(true); // MEMO :: 기본값으로 되돌린 상태로 풀로 돌려줘야 한다 (Connectino pool 고려)
                con.close(); // Pool 을 사용하면 이 close 를 제어해서 돌아가게끔 세팅이 되어 있음
            } catch (Exception e) {
                log.info("Connection Closing ERROR = {}", e, e);
            }
        }
    }
}
