package springdb.jdbc_study.exception.basic_study;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

/*
 이 Test 에는 Connection 에러가 등장한 적이 없지만, Connection 에러가 전달될 수 있음
 이제 예외들이 굳이 해결도 못하는 Class 들에 누수되지 않은채로, 모두 일괄 처리소로 전달될 수 있다
 RuntimeException 은 명세해주는 것도 중요함 - 주석으로 @throws 같이, 혹은 아예 그냥 checked 처럼 method 옆에 throws 해주기도 함
  ( 차피 줄줄이 소세지 달 필요 없기 때문)
  - SQL Exception 처럼 반드시 발생할 수 있는 Checked Exception 들은, 처리하려는 클래스에서 Unchecked 로 바꿔주는 조치도 좋음
  - 단, 이 경우 꼭 Throwable Constructor 를 만들어서 exception 을 연동시켜준 후 최종적으로 모두 Log 에 Trace 해야 한다
  - 안그러면 제공해주는 Log 들을 다 잃게 된다
 */
@Slf4j
public class AppWithUncheckedExceptionTest {

    @Test
    void checked() {
        Controller controller = new Controller();
        Assertions.assertThatThrownBy(() -> controller.request())
                .isInstanceOf(RuntimeSqlException.class);
    }

    @Test
    @DisplayName("또다른 팁 : 예외를 출력할 때는 로깅을 남겨야 한다. SOUT 을 하면 안됨 - IO 작업임")
    void printEx() {
        Controller controller = new Controller();
        try {
            controller.request();
        } catch (Exception e) {
            log.info("Exception = {}", e.getMessage(), e); // 스택 트레이스 가능 (마지막 PARAMS)
            // system.out.println 으로 트레이스 하면 안된다........ IO 작업 함부로 하면 안됨.
        }
    }

    static class Controller {
        Service s = new Service();

        public void request()  { // 누적되는 것 없음
            s.logic();
        }
    }

    static class Service {

        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();

        // 없어도 밖으로 알아서 던져진다
        public void logic() {
            repository.call();
            networkClient.call();
        }
    }

    static class NetworkClient { // 외부 서버와 통신하는 곳 (내가 Client 가 됨)
        public void call() {
            throw new RuntimeConnectionException("연결 실패");
        }
    }

    static class Repository {
        public void call() { // 차피 SQLEXception 은 JDBC 쓰면 무조건 발생하게 되어 있음
            try {
                runSql();
            } catch (SQLException e) {
                throw new RuntimeSqlException(e); // 바꿔서 던져줄거임 // 이렇게 이전 예외 넣어줄거면 근데 생성자 새로해야함 Throwable 로
            }
        }

        private void runSql() throws SQLException {
            throw new SQLException("예외 발생");
        }
    }

    static class RuntimeConnectionException extends RuntimeException {
        public RuntimeConnectionException(String message) {
            super(message);
        }
    }

    static class RuntimeSqlException extends RuntimeException {
        public RuntimeSqlException(String message) {
            super(message);
        }

        // 이전 Exception 을 변환하여 던지고 싶을 경우에 해당 Constructor 를 가져가야 한다
        public RuntimeSqlException(Throwable cause) {
            super(cause);
        }
    }
}
