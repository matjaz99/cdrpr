package si.iskratel.cdr.manager;

public class CdrException extends RuntimeException {

  private static final long serialVersionUID = -8542440268458250982L;

  public CdrException() {
  }

  public CdrException(String message) {
    super(message);
  }

  public CdrException(Throwable cause) {
    super(cause);
  }

  public CdrException(String message, Throwable cause) {
    super(message, cause);
  }

}
