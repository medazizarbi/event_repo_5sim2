package tn.esprit.eventsproject.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.entities.Tache;
import tn.esprit.eventsproject.repositories.EventRepository;
import tn.esprit.eventsproject.repositories.LogisticsRepository;
import tn.esprit.eventsproject.repositories.ParticipantRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
@Slf4j
@RequiredArgsConstructor
@Service
public class EventServicesImpl implements IEventServices{

    private static final Logger log = LogManager.getLogger(EventServicesImpl.class);


    private final EventRepository eventRepository;
    private final ParticipantRepository participantRepository;
    private final LogisticsRepository logisticsRepository;

    @Override
    public Participant addParticipant(Participant participant) {
        log.info("Adding a new Participant: {}", participant);
        Participant savedParticipant = participantRepository.save(participant);
        log.info("Participant added with ID: {}", savedParticipant.getIdPart());
        return savedParticipant;
    }

    @Override
    public Event addAffectEvenParticipant(Event event, int idParticipant) {
        log.info("Associating Event '{}' with Participant ID: {}", event.getDescription(), idParticipant);
        Participant participant = participantRepository.findById(idParticipant).orElse(null);

        if (participant == null) {
            log.warn("Participant with ID '{}' not found!", idParticipant);
            return null;
        }

        if (participant.getEvents() == null) {
            participant.setEvents(new HashSet<>());
        }
        participant.getEvents().add(event);

        Event savedEvent = eventRepository.save(event);
        log.info("Event '{}' associated successfully with Participant ID: {}", event.getDescription(), idParticipant);
        return savedEvent;
    }

    @Override
    public Event addAffectEvenParticipant(Event event) {
        log.info("Associating Event '{}' with multiple Participants", event.getDescription());
        Set<Participant> participants = event.getParticipants();

        for (Participant aParticipant : participants) {
            Participant participant = participantRepository.findById(aParticipant.getIdPart()).orElse(null);

            if (participant == null) {
                log.warn("Participant with ID '{}' not found!", aParticipant.getIdPart());
                continue;
            }

            if (participant.getEvents() == null) {
                participant.setEvents(new HashSet<>());
            }
            participant.getEvents().add(event);
        }

        Event savedEvent = eventRepository.save(event);
        log.info("Event '{}' associated successfully with Participants", event.getDescription());
        return savedEvent;
    }

    @Override
    public Logistics addAffectLog(Logistics logistics, String descriptionEvent) {
        log.info("Associating Logistics '{}' with Event '{}'", logistics, descriptionEvent);
        Event event = eventRepository.findByDescription(descriptionEvent);

        if (event == null) {
            log.warn("Event with description '{}' not found!", descriptionEvent);
            return null;
        }

        if (event.getLogistics() == null) {
            event.setLogistics(new HashSet<>());
        }
        event.getLogistics().add(logistics);
        eventRepository.save(event);

        Logistics savedLogistics = logisticsRepository.save(logistics);
        log.info("Logistics '{}' associated successfully with Event '{}'", savedLogistics, descriptionEvent);
        return savedLogistics;
    }

    @Override
    public List<Logistics> getLogisticsDates(LocalDate date_debut, LocalDate date_fin) {
        log.info("Fetching Logistics between dates {} and {}", date_debut, date_fin);
        List<Event> events = eventRepository.findByDateDebutBetween(date_debut, date_fin);

        if (events.isEmpty()) {
            log.warn("No Events found between {} and {}", date_debut, date_fin);
            return new ArrayList<>();
        }

        List<Logistics> logisticsList = new ArrayList<>();
        for (Event event : events) {
            if (event.getLogistics() != null) {
                for (Logistics logistics : event.getLogistics()) {
                    if (logistics.isReserve()) {
                        logisticsList.add(logistics);
                        log.debug("Added Logistics: {}", logistics);
                    }
                }
            }
        }
        log.info("Found {} Logistics between the given dates.", logisticsList.size());
        return logisticsList;
    }

    @Scheduled(cron = "*/60 * * * * *")
    @Override
    public void calculCout() {
        log.info("Starting cost calculation for Events.");
        List<Event> events = eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
                "Tounsi", "Ahmed", Tache.ORGANISATEUR);

        for (Event event : events) {
            log.info("Calculating cost for Event '{}'", event.getDescription());
            float totalCost = 0f;

            if (event.getLogistics() != null) {
                for (Logistics logistics : event.getLogistics()) {
                    if (logistics.isReserve()) {
                        totalCost += logistics.getPrixUnit() * logistics.getQuantite();
                        log.debug("Added Logistics cost: {}", logistics.getPrixUnit() * logistics.getQuantite());
                    }
                }
            }

            event.setCout(totalCost);
            eventRepository.save(event);
            log.info("Total cost for Event '{}' is {}", event.getDescription(), totalCost);
        }
        log.info("Cost calculation completed.");
    }

}
