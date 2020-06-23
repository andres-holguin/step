// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import java.util.Collections;
import java.util.Arrays;
import java.util.ArrayList;

public final class FindMeetingQuery {

  /** 
   * Return possible times everyone can meet by removing all minutes already scheduled. 
   * If optional attendees can all be included, those times will be returned instead.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    final int MINUTES_IN_DAY = 60 * 24;

    ArrayList<TimeRange> timeRanges = new ArrayList<>();

    // If a meeting can be scheduled while considering optional attendees, those times will be returned instead
    ArrayList<TimeRange> timeRangesWithOptionalAttendees = new ArrayList<>();

    // Initialize boolean arrays for each minute in the day.
    boolean[] availableMinutes = new boolean[MINUTES_IN_DAY + 1];
    Arrays.fill(availableMinutes, true);
    boolean[] availableMinutesWithOptionalAttendees = new boolean[MINUTES_IN_DAY + 1];
    Arrays.fill(availableMinutesWithOptionalAttendees, true);

    // Initialize some consts that will be used on iterations
    Collection<String> requiredAttendees = request.getAttendees();
    Collection<String> optionalAttendees = request.getOptionalAttendees();

    // Treat optional attendees as required if there are no required attendees
    if (requiredAttendees.isEmpty()) {
      requiredAttendees = optionalAttendees;
      optionalAttendees = Collections.emptySet();
    }
    
    // For each event with matching attending, remove respective available minutes
    for (Event event : events) {
      if (!Collections.disjoint(event.getAttendees(), requiredAttendees)) {
        // If the event and request attendees have an overlap (not disjoint),
        // Someone in the meeting request has another event, so remove those available minutes
        TimeRange eventTime = event.getWhen();
        for (int i = eventTime.start(); i < eventTime.end(); i++) {
          availableMinutes[i] = false;
          availableMinutesWithOptionalAttendees[i] = false;
        }
      }

      if (!Collections.disjoint(event.getAttendees(), optionalAttendees)) {
        // Same as above, but only considers optional attendees.
        TimeRange eventTime = event.getWhen();
        for (int i = eventTime.start(); i < eventTime.end(); i++) {
          availableMinutesWithOptionalAttendees[i] = false;
        }
      }      
    }

    // Build Time Ranges from boolean arrays
    buildTimeRanges(timeRangesWithOptionalAttendees, availableMinutesWithOptionalAttendees, request.getDuration());
    if (!timeRangesWithOptionalAttendees.isEmpty()) {
      return timeRangesWithOptionalAttendees;
    } else {
      buildTimeRanges(timeRanges, availableMinutes, request.getDuration());
      return timeRanges;
    }
  }

  /**
   * Fill the list of Time Ranges ( @param timeRanges ) with options for times to schedule 
   * based on the array of available minutes ( @param availableMinutes ) and the duration of
   * the requested meeting ( @param requestDuration ).
   */
  private void buildTimeRanges(ArrayList<TimeRange> timeRanges, boolean[] availableMinutes, long requestDuration) {
    boolean lastMinuteAvailable = false;
    int start = 1, end = 0;
    for (int i = 0; i < availableMinutes.length; i++) {
      boolean currentMinuteAvailable = availableMinutes[i];
      if (!lastMinuteAvailable && currentMinuteAvailable) { 
        // The start of a potential time range
        start = i;
      }
      boolean onLastIteration = (i == availableMinutes.length - 1);
      if (lastMinuteAvailable && (!currentMinuteAvailable || onLastIteration)) { 
        // The end of a potential time range, or the end of the boolean array
        end = i;
        if (end - start >= requestDuration) {
          timeRanges.add(TimeRange.fromStartEnd(start, end, false));
        }
      }
      lastMinuteAvailable = currentMinuteAvailable;
    }
  }
}
