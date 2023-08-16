package springdb.jdbc_study.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import springdb.jdbc_study.domain.Member;
import springdb.jdbc_study.repository.MemberRepository;
import springdb.jdbc_study.repository.MemberRepositoryV3;
import springdb.jdbc_study.repository.MemberRepositoryV4_1;
import springdb.jdbc_study.repository.MemberRepositoryV4_2;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
 예외 누수 문제까지 해결해보자
 SQLException 을 제거해보자
 = 순수한 Java 코드의 완성
 Member Repository 에 의존하자
 */
@SpringBootTest
@Slf4j
public class MemberServiceV4Test {

    static final String MEMBER_A = "memberA";
    static final String MEMBER_B = "memberB";
    static final String MEMBER_FOR_ERROR = "FOR_ERROR";

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberServiceV4 memberService;

    /*
     DataSource - application.properties 정보 작성 필수임!
     TxManager - 자동주입
      ---> 둘을 빼도 정상동작한다
     */
    @TestConfiguration
    static class TestConfig {

        // DataSource 는 Spring Container 에서 가져와주자
        @Autowired
        private DataSource dataSource;

        /*
         구현체 등록
         */
        @Bean
        MemberRepositoryV4_2 memberRepository() {
            return new MemberRepositoryV4_2(dataSource);
        }

        @Bean
        MemberServiceV4 memberService() {
            return new MemberServiceV4(memberRepository());
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
    void accountTransferNormalSituation()  {
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
    void accountTransferErrorSituation()  {

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
    void afterEach()  {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_FOR_ERROR);
    }

}
