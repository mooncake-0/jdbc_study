package springdb.jdbc_study.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
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
 @Transactional AOP 를 적용해보자
 단, Test 는 Spring Container 를 띄우지 않는다
 ------> Transactional Proxy (AOP) 기술을 사용하려면 Spring Container 및 Spring 의 간섭이 필요
 ------> Spring 을 이 Test 에선 같이 실행해줘야한다
 */
@SpringBootTest
@Slf4j
public class MemberServiceV3_FinalTest {

    static final String MEMBER_A = "memberA";
    static final String MEMBER_B = "memberB";
    static final String MEMBER_FOR_ERROR = "FOR_ERROR";

    // 주입 지정으로 인한 Autowire 명시
    @Autowired
    private MemberRepositoryV3 memberRepository;

    @Autowired
    private MemberServiceV3_Final memberService;

    /*
     Spring 기술을 사용하므로, Spring Container 에 있어야할
     Bean 들을 Test Configuration 으로 주입 필요하다
     */
    @TestConfiguration
    static class TestConfig {

        //1 - DataSource 주입 필요
        @Bean
        DataSource dataSource() {
            return new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        }

        //2 - TxManager 주입 필요 - DataSource 까지 전달해줘야 한다
        @Bean
        PlatformTransactionManager transactionManager() {
            return new DataSourceTransactionManager(dataSource());
        }

        //3 - 이 Test 에서 사용되는 Repository 주입 - 스프링 컨테이너 쓰니까 대신 @BE 로 직접 주입 안해줘도 됨
        @Bean
        MemberRepositoryV3 memberRepository() {
            return new MemberRepositoryV3(dataSource());
        }

        //4 - 이 Test 에서 사용되는 Service 주입
        @Bean
        MemberServiceV3_Final memberService() {
            return new MemberServiceV3_Final(memberRepository());
        }
    }


    @Test
    void 정말_AOP가_ProxyClass를_만들어줄까() {

        log.info("memberService class = {}", memberService.getClass());
        log.info("memberRepository class = {}", memberRepository.getClass());

        // AopCheck 를 지원해주는 Lib 도 지원한다 - AopUtils - AOP 클래스가 맞는지 확인
        assertThat(AopUtils.isAopProxy(memberService)).isTrue(); //@Tx 달린 곳만 Aop 지원
        assertThat(AopUtils.isAopProxy(memberRepository)).isFalse();
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
