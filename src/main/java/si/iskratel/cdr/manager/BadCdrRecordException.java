package si.iskratel.cdr.manager;


public class BadCdrRecordException extends Exception {

  private static final long serialVersionUID = 1235607131844650684L;

  public BadCdrRecordException() {
    super();
  }

  public BadCdrRecordException(Exception ioEx) {
    super(ioEx);
  }

  public BadCdrRecordException(String s) {
    super(s);
  }

  public BadCdrRecordException(String message, Throwable cause) {
    super(message, cause);
  }
}
