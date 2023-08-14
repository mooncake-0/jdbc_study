package springdb.jdbc_study.exception.basic_study;


import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class UncheckedExceptionTest {

    @Test
    void unchecked_catch() {
        Service service = new Service();
        service.callCheck(); // 통과한다 : 여기까지 예외가 올라오지 않음 : 잘 잡힌 것
    }

    @Test
    void unchecked_throw() {
        Service service = new Service();
//        service.callThrow(); // 컴파일에서 잡지 않는다 > 예외가 올라오기 때문에 Test 실패하게 됨. (Test 는 Exception 누수시 실패)
        Assertions.assertThatThrownBy(() -> service.callThrow()).isInstanceOf(MyUncheckedException.class); // 예외를 잘 잡았음
    }

    /**
     *  RuntimeException 을 상속받은 예외는 언체크 예외이다
     */
    static class MyUncheckedException extends RuntimeException {
        public MyUncheckedException(String msg) {
            super(msg);
        }
    }

    /*
     Uncheck 예외는 잡거나 던지지 않아도 된다. 안잠으면 밖으로 자동으로 던진다
     */
    static class Service{
        Repository repository = new Repository();

        /*
         필요한 경우, 예외를 잡아서 처리하면 됨
         */
        public void callCheck() {
            // 체크든 언체크든, 모든 예외는 잡거나 던지면 된다
            try {
                repository.call();
            } catch (MyUncheckedException exception) {
                log.info("예외처리, message = {}", exception.getMessage(), exception);
            }
        }

        public void callThrow() { // 예외를 잡지 않아도 자연스럽게 호출된 곳으로 던진다, 선언 X 해도 됨 (물론 해줘도 됨)
            repository.call();
        }
    }


    static class Repository{
        public void call() { // 바로 차이가 보임. 발생한 에러 >> 밖으로 던지지 않아도 Compile 오류가 발생하지 않는다

            throw new MyUncheckedException("예외 발생");
        }
    }
}
