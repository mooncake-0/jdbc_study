package springdb.jdbc_study.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import springdb.jdbc_study.connection.ConnectionConst;
import springdb.jdbc_study.domain.Member;
import springdb.jdbc_study.repository.MemberRepositoryV1;

import javax.sql.DataSource;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static springdb.jdbc_study.connection.ConnectionConst.*;

public class MemberServiceV1Test {

    static final String MEMBER_A = "memberA";
    static final String MEMBER_B = "memberB";
    static final String MEMBER_FOR_ERROR = "FOR_ERROR";

    private MemberRepositoryV1 memberRepository;
    private MemberServiceV1 memberService;

    @BeforeEach
    void before() { // 의존성들을 만들어서 넣어준다
        DataSource ds = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        memberRepository = new MemberRepositoryV1(ds);
        memberService = new MemberServiceV1(memberRepository);
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
        Member membeError = new Member(MEMBER_FOR_ERROR, 10000);
        memberRepository.save(memberA);
        memberRepository.save(membeError);

        // when (EX 이름에 대한 예외가 터질 것)
        // 다만 문제는, A는 이체되었지만, B에겐 오지 않는 다는 점이 문제
        // 왜냐하면 Transaction 은 제어하지 않았고 autocommit 진행중이기 때문
        assertThatThrownBy(() -> memberService.accountTransfer(memberA.getMemberId(), membeError.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);

        // JPA 아니에여~~~~~~ 다시 불러와야 돼여~~~ 위에꺼 안바뀌어여~~~
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberERROR = memberRepository.findById(membeError.getMemberId());

        // then (잘못된 상황이 됨을 확인한다)
        assertThat(findMemberA.getMoney()).isEqualTo(8000);
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
