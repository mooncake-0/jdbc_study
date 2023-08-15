package springdb.jdbc_study.repository.exception;

public class MyDbDuplicateKeyException extends MyDbException{ // 내가 한 카테고리 화


    public MyDbDuplicateKeyException() {
    }

    public MyDbDuplicateKeyException(String message) {
        super(message);
    }

    public MyDbDuplicateKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyDbDuplicateKeyException(Throwable cause) {
        super(cause);
    }
}
