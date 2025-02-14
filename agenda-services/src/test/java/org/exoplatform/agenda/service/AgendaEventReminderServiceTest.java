/*
 * Copyright (C) 2020 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
*/
package org.exoplatform.agenda.service;

import static org.junit.Assert.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import org.exoplatform.commons.api.notification.service.WebNotificationService;
import org.junit.Test;

import org.exoplatform.agenda.constant.*;
import org.exoplatform.agenda.model.*;


public class AgendaEventReminderServiceTest extends BaseAgendaEventTest {

  @Test
  public void testSaveEventReminders() throws Exception { // NOSONAR
    ZonedDateTime start = ZonedDateTime.now().withNano(0);

    boolean allDay = false;

    Event event = newEventInstance(start, start, allDay);
    event = createEvent(event.clone(), Long.parseLong(testuser1Identity.getId()), testuser5Identity);

    long eventId = event.getId();
    long userIdentityId = Long.parseLong(testuser1Identity.getId());
    List<EventReminder> eventReminders = agendaEventReminderService.getEventReminders(eventId, userIdentityId);
    assertNotNull(eventReminders);
    assertEquals(1, eventReminders.size());

    EventReminder eventReminder = eventReminders.get(0);
    assertNotNull(eventReminder);

    eventReminder = eventReminder.clone();
    eventReminder.setId(0);
    eventReminder.setBefore(eventReminder.getBefore() + 1);
    eventReminders.add(eventReminder);

    agendaEventReminderService.saveEventReminders(event, eventReminders, userIdentityId);
    eventReminders = agendaEventReminderService.getEventReminders(eventId, userIdentityId);
    assertNotNull(eventReminders);
    assertEquals(2, eventReminders.size());

    agendaEventReminderService.saveEventReminders(event, Collections.emptyList(), userIdentityId);
    eventReminders = agendaEventReminderService.getEventReminders(eventId, userIdentityId);
    assertNotNull(eventReminders);
    assertEquals(0, eventReminders.size());
  }

  @Test
  public void testUpdateEventStatus() throws Exception { // NOSONAR
    ZonedDateTime start = ZonedDateTime.now().withNano(0);

    boolean allDay = false;
    long userIdentityId = Long.parseLong(testuser1Identity.getId());

    Event event = newEventInstance(start, start, allDay);
    event = createEvent(event.clone(), userIdentityId, testuser5Identity);

    long eventId = event.getId();
    List<EventReminder> eventReminders = agendaEventReminderService.getEventReminders(eventId, userIdentityId);
    assertNotNull(eventReminders);
    assertEquals(1, eventReminders.size());
    assertNotNull(eventReminders.get(0).getDatetime());

    List<EventReminder> origEventReminders = new ArrayList<>(eventReminders);

    List<EventAttendee> attendees = agendaEventAttendeeService.getEventAttendees(eventId).getEventAttendees();

    event.setStatus(EventStatus.CANCELLED);
    event = agendaEventService.updateEvent(event,
                                           attendees,
                                           null,
                                           origEventReminders,
                                           null,
                                           REMOTE_EVENT,
                                           allDay,
                                           userIdentityId);
    eventReminders = agendaEventReminderService.getEventReminders(eventId, userIdentityId);
    assertNotNull(eventReminders);
    assertEquals(0, eventReminders.size());

    event.setStatus(EventStatus.TENTATIVE);
    event = agendaEventService.updateEvent(event,
                                           attendees,
                                           null,
                                           origEventReminders,
                                           null,
                                           null,
                                           false,
                                           userIdentityId);
    eventReminders = agendaEventReminderService.getEventReminders(eventId, userIdentityId);
    assertNotNull(eventReminders);
    assertEquals(1, eventReminders.size());
    assertNull(eventReminders.get(0).getDatetime());

    event.setStatus(EventStatus.CONFIRMED);
    agendaEventService.updateEvent(event,
                                   attendees,
                                   Collections.emptyList(),
                                   origEventReminders,
                                   null,
                                   null,
                                   false,
                                   userIdentityId);
    eventReminders = agendaEventReminderService.getEventReminders(eventId, userIdentityId);
    assertNotNull(eventReminders);
    assertEquals(1, eventReminders.size());
    assertNotNull(eventReminders.get(0).getDatetime());
  }

