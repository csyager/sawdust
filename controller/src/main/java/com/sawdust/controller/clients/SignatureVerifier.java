package com.sawdust.controller.clients;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class SignatureVerifier {
    public static boolean verifySignature(
            final String requestBody,
            final String base64Signature,
            final String publicKeyStr
    ) {
        try {
            // decode public key
            PublicKey publicKey = loadPublicKey(publicKeyStr);

            // decode base64 signature
            byte[] signatureBytes = Base64.getDecoder().decode(base64Signature);

            // Initialize verifier
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(publicKey);
            verifier.update(requestBody.getBytes(StandardCharsets.UTF_8));

            // Verify signature
            return verifier.verify(signatureBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static PublicKey loadPublicKey(String publicKeyStr) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }
}
