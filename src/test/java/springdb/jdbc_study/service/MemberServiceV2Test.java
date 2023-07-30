package springdb.jdbc_study.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import springdb.jdbc_study.domain.Member;
import springdb.jdbc_study.repository.MemberRepositoryV2;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;

import static springdb.jdbc_study.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.*;

/*
 트랜젝션을 보장해준 ServiceV2 를 사용해보았음
 - 문제점 1) 코드가 졸라 더러움
 -      2) Exception 졸라 지저분함
 */
public class MemberServiceV2Test {
    static final String MEMBER_A = "memberA";
    static final String MEMBER_B = "memberB";
    static final String MEMBER_FOR_ERROR = "FOR_ERROR";
    private DataSource ds;
    private MemberRepositoryV2 memberRepository;
    private MemberServiceV2 memberService;

    @BeforeEach
    void before() { // 의존성들을 만들어서 넣어준다
        ds = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        memberRepository = new MemberRepositoryV2(ds);
        memberService = new MemberServiceV2(memberRepository, ds);
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
        // 2000 원을 A -> B 로 이동한다
        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);

        // then (멀쩡하게 통과함)
        // 다른 커넥션을 사용하여 Member 를 조회한다
        Connection conForReq = ds.getConnection();
        Member findMemberA = memberRepository.findById(conForReq, memberA.getMemberId());
        Member findMemberB = memberRepository.findById(conForReq, memberB.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("이체 중 예외 발생")
    void accountTransferErrorSituation() throws SQLException {

        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member membeError = new Member(MEMBER_FOR_ERROR, 10000);
        memberRepository.save(memberA);
        memberRepository.save(membeError);

        // when (EX 이름에 대한 예외가 터질 것)
        assertThatThrownBy(() -> memberService.accountTransfer(memberA.getMemberId(), membeError.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);

        // then (트랜젝션을 처음으로 보장해주니, Rollback 현상이 되어 트랜젝션 전 상태로 되돌아감을 확인할 수 있다)
        // 다른 커넥션을 사용하여 Member 를 조회한다
        Connection conForReq = ds.getConnection();
        Member findMemberA = memberRepository.findById(conForReq, memberA.getMemberId());
        Member findMemberERROR = memberRepository.findById(conForReq, membeError.getMemberId());
//        assertThat(findMemberA.getMoney()).isEqualTo(8000);
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
