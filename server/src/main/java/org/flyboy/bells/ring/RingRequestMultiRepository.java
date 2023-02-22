package org.flyboy.bells.ring;

import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Comparator;
import java.util.List;

/**
 * queries all existing bell event repositories
 * to return soonest events.
 *
 * @author John J. Franey
 */
@ApplicationScoped
public class RingRequestMultiRepository {

    private final Comparator<RingRequest> ringRequestComparator = Comparator.comparing(RingRequest::dateTime);
    @Inject
    @Named("Google")
    RingRequestRepository googleRepository;

    public Uni<List<RingRequest>> getRequests() {
        return googleRepository.getRequests().map(requests -> {
            return requests.stream().sorted(ringRequestComparator).toList();
        });
    }

}
