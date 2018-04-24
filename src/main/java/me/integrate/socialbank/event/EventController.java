package me.integrate.socialbank.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
public class EventController {
    private EventService eventService;

    @Autowired
    public EventController(EventService eventService) { this.eventService = eventService; }

    @GetMapping("/events/{id}")
    public Event getEventById(@PathVariable int id) {
        return eventService.getEventById(id);
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public Event saveEvent(@RequestBody Event event, Authentication authentication) {
        event.setCreatorEmail(authentication.getName());
        if (event.getIniDate().after(event.getEndDate()) || event.getIniDate().before(new Date())) throw new EventWithIncorrectDateException();
        return eventService.saveEvent(event);
    }

    @GetMapping("/events")
    public @ResponseBody List<Event> getEvents() {
        return eventService.getEvents();
    }

}
