package asaintsever.httpsinkconnector.http.authentication;

public class AuthException extends Exception {

    private static final long serialVersionUID = 1587681686134707870L;
    
    
    public AuthException(Exception e) {
        super(e);
    }
    
    public AuthException(String msg) {
        super(msg);
    }
    
    public AuthException(String msg, Exception e) {
        super(msg, e);
    }
    
}
