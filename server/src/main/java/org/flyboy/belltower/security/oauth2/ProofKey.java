package org.flyboy.belltower.security.oauth2;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * values for Proof Key for Code Exchange (PKCE)
 *
 * @param verifier of PKCE
 * @param challenge of PKCE
 * @param method of PKCE
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7636">RFC7636</a>
 * @author John J. Franey
 */
public record ProofKey(String verifier, String challenge, String method) {


    private ProofKey(byte[] verifier) {
        this(new String(verifier), createChallenge(verifier), "S256");
    }

    /**
     * generate values for the Proof Key.
     */
    public ProofKey() {
        this(createVerifier());
    }

    private static String createChallenge(byte[] verifier) {
        // encode the verifier with SHA-256
        MessageDigest messageDigest;
        final String algorithm = "SHA-256";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Message digest fails to find algorithm: " + algorithm);
        }
        messageDigest.update(verifier, 0, verifier.length);
        byte[] challengeBytes =  messageDigest.digest();
        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(challengeBytes);
    }

    private static byte[] createVerifier() {
        //   RFC 7636
        //   code-verifier = 43*128unreserved
        //   unreserved = ALPHA / DIGIT / "-" / "." / "_" / "~"
        //   ALPHA = %x41-5A / %x61-7A
        //   DIGIT = %x30-39
        //
        //   length: 43 - 129 characters

        String legalString = "-.~_"
                + "0123456789"
                + "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "abcdefghijklmnopqrstuvwxyz";
        byte[] legalBytes = legalString.getBytes();

        final Random generator = new Random();
        final int length = generator.nextInt(43, 129);
        byte[] codeVerifierBytes = new byte[length];
        IntStream.range(0, length).forEach(i -> {
            int offset = generator.nextInt(0, legalBytes.length);
            codeVerifierBytes[i] = legalBytes[offset];
        });
        return codeVerifierBytes;
    }

    public static ProofKey generate() {
        return new ProofKey();
    }
}