  @Test
  public void testSaveRecurrentEventReminders() throws Exception { // NOSONAR
    ZonedDateTime start = getDate().withNano(0);

    boolean allDay = true;

    Event event = newEventInstance(start, start, allDay);
    EventRecurrence recurrence = new EventRecurrence(0,
                                                     start.plusDays(2).toLocalDate(),
                                                     0,
                                                     EventRecurrenceType.DAILY,
                                                     EventRecurrenceFrequency.DAILY,
                                                     1,
                                                     null,
                                                     null,
                                                     null,
                                                     null,
                                                     null,
                                                     null,
                                                     null,
                                                     null,
                                                     null,
                                                     null,
                                                     null);
    event.setRecurrence(recurrence);

    event = createEvent(event.clone(), Long.parseLong(testuser1Identity.getId()), testuser1Identity, testuser2Identity);

    long eventId = event.getId();

    try {
      agendaEventService.saveEventExceptionalOccurrence(eventId, start.plusDays(4));
      fail("Shouldn't be able to create an exceptional occurrence out of range");
    } catch (Exception e) {
      // Expected
    }

    Event exceptionalOccurrence = agendaEventService.saveEventExceptionalOccurrence(eventId,
                                                                                    start.plusDays(1));

    assertNotNull(exceptionalOccurrence);
    long exceptionalOccurrenceId = exceptionalOccurrence.getId();
    long userIdentityId = Long.parseLong(testuser1Identity.getId());

    List<EventReminder> eventReminders = agendaEventReminderService.getEventReminders(eventId, userIdentityId);
    assertNotNull(eventReminders);
    assertEquals(1, eventReminders.size());

    List<EventReminder> exceptionalOccurrenceReminders = agendaEventReminderService.getEventReminders(exceptionalOccurrenceId,
                                                                                                      userIdentityId);
    assertNotNull(exceptionalOccurrenceReminders);
    assertEquals(1, exceptionalOccurrenceReminders.size());

    EventReminder eventReminder = eventReminders.get(0);
    assertNotNull(eventReminder);

    eventReminder = eventReminder.clone();
    eventReminder.setId(0);
    eventReminder.setBefore(eventReminder.getBefore() + 1);
    eventReminders.add(eventReminder);

    agendaEventReminderService.saveEventReminders(event, eventReminders, userIdentityId);

    eventReminders = agendaEventReminderService.getEventReminders(eventId, userIdentityId);
    assertNotNull(eventReminders);
    assertEquals(2, eventReminders.size());
    exceptionalOccurrenceReminders = agendaEventReminderService.getEventReminders(exceptionalOccurrenceId, userIdentityId);
    assertNotNull(exceptionalOccurrenceReminders);
    assertEquals(2, exceptionalOccurrenceReminders.size());

    exceptionalOccurrenceReminders.remove(0);
    agendaEventReminderService.saveEventReminders(exceptionalOccurrence, exceptionalOccurrenceReminders, userIdentityId);

    eventReminders = agendaEventReminderService.getEventReminders(eventId, userIdentityId);
    assertNotNull(eventReminders);
    assertEquals(2, eventReminders.size());
    exceptionalOccurrenceReminders = agendaEventReminderService.getEventReminders(exceptionalOccurrenceId, userIdentityId);
    assertNotNull(exceptionalOccurrenceReminders);
    assertEquals(1, exceptionalOccurrenceReminders.size());

    agendaEventReminderService.saveEventReminders(event, Collections.emptyList(), userIdentityId);
    eventReminders = agendaEventReminderService.getEventReminders(eventId, userIdentityId);
    assertNotNull(eventReminders);
    assertEquals(0, eventReminders.size());
    exceptionalOccurrenceReminders = agendaEventReminderService.getEventReminders(exceptionalOccurrenceId, userIdentityId);
    assertNotNull(exceptionalOccurrenceReminders);
    assertEquals(0, exceptionalOccurrenceReminders.size());
  }

