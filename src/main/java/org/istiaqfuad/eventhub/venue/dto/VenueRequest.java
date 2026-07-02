package org.istiaqfuad.eventhub.venue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.istiaqfuad.eventhub.venue.entity.LayoutType;

public record VenueRequest(
        @NotBlank(message = "venue name is required")
        @Size(max = 255, message = "venue name must not exceed 255 characters")
        String name,

        @NotNull(message = "layout type is required")
        LayoutType layoutType,

        @Size(max = 255, message = "address must not exceed 255 characters")
        String address,

        @Size(max = 120, message = "city must not exceed 120 characters")
        String city
) {
}
