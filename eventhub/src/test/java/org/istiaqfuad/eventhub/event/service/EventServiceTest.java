package org.istiaqfuad.eventhub.event.service;

import org.istiaqfuad.eventhub.event.dto.EventRequest;
import org.istiaqfuad.eventhub.event.dto.EventResponse;
import org.istiaqfuad.eventhub.event.repository.CategoryRepository;
import org.istiaqfuad.eventhub.event.repository.EventRepository;
import org.istiaqfuad.eventhub.event.repository.TagRepository;
import org.istiaqfuad.eventhub.security.web.AuthenticatedUser;
import org.istiaqfuad.eventhub.user.entity.Organizer;
import org.istiaqfuad.eventhub.user.repository.OrganizerRepository;
import org.istiaqfuad.eventhub.venue.repository.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The owning organizer on {@link EventService#create} is derived from the caller,
 * never trusted from the request body — except an ADMIN acting on another's behalf.
 */
class EventServiceTest {

    private static final long CALLER_ID = 20L;

    private EventRepository events;
    private OrganizerRepository organizers;
    private EventService service;

    @BeforeEach
    void setUp() {
        events = mock(EventRepository.class);
        organizers = mock(OrganizerRepository.class);
        service = new EventService(events, organizers,
                mock(CategoryRepository.class), mock(VenueRepository.class), mock(TagRepository.class), mock(org.istiaqfuad.eventhub.event.repository.TicketTypeRepository.class));
        when(events.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private static Organizer organizer(long id) {
        Organizer o = new Organizer();
        o.setId(id);
        return o;
    }

    private static EventRequest request(Long organizerId) {
        return new EventRequest(organizerId, "Title", null, null, null, null, null, null,
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(2), null, null);
    }

    private static AuthenticatedUser user(long id, String... roles) {
        return new AuthenticatedUser(id, Set.of(roles));
    }

    @Test
    void nonAdminUsesOwnOrganizerProfile() {
        when(organizers.findByUserId(CALLER_ID)).thenReturn(Optional.of(organizer(500L)));

        EventResponse res = service.create(request(null), user(CALLER_ID));

        assertThat(res.organizerId()).isEqualTo(500L);
        verify(organizers, never()).getReferenceById(anyLong());
    }

    @Test
    void nonAdminWithoutOrganizerProfileIsDenied() {
        when(organizers.findByUserId(CALLER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request(null), user(CALLER_ID)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void nonAdminCannotSpoofOrganizerViaRequestBody() {
        when(organizers.findByUserId(CALLER_ID)).thenReturn(Optional.of(organizer(500L)));

        // Body claims organizer 999, but the caller's own profile (500) must win.
        EventResponse res = service.create(request(999L), user(CALLER_ID));

        assertThat(res.organizerId()).isEqualTo(500L);
        verify(organizers, never()).getReferenceById(anyLong());
    }

    @Test
    void adminMayActForAnotherOrganizerViaRequestBody() {
        when(organizers.getReferenceById(777L)).thenReturn(organizer(777L));

        EventResponse res = service.create(request(777L), user(99L, "ADMIN"));

        assertThat(res.organizerId()).isEqualTo(777L);
        verify(organizers, never()).findByUserId(anyLong());
    }
}
