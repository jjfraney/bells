package org.flyboy.bells.ring;

import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author John J. Franey
 */
public class RingRequestMultiRepositoryTest {

    RingRequestMultiRepository ringRequestMultiRepository;
    RingRequestRepository mockGoogleRingRequestRepository;

    @BeforeEach
    public void beforeEach() {
        ringRequestMultiRepository = new RingRequestMultiRepository();
        mockGoogleRingRequestRepository = Mockito.mock(RingRequestRepository.class);
        ringRequestMultiRepository.googleRepository = mockGoogleRingRequestRepository;
    }

    @Test
    public void testSort() {
        RingRequest earlier = new RingRequest(ZonedDateTime.now().minusSeconds(30), "earlier.ogg");
        RingRequest later = new RingRequest(ZonedDateTime.now().plusSeconds(30), "later.ogg");

        List<RingRequest> queryResult = new ArrayList<>(List.of(later, earlier));
        // return list in reverse chronological order
        Mockito.when(mockGoogleRingRequestRepository.getRequests()).thenReturn(Uni.createFrom().item(queryResult));

        List<RingRequest> expected = List.of(earlier, later);
        ringRequestMultiRepository.getRequests().subscribe().with(
                bellEvents -> Assertions.assertEquals(expected, bellEvents)
        );
    }
}
