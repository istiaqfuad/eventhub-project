package org.istiaqfuad.eventhub.venue.dto;

import java.util.List;

public record VenueLayoutResponse(
        VenueResponse venue,
        List<SectionWithSeatsResponse> sections
) {
    public record SectionWithSeatsResponse(
            SectionResponse section,
            List<SeatResponse> seats
    ) {}
}