  @Test
  public void testSaveUpcomingEventReminders() throws Exception { // NOSONAR
    ZonedDateTime start = getDate().withNano(0);

    boolean allDay = true;

    Event event = newEventInstance(start, start, allDay);
    EventRecurrence recurrence = new EventRecurrence(0,
                                                     null,
                                                     0,
                                                     EventRecurrenceType.DAILY,
                                                     EventRecurrenceFrequency.DAILY,
                                                     1,
                                                     null,
                                                     null,
                                                     null,
                                                     null,
                                                     null,
                                                     null,
                                                     null,
                                                     null,
                                                     null,
                                                     null,
                                                     null);
    event.setRecurrence(recurrence);

    long testuser1Id = Long.parseLong(testuser1Identity.getId());
    event = createEvent(event.clone(), testuser1Id, testuser1Identity, testuser2Identity);

    long eventId = event.getId();

    EventReminder upcomingEventsReminder = new EventReminder(5000l,
                                                             10000l,
                                                             testuser1Id,
                                                             5,
                                                             ReminderPeriodType.HOUR);
    List<EventReminder> upcomingEventsReminders = Collections.singletonList(upcomingEventsReminder);

    List<EventReminder> eventReminders = agendaEventReminderService.getEventReminders(eventId, testuser1Id);
    assertNotNull(eventReminders);
    assertEquals(1, eventReminders.size());

    agendaEventService.saveEventExceptionalOccurrence(eventId, start.plusDays(4));

    agendaEventReminderService.saveUpcomingEventReminders(eventId, start.plusDays(5), upcomingEventsReminders, testuser1Id);

    Event exceptionalOccurrence = agendaEventService.saveEventExceptionalOccurrence(eventId, start.plusDays(10));

    assertNotNull(exceptionalOccurrence);

    eventReminders = agendaEventReminderService.getEventReminders(exceptionalOccurrence.getId(), testuser1Id);
    assertNotNull(eventReminders);
    assertEquals(1, eventReminders.size());
    EventReminder savedUpcomingEventReminder = eventReminders.get(0);
    assertNotNull(savedUpcomingEventReminder);
    assertNull(savedUpcomingEventReminder.getUntilOccurrenceId());
    assertNull(savedUpcomingEventReminder.getFromOccurrenceId());
    assertNotNull(savedUpcomingEventReminder.getDatetime());

    eventReminders = agendaEventReminderService.getEventReminders(eventId, testuser1Id);
    assertNotNull(eventReminders);
    assertEquals(2, eventReminders.size());
    savedUpcomingEventReminder = eventReminders.stream()
                                               .filter(reminder -> reminder.getFromOccurrenceId() != null)
                                               .findAny()
                                               .orElse(null);
    assertNotNull(savedUpcomingEventReminder);
    assertNull(savedUpcomingEventReminder.getUntilOccurrenceId()); // NOSONAR
    EventReminder savedOriginalEventReminder = eventReminders.stream()
                                                             .filter(reminder -> reminder.getUntilOccurrenceId() != null)
                                                             .findAny()
                                                             .orElse(null);
    assertNotNull(savedOriginalEventReminder);
    assertNull(savedOriginalEventReminder.getFromOccurrenceId()); // NOSONAR

    exceptionalOccurrence = agendaEventService.saveEventExceptionalOccurrence(eventId, start.plusDays(11));
    assertNotNull(exceptionalOccurrence);
    eventReminders = agendaEventReminderService.getEventReminders(exceptionalOccurrence.getId(), testuser1Id);
    assertNotNull(eventReminders);
    assertEquals(1, eventReminders.size());
    savedUpcomingEventReminder = eventReminders.get(0);
    assertNotNull(savedUpcomingEventReminder);
    assertNotNull(savedUpcomingEventReminder.getDatetime());

    agendaEventService.saveEventExceptionalOccurrence(eventId, start.plusDays(4));
    assertNotNull(eventReminders);
    assertEquals(1, eventReminders.size());
    savedUpcomingEventReminder = eventReminders.get(0);
    assertNotNull(savedUpcomingEventReminder);
    assertNotNull(savedUpcomingEventReminder.getDatetime());
    assertNull(savedUpcomingEventReminder.getFromOccurrenceId());
    assertNull(savedUpcomingEventReminder.getUntilOccurrenceId());
  }

  @Test
  public void testGetEventReminders() throws Exception { // NOSONAR
    ZonedDateTime start = ZonedDateTime.now().withNano(0);

    boolean allDay = false;

    Event event = newEventInstance(start, start, allDay);
    event = createEvent(event.clone(), Long.parseLong(testuser1Identity.getId()), testuser5Identity);

    long eventId = event.getId();
    long userIdentityId = Long.parseLong(testuser1Identity.getId());
    List<EventReminder> eventReminders = agendaEventReminderService.getEventReminders(eventId, userIdentityId);
    assertNotNull(eventReminders);
    assertEquals(1, eventReminders.size());

    EventReminder eventReminderToStore = REMINDERS.get(0);

    EventReminder eventReminder = eventReminders.get(0);
    assertNotNull(eventReminder);
    assertTrue(eventReminder.getId() > 0);
    assertEquals(eventReminder.getBefore(), eventReminderToStore.getBefore());
    assertEquals(eventReminder.getBeforePeriodType(), eventReminderToStore.getBeforePeriodType());
    assertEquals(userIdentityId, eventReminder.getReceiverId());
    assertNotNull(eventReminder.getDatetime());
    assertEquals(event.getStart().minusMinutes(eventReminder.getBefore()).withZoneSameInstant(ZoneOffset.UTC),
                 eventReminder.getDatetime().withZoneSameInstant(ZoneOffset.UTC));
  }

  @Test
  public void testGetDefaultReminders() throws Exception { // NOSONAR
    List<EventReminderParameter> defaultReminders = agendaUserSettingsService.getDefaultReminders();
    assertNotNull(defaultReminders);
    assertFalse(defaultReminders.isEmpty());

    try {
      defaultReminders.add(new EventReminderParameter());
      fail("Shouldn't allow list modification");
    } catch (Exception e) {
      // Expected
    }
  }

