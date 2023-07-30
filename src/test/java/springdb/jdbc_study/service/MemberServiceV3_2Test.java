package springdb.jdbc_study.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import springdb.jdbc_study.domain.Member;
import springdb.jdbc_study.repository.MemberRepositoryV3;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static springdb.jdbc_study.connection.ConnectionConst.*;

/*
 TxTemplate 을 활용한 ServiceV3_2 로 변경한다
 - V3_1 에서 변경할건 하나도 없음
 */
public class MemberServiceV3_2Test {

    static final String MEMBER_A = "memberA";
    static final String MEMBER_B = "memberB";
    static final String MEMBER_FOR_ERROR = "FOR_ERROR";

    private DataSource dataSource;
    private MemberRepositoryV3 memberRepository;
    private MemberServiceV3_2 memberService;

    @BeforeEach
    void before() { // 의존성들을 만들어서 넣어준다
        dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        memberRepository = new MemberRepositoryV3(dataSource);

        // TxManager 주입
        // TxManager 는 Connection 을 생성할 수 있어야함
        // 그러기 위해선 DataSource 가 필요 (안넣어도 생성되는데, TxManager에 DataSource Set 없다고 오류 뜸)
        PlatformTransactionManager txManager = new DataSourceTransactionManager(dataSource); // JDBC 기술의 TxManager
        memberService = new MemberServiceV3_2(txManager, memberRepository);
    }

    @Test
    @DisplayName("정상 이체")
    void accountTransferNormalSituation() throws Exception {
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        // when
        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);

        // then (멀쩡하게 통과함)
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberB.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("이체 중 예외 발생")
    void accountTransferErrorSituation() throws SQLException {

        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberError = new Member(MEMBER_FOR_ERROR, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberError);

        // when (EX 이름에 대한 예외가 터질 것)
        assertThatThrownBy(() -> memberService.accountTransfer(memberA.getMemberId(), memberError.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);

        // then (잘못된 상황이 됨을 확인한다)
        // Spring 제공 TxManager, TxSyncManager 을 사용해도 정상적으로 Rollback 됨을 확인할 수 있다
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberERROR = memberRepository.findById(memberError.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(10000);
        assertThat(findMemberERROR.getMoney()).isEqualTo(10000);
    }


    // DB 초기화로 지속 수행 (트랜젝션 롤백 쓰면 이런거 없어도 됨)
    @AfterEach
    void afterEach() throws SQLException {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_FOR_ERROR);
    }

}
