package org.flyboy.bells.ring;

import io.quarkus.test.junit.QuarkusTest;
import org.flyboy.bells.security.oauth2.TokenService;

import javax.inject.Inject;

/**
 * @author John J. Franey
 */
@QuarkusTest
public class TokenServiceTest {


    @Inject
    TokenService tokenService;

}
