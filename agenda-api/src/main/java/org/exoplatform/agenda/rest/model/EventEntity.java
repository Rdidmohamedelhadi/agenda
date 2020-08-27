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
package org.exoplatform.agenda.rest.model;

import org.exoplatform.agenda.constant.EventAvailability;
import org.exoplatform.agenda.constant.EventStatus;
import org.exoplatform.social.rest.entity.IdentityEntity;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventEntity {

  private long              id;

  /**
   * Parent event object, when :
   * <ul>
   * <li>this is an occurrence (exceptional or ordinal) of a recurring event
   * </li>
   * <li>this is a duplication of an event that was modified by an attendee
   * having Readonly ACL on event. In that case, the event is modified and
   * duplicated in user calendar only.</li>
   * </ul>
   */
  private EventEntity       parent;

  /**
   * External Event technical identifier if imported from external Store. This
   * can be used to identify calendar event if re-importing events from remote
   * provider.
   */
  private String            remoteId;

  /**
   * Configured Identifier of Remote Calendar identifier, for example: "google"
   * for Google Calendar and "office365" for Office 365 Calendar.
   */
  private String            remoteProviderId;

  private CalendarEntity    calendar;

  private IdentityEntity    creator;

  private String            created;

  private String            updated;

  private String            summary;

  private String            description;

  /**
   * Geopgraphic location of the event, content is free text and no predefined
   * format is used.
   */
  private String            location;

  /**
   * CSS color HEX value of the event
   */
  private String            color;

  /**
   * Start date of the event.
   * <ul>
   * <li>It's of type datetime converted to user timezone if not "all-day"
   * event.(Timestamp representation using RFC-3339.)</li>
   * <li>When it's an all-day event, the returned date is of format
   * 'yyyy-mm-dd', for example: 2020-07-20.</li>
   * </ul>
   */
  private String            start;

  /**
   * End date of the event.
   * <ul>
   * <li>It's of type datetime converted to user timezone if not "all-day"
   * event.(Timestamp representation using RFC-3339.)</li>
   * <li>When it's an all-day event, the returned date is of format
   * 'yyyy-mm-dd', for example: 2020-07-20.</li>
   * </ul>
   */
  private String            end;

  /**
   * Whether the event happens all-day or at dedicated period of a day
   */
  private boolean           allDay;

  private EventAvailability availability;

  private EventStatus       status;

}