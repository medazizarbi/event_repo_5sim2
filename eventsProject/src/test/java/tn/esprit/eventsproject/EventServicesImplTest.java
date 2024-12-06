package tn.esprit.eventsproject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.entities.Tache;
import tn.esprit.eventsproject.repositories.EventRepository;
import tn.esprit.eventsproject.repositories.LogisticsRepository;
import tn.esprit.eventsproject.repositories.ParticipantRepository;
import tn.esprit.eventsproject.services.EventServicesImpl;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventServicesImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private LogisticsRepository logisticsRepository;

    @InjectMocks
    private EventServicesImpl eventServices;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddParticipant() {
        // Arrange
        Participant participant = new Participant();
        participant.setIdPart(1);
        when(participantRepository.save(participant)).thenReturn(participant);

        // Act
        Participant result = eventServices.addParticipant(participant);

        // Assert
        assertNotNull(result);
        verify(participantRepository, times(1)).save(participant);
    }

    @Test
    void testAddAffectEvenParticipantWithId() {
        // Arrange
        Participant participant = new Participant();
        participant.setEvents(new HashSet<>());
        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));

        Event event = new Event();
        event.setDescription("Event with ID");
        when(eventRepository.save(event)).thenReturn(event);

        // Act
        Event result = eventServices.addAffectEvenParticipant(event, 1);

        // Assert
        assertNotNull(result);
        verify(participantRepository, times(1)).findById(1);
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void testAddAffectEvenParticipantWithoutId() {
        // Arrange
        Participant participant = new Participant();
        participant.setIdPart(1);
        participant.setEvents(new HashSet<>());

        Event event = new Event();
        Set<Participant> participants = new HashSet<>();
        participants.add(participant);
        event.setParticipants(participants);

        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        when(eventRepository.save(event)).thenReturn(event);

        // Act
        Event result = eventServices.addAffectEvenParticipant(event);

        // Assert
        assertNotNull(result);
        verify(participantRepository, times(1)).findById(1);
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void testAddAffectLog() {
        // Arrange
        Event event = new Event();
        event.setLogistics(new HashSet<>());
        when(eventRepository.findByDescription("Event 1")).thenReturn(event);

        Logistics logistics = new Logistics();
        logistics.setIdLog(1);

        when(logisticsRepository.save(logistics)).thenReturn(logistics);

        // Act
        Logistics result = eventServices.addAffectLog(logistics, "Event 1");

        // Assert
        assertNotNull(result);
        verify(eventRepository, times(1)).findByDescription("Event 1");
        verify(logisticsRepository, times(1)).save(logistics);
    }

    @Test
    void testGetLogisticsDates() {
        // Arrange
        Event event = new Event();
        event.setLogistics(new HashSet<>(Collections.singletonList(new Logistics(true, 100, 2))));

        when(eventRepository.findByDateDebutBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(event));

        // Act
        List<Logistics> result = eventServices.getLogisticsDates(LocalDate.now().minusDays(1), LocalDate.now());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(eventRepository, times(1)).findByDateDebutBetween(any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void testCalculCout() {
        // Arrange
        Event event = new Event();
        event.setDescription("Event Test");
        event.setLogistics(new HashSet<>(Collections.singletonList(new Logistics(true, 50, 2))));

        when(eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
                "Tounsi", "Ahmed", Tache.ORGANISATEUR)).thenReturn(Collections.singletonList(event));
        when(eventRepository.save(event)).thenReturn(event);

        // Act
        eventServices.calculCout();

        // Assert
        verify(eventRepository, times(1)).findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
                "Tounsi", "Ahmed", Tache.ORGANISATEUR);
        verify(eventRepository, times(1)).save(event);
    }
}