  @Test
  public void testSaveUserReminders() throws Exception { // NOSONAR
    ZonedDateTime start = ZonedDateTime.now().withNano(0);

    boolean allDay = false;

    Event event = newEventInstance(start, start, allDay);
    event = createEvent(event.clone(), Long.parseLong(testuser1Identity.getId()), testuser4Identity, testuser5Identity);

    long eventId = event.getId();
    long userIdentityId = Long.parseLong(testuser1Identity.getId());
    List<EventReminder> eventReminders = agendaEventReminderService.getEventReminders(eventId, userIdentityId);
    assertNotNull(eventReminders);
    assertEquals(1, eventReminders.size());

    long user4IdentityId = Long.parseLong(testuser4Identity.getId());

    eventReminders = Collections.singletonList(new EventReminder(user4IdentityId, 2, ReminderPeriodType.DAY));
    agendaEventReminderService.saveEventReminders(event, eventReminders, user4IdentityId);

    eventReminders = agendaEventReminderService.getEventReminders(eventId, user4IdentityId);
    assertEquals(1, eventReminders.size());

    eventReminders = agendaEventReminderService.getEventReminders(eventId);
    assertEquals(2, eventReminders.size());

    agendaEventReminderService.removeUserReminders(eventId, user4IdentityId);
    eventReminders = agendaEventReminderService.getEventReminders(eventId);
    assertEquals(1, eventReminders.size());

    eventReminders = agendaEventReminderService.getEventReminders(eventId, user4IdentityId);
    assertEquals(0, eventReminders.size());
  }

  @Test
  public void sendRemindersTest() throws Exception {
    TimeZone.setDefault(TimeZone.getTimeZone("Tunisia"));
    ZonedDateTime start = ZonedDateTime.now().withNano(0).plusMinutes(1);
    ZonedDateTime end = start.plusMinutes(15);
    boolean allDay = false;
    long userIdentityId = Long.parseLong(testuser1Identity.getId());
    WebNotificationService webNotificationService = container.getComponentInstanceOfType(WebNotificationService.class);

    Event reccurrentEvent = newEventInstance(start, end, allDay);
    EventRecurrence recurrence = new EventRecurrence(0,
            start.plusDays(2).toLocalDate(),
            0,
            EventRecurrenceType.DAILY,
            EventRecurrenceFrequency.DAILY,
            1,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);
    reccurrentEvent.setRecurrence(recurrence);

    reccurrentEvent = createEvent(reccurrentEvent.clone(), Long.parseLong(testuser1Identity.getId()), testuser1Identity, testuser2Identity);

    long recEventId = reccurrentEvent.getId();

    agendaEventService.saveEventExceptionalOccurrence(recEventId, start);
    agendaEventService.saveEventExceptionalOccurrence(recEventId, start.plusDays(1));
    List<Event> exceptionalOccurrence =  agendaEventService.getExceptionalOccurrenceEvents(recEventId, TimeZone.getTimeZone("Tunisia").toZoneId() , userIdentityId);

    assertNotNull(exceptionalOccurrence);
    long firstExceptionalOccurrenceId = exceptionalOccurrence.get(0).getId();
    List<EventReminder>  eventReminders = agendaEventReminderService.getEventReminders(recEventId, userIdentityId);
    assertNotNull(eventReminders);
    assertEquals(1, eventReminders.size());

    List<EventReminder> exceptionalOccurrenceReminders = agendaEventReminderService.getEventReminders(firstExceptionalOccurrenceId,
            userIdentityId);
    assertNotNull(exceptionalOccurrenceReminders);
    assertEquals(1, exceptionalOccurrenceReminders.size());
    webNotificationService.resetNumberOnBadge(testuser1Identity.getRemoteId());
    agendaEventReminderService.sendReminders();
    // Assert receive only one reminder notification of the first recurrence.
    int notificationSize  = webNotificationService.getNumberOnBadge(testuser1Identity.getRemoteId());
    assertEquals(1, notificationSize);
    webNotificationService.resetNumberOnBadge(testuser1Identity.getRemoteId());

    //
    Event event = newEventInstance(start, start, allDay);
    event.setRecurrence(null);
    event = createEvent(event.clone(), Long.parseLong(testuser1Identity.getId()), testuser1Identity);
    long eventId = event.getId();
    eventReminders = agendaEventReminderService.getEventReminders(eventId, userIdentityId);
    assertNotNull(eventReminders);
    assertEquals(1, eventReminders.size());
    EventReminder eventReminder = eventReminders.get(0);
    assertNotNull(eventReminder);
    agendaEventReminderService.sendReminders();
    // Assert receiving the reminder notification for the non-recurring event.
    assertEquals(notificationSize + 1, webNotificationService.getNumberOnBadge(testuser1Identity.getRemoteId()));
  }

}
