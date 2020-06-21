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

  /** return possible times everyone can meet by removing all minutes already scheduled */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    ArrayList<TimeRange> timeRanges = new ArrayList<>();
    final int MINUTES_IN_DAY = 60 * 24;

    // Initialize boolean array for each minutes in the day
    boolean[] availableMinutes = new boolean[MINUTES_IN_DAY + 1];
    Arrays.fill(availableMinutes, true);  

    // For each event with matching attending, remove respective available minutes
    for (Event event : events) {
      // Skip if attendees don't overlap
      if (Collections.disjoint(event.getAttendees(), request.getAttendees())) continue;
      // Someone in the meeting request has another event, so remove those available minutes
      TimeRange eventTime = event.getWhen();
      for (int i = eventTime.start(); i < eventTime.end(); i++) {
        availableMinutes[i] = false;
      }
    }

    // Build Time Ranges from boolean array
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
        if (end - start >= request.getDuration()) {
          timeRanges.add(TimeRange.fromStartEnd(start, end, false));
        }
      }
      lastMinuteAvailable = currentMinuteAvailable;
    }

    return timeRanges;
  }
}
