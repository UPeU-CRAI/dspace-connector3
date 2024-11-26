import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.protocol.HttpClientContext;

public class AuthManager {

    private String jwtToken;
    private long tokenExpirationTime;
    private String csrfToken;
    private final Object lock = new Object();
    private final BasicCookieStore cookieStore;
    private final HttpClientContext httpClientContext;
    private final String baseUrl;
    private final String username;
    private final String password;

    public AuthManager(String baseUrl, String username, String password) {
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
        this.cookieStore = new BasicCookieStore();
        this.httpClientContext = HttpClientContext.create();
        this.httpClientContext.setCookieStore(cookieStore);
    }

    public HttpClientContext getContext() {
        return httpClientContext;
    }

    private String obtainCsrfToken() {
        // Implementación existente
    }

    private String obtainJwtToken() {
        // Implementación existente
    }

    public String getJwtToken() {
        synchronized (lock) {
            if (jwtToken == null || System.currentTimeMillis() > tokenExpirationTime) {
                jwtToken = obtainJwtToken();
            }
            return jwtToken;
        }
    }

    public void addAuthenticationHeaders(HttpUriRequestBase request) {
        getJwtToken(); // Asegúrate de que el token sea válido
        request.addHeader("Authorization", "Bearer " + jwtToken);
        request.addHeader("X-XSRF-TOKEN", csrfToken);
        request.addHeader("Content-Type", "application/json");
    }

    public void renewAuthentication() {
        synchronized (lock) {
            jwtToken = null;
            csrfToken = null;
            obtainJwtToken();
        }
    }

    public boolean isAuthenticated() {
        return jwtToken != null && System.currentTimeMillis() < tokenExpirationTime;
    }
}
