package me.integrate.socialbank.event;

import me.integrate.socialbank.user.UserRepository;
import me.integrate.socialbank.user.UserTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static me.integrate.socialbank.event.EventTestUtils.createEvent;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ExtendWith(SpringExtension.class)
public class EventServiceTest {

    @Autowired
    EventService eventService;

    @Autowired
    UserRepository userRepository;

    @Test
    void givenEventWhenSaveItThenReturnsSameEvent() {

        String email = "pepito@pepito.com";
        userRepository.saveUser(UserTestUtils.createUser(email));
        Event event = createEvent(email);
        Event savedEvent = eventService.saveEvent(event);

        assertEquals(event, savedEvent);
    }

    @Test
    void givenEventsWhenSaveItThenReturnListOfThem() {
        String email = "pepito@pepito.com";
        userRepository.saveUser(UserTestUtils.createUser(email));
        Event event = eventService.saveEvent(createEvent(email));
        Event event2 = eventService.saveEvent(createEvent(email));

        List<Event> returnList = eventService.getAllEvents();

        assertTrue(returnList.contains(event));
        assertTrue(returnList.contains(event2));
    }

    @Test
    void givenEventsOfSameCategoryWhenGetByCategoryThenReturnsBoth() {
        String email = "pepito@pepito.com";
        userRepository.saveUser(UserTestUtils.createUser(email));
        Event event = eventService.saveEvent(createEvent(email, Category.CULTURE));
        Event event2 = eventService.saveEvent(createEvent(email, Category.CULTURE));

        List<Event> events = eventService.getEventsByCategory(Category.CULTURE);

        assertTrue(events.contains(event));
        assertTrue(events.contains(event2));
    }

    @Test
    void givenEventsOfDifferentCategoriesWhenGetByCategoryThenReturnsOnlyOne() {
        String email = "pepito@pepito.com";
        userRepository.saveUser(UserTestUtils.createUser(email));
        Event event = eventService.saveEvent(createEvent(email, Category.CULTURE));
        Event event2 = eventService.saveEvent(createEvent(email, Category.GASTRONOMY));

        List<Event> events = eventService.getEventsByCategory(Category.CULTURE);

        assertTrue(events.contains(event));
        assertFalse(events.contains(event2));
    }

    @Test
    void givenEventsOfDifferentCategoriesWhenGetByAnotherCategoryThenDoesntReturnSameEvents() {
        String email = "pepito@pepito.com";
        userRepository.saveUser(UserTestUtils.createUser(email));
        Event event = eventService.saveEvent(createEvent(email, Category.CULTURE));
        Event event2 = eventService.saveEvent(createEvent(email, Category.GASTRONOMY));

        List<Event> events = eventService.getEventsByCategory(Category.LEISURE);

        assertFalse(events.contains(event));
        assertFalse(events.contains(event2));
    }

    @Test
    void givenEventWithoutDateWhenSaveItThenReturnsSameEvent() {

        String email = "pepito@pepito.com";
        userRepository.saveUser(UserTestUtils.createUser(email));
        Event event = createEvent(email, null, null);
        Event savedEvent = eventService.saveEvent(event);

        assertEquals(event, savedEvent);
    }

    @Test
    void givenStoredEventWhenDeletedThenIsNoLongerStored() {
        String email = "pepito@pepito.com";
        userRepository.saveUser(UserTestUtils.createUser(email));
        int savedEventId = eventService.saveEvent(createEvent(email)).getId();
        eventService.deleteEvent(savedEventId);
        assertThrows(EventNotFoundException.class, () -> eventService.getEventById(savedEventId));
    }

    @Test
    void givenDifferentEventsStoredInDatabaseWhenDeletedOneThenTheOtherIsStillStored() {
        String email = "email@email.tld";
        userRepository.saveUser(UserTestUtils.createUser(email));
        Event eventOne = eventService.saveEvent(EventTestUtils.createEvent(email));
        Event eventTwo = eventService.saveEvent(EventTestUtils.createEvent(email));
        eventService.deleteEvent(eventOne.getId());
        assertEquals(eventTwo, eventService.getEventById(eventTwo.getId()));
        assertThrows(EventNotFoundException.class, () -> eventService.getEventById(eventOne.getId()));
    }

