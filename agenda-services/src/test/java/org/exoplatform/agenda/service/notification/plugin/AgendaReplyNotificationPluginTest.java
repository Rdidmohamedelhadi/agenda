package org.exoplatform.agenda.service.notification.plugin;

import org.exoplatform.agenda.constant.EventAttendeeResponse;
import org.exoplatform.agenda.constant.EventModificationType;
import org.exoplatform.agenda.model.Event;
import org.exoplatform.agenda.model.EventAttendee;
import org.exoplatform.agenda.notification.plugin.EventReplyNotificationPlugin;
import org.exoplatform.agenda.service.BaseAgendaEventTest;
import org.exoplatform.agenda.util.NotificationUtils;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.social.core.identity.model.Identity;
import org.junit.Assert;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.List;

public class AgendaReplyNotificationPluginTest extends BaseAgendaEventTest {
  @Test
  public void testSendNotificationWhenReplyToEvent() throws Exception {
    // Given
    ZonedDateTime start = ZonedDateTime.now();

    boolean allDay = false;

    Event event = newEventInstance(start, start, allDay);
    event = createEvent(event, Long.parseLong(testuser1Identity.getId()), testuser2Identity, testuser5Identity);

    List<EventAttendee> eventAttendees = agendaEventAttendeeService.getEventAttendees(event.getId());
    eventAttendees.add(new EventAttendee(0,
                                         event.getId(),
                                         Long.parseLong(testuser2Identity.getId()),
                                         EventAttendeeResponse.ACCEPTED));
    agendaEventAttendeeService.saveEventAttendees(event,
                                                  eventAttendees,
                                                  Long.parseLong(testuser1Identity.getId()),
                                                  false,
                                                  true,
                                                  EventModificationType.ADDED);
    agendaEventAttendeeService.sendEventResponse(event.getId(),
                                                 Long.parseLong(testuser2Identity.getId()),
                                                 EventAttendeeResponse.ACCEPTED);
    InitParams initParams = new InitParams();
    ValueParam value = new ValueParam();
    value.setName(NotificationUtils.AGENDA_REPLY_NOTIFICATION_PLUGIN);
    value.setValue("#111111");
    initParams.addParam(value);
    value.setName("agenda.notification.plugin.key");
    value.setValue("111");
    initParams.addParam(value);

    EventReplyNotificationPlugin replyNotificationPlugin = new EventReplyNotificationPlugin(initParams,
                                                                                            identityManager,
                                                                                            agendaCalendarService);
    NotificationContext ctx =
                            NotificationContextImpl.cloneInstance()
                                                   .append(NotificationUtils.EVENT_AGENDA, event)
                                                   .append(NotificationUtils.EVENT_PARTICIPANT_ID,
                                                           eventAttendees.get(2).getIdentityId())
                                                   .append(NotificationUtils.EVENT_RESPONSE, eventAttendees.get(2).getResponse());
    String eventUrl = System.getProperty("gatein.email.domain.url")
                            .concat("portal/classic/agenda?parentId=")
                            .concat(String.valueOf(event.getId()));

    String avatarUrl = "/portal/rest/v1/social/users/default-image/";
    Identity identity = identityManager.getIdentity(String.valueOf(testuser2Identity.getId()));

    // When
    NotificationInfo notificationInfo = replyNotificationPlugin.makeNotification(ctx);
    Assert.assertNotNull(notificationInfo);

    // Then
    Assert.assertEquals(String.valueOf(event.getId()),
                        notificationInfo.getValueOwnerParameter(NotificationUtils.TEMPLATE_VARIABLE_EVENT_ID));
    Assert.assertEquals(event.getSummary(),
                        notificationInfo.getValueOwnerParameter(NotificationUtils.TEMPLATE_VARIABLE_EVENT_TITLE));
    Assert.assertTrue(notificationInfo.getValueOwnerParameter(NotificationUtils.STORED_PARAMETER_EVENT_PARTICIPANT_AVATAR_URL)
                                      .startsWith(avatarUrl));
    Assert.assertTrue(notificationInfo.getValueOwnerParameter(NotificationUtils.STORED_PARAMETER_EVENT_URL).startsWith(eventUrl));
    Assert.assertEquals(eventAttendees.get(2).getResponse().name(),
                        notificationInfo.getValueOwnerParameter(NotificationUtils.STORED_PARAMETER_EVENT_RESPONSE));
    Assert.assertEquals(identity.getProfile().getFullName(),
                        notificationInfo.getValueOwnerParameter(NotificationUtils.STORED_PARAMETER_EVENT_PARTICIPANT_NAME));

  }
}