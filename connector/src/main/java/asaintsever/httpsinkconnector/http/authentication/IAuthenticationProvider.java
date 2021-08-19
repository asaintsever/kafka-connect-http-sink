package asaintsever.httpsinkconnector.http.authentication;

import asaintsever.httpsinkconnector.config.IConfigAccessor;

public interface IAuthenticationProvider extends IConfigAccessor {

    String[] addAuthentication() throws AuthException;
}