    @Test
    void givenStoredEventsWhenDeleteIsTooLateThenThrowException() {
        String email = "pepito@pepito.com";
        userRepository.saveUser(UserTestUtils.createUser(email));

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.HOUR_OF_DAY, 1);
        Date iniDate = cal.getTime();

        Date endDate = cal.getTime();

        final int EventIdOne = eventService.saveEvent(createEvent("pepito@pepito.com", iniDate, endDate)).getId();
        assertThrows(TooLateException.class, () -> eventService.deleteEvent(EventIdOne));

        iniDate = new Date(cal.getTimeInMillis() + (4 * 60000));
        endDate = new Date(cal.getTimeInMillis() + (5 * 60000));

        final int EventIdTwo = eventService.saveEvent(createEvent("pepito@pepito.com", iniDate, endDate)).getId();
        assertThrows(TooLateException.class, () -> eventService.deleteEvent(EventIdTwo));
    }

    @Test
    void givenStoredEventsWhenRetrievedByCategoryAndTagsTheyAreReturned() {
        String email = "pepito@pepito.com";
        userRepository.saveUser(UserTestUtils.createUser(email));

        Event event = createEvent(email, Category.CULTURE);
        event.setTags(Arrays.asList("Funny", "Boring"));
        eventService.saveEvent(event);
        Event event2 = createEvent(email, Category.GASTRONOMY);
        event2.setTags(Arrays.asList("Funny", "Scary"));
        eventService.saveEvent(event2);

        assertTrue(eventService.getEvents(Category.CULTURE, Collections.singletonList("Funny")).contains(event));
        assertFalse(eventService.getEvents(Category.CULTURE, Collections.singletonList("Funny")).contains(event2));
    }

    @Test
    void givenStoredEventsWhenRetrievedWithoutParamsTheyAreReturned() {
        String email = "pepito@pepito.com";
        userRepository.saveUser(UserTestUtils.createUser(email));

        Event event = createEvent(email, Category.CULTURE);
        event.setTags(Arrays.asList("Funny", "Boring"));
        eventService.saveEvent(event);
        Event event2 = createEvent(email, Category.GASTRONOMY);
        event2.setTags(Arrays.asList("Funny", "Scary"));
        eventService.saveEvent(event2);

        assertTrue(eventService.getEvents(null, null).contains(event));
        assertTrue(eventService.getEvents(null, null).contains(event2));
    }

    @Test
    void givenStoredEventsWhenRetrievedByCategoryTheyAreReturned() {
        String email = "pepito@pepito.com";
        userRepository.saveUser(UserTestUtils.createUser(email));

        Event event = createEvent(email, Category.CULTURE);
        event.setTags(Arrays.asList("Funny", "Boring"));
        eventService.saveEvent(event);
        Event event2 = createEvent(email, Category.GASTRONOMY);
        event2.setTags(Arrays.asList("Funny", "Scary"));
        eventService.saveEvent(event2);

        assertTrue(eventService.getEvents(Category.CULTURE, null).contains(event));
        assertFalse(eventService.getEvents(Category.CULTURE, null).contains(event2));
    }

    @Test
    void givenStoredEventsWhenRetrievedByTagsTheyAreReturned() {
        String email = "pepito@pepito.com";
        userRepository.saveUser(UserTestUtils.createUser(email));

        Event event = createEvent(email, Category.CULTURE);
        event.setTags(Arrays.asList("Funny", "Boring"));
        eventService.saveEvent(event);
        Event event2 = createEvent(email, Category.GASTRONOMY);
        event2.setTags(Arrays.asList("Funny", "Scary"));
        eventService.saveEvent(event2);

        assertTrue(eventService.getEvents(null, Collections.singletonList("Funny")).contains(event));
        assertTrue(eventService.getEvents(null, Collections.singletonList("Funny")).contains(event2));
        assertTrue(eventService.getEvents(null, Collections.singletonList("Boring")).contains(event));
        assertFalse(eventService.getEvents(null, Collections.singletonList("Boring")).contains(event2));
    }
}
