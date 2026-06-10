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
public class AppleTokenValidator {

    private static final String APPLE_JWKS_URL = "https://appleid.apple.com/auth/keys";

    public AppleTokenClaims validate(String identityToken) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(identityToken);

            JWKSet jwkSet = JWKSet.load(new URL(APPLE_JWKS_URL));
            String keyId = signedJWT.getHeader().getKeyID();

            RSAKey rsaKey = (RSAKey) jwkSet.getKeyByKeyId(keyId);
            if (rsaKey == null) {
                throw new IllegalArgumentException("Apple public key not found for kid: " + keyId);
            }

            JWSVerifier verifier = new RSASSAVerifier(rsaKey);
            if (!signedJWT.verify(verifier)) {
                throw new IllegalArgumentException("Apple token signature is invalid");
            }

            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expiration == null || expiration.before(new Date())) {
                throw new IllegalArgumentException("Apple token is expired");
            }

            String sub = signedJWT.getJWTClaimsSet().getSubject();
            String email = signedJWT.getJWTClaimsSet().getStringClaim("email");

            return new AppleTokenClaims(sub, email);

        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to validate Apple token: " + e.getMessage(), e);
        }
    }

    public record AppleTokenClaims(String sub, String email) {}
}