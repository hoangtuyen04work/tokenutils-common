package com.utils.token;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import com.nimbusds.jwt.SignedJWT;

public class TokenUtils {
    private final String signerKey;
    public TokenUtils (String signerKey) {
        this.signerKey = signerKey;
    }

    public String getUserIdByToken(String token) throws ParseException {
        SignedJWT signedJWT;
        try {
            signedJWT = SignedJWT.parse(token);
        } catch (ParseException e) {
            return null;
        }
        return signedJWT.getJWTClaimsSet().getSubject();
    }


    public boolean checkToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(signerKey.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        boolean verified = signedJWT.verify(verifier);
        return verified && expiryTime.after(new Date());
    }

    public String generateToken(String userId, List<String> roles) throws JOSEException {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet;
        jwtClaimsSet = new JWTClaimsSet.Builder()
                .issuer("hoangtuyen.com")
                .subject(userId)
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plus(24*60*60, ChronoUnit.SECONDS)))
                .jwtID(UUID.randomUUID().toString())
                .claim("roles",buildRoles(roles))
                .build();
        JWSObject jwsObject = new JWSObject(jwsHeader, new Payload(jwtClaimsSet.toJSONObject()));
        jwsObject.sign(new MACSigner(signerKey.getBytes()));
        String token =  jwsObject.serialize();
        return token;
    }

    private List<String> buildRoles(List<String> roles) {
        return roles;
    }
}

