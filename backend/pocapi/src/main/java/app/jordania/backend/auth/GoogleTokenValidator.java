package app.jordania.backend.auth;

import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Date;

@Service
public class GoogleTokenValidator {

    private static final String GOOGLE_JWKS_URL = "https://www.googleapis.com/oauth2/v3/certs";

    public GoogleTokenClaims validate(String idToken) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(idToken);

            JWKSet jwkSet = JWKSet.load(new URL(GOOGLE_JWKS_URL));
            String keyId = signedJWT.getHeader().getKeyID();

            RSAKey rsaKey = (RSAKey) jwkSet.getKeyByKeyId(keyId);
            if (rsaKey == null) {
                throw new IllegalArgumentException("Google public key not found for kid: " + keyId);
            }

            JWSVerifier verifier = new RSASSAVerifier(rsaKey);
            if (!signedJWT.verify(verifier)) {
                throw new IllegalArgumentException("Google token signature is invalid");
            }

            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expiration == null || expiration.before(new Date())) {
                throw new IllegalArgumentException("Google token is expired");
            }

            String sub = signedJWT.getJWTClaimsSet().getSubject();
            String email = signedJWT.getJWTClaimsSet().getStringClaim("email");
            String name = signedJWT.getJWTClaimsSet().getStringClaim("name");

            return new GoogleTokenClaims(sub, email, name);

        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to validate Google token: " + e.getMessage(), e);
        }
    }

    public record GoogleTokenClaims(String sub, String email, String name) {}
}