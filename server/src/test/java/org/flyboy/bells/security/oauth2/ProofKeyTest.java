package org.flyboy.bells.security.oauth2;

import org.junit.jupiter.api.Test;

/**
 * @author John J. Franey
 */
public class ProofKeyTest {

    @Test
    public void testLegal() {
        String legalString = "-.~_"
                + "0123456789"
                + "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "abcdefghijklmnopqrstuvwxyz";
        ProofKey pkce = ProofKey.generate();
        System.out.println(pkce);
    }
}
