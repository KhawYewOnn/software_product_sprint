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
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashSet;

public final class FindMeetingQuery {
  /**
  Check if any of the string that appear in stringSet appear in otherSet
   */
  public boolean hasMatch(HashSet<String> stringSet, HashSet<String> otherSet) {
    Iterator<String> it = stringSet.iterator();
    while(it.hasNext()){
      if (otherSet.contains(it.next())) {
        return true;
      }
    }
    return false;
  }

  public ArrayList<TimeRange> filterTimes(Collection<Event> events, HashSet<String> attendees) {
    ArrayList<TimeRange> times = new ArrayList<TimeRange>();
    for (Event event: events) {
      HashSet<String> currAttendees = new HashSet<String>();
      currAttendees.addAll(event.getAttendees());
      if (hasMatch(currAttendees, attendees)) {
        if(event.getWhen().duration() == 0) {
          continue;
        }
        times.add(event.getWhen());
      }
    }
    return times;
  }

  public ArrayList<TimeRange> mergeTimes(ArrayList<TimeRange> times) {
    Collections.sort(times, TimeRange.ORDER_BY_START);
    ArrayList<TimeRange> merged = new ArrayList<>();
    if (times.size() == 0) {
      return merged;
    }
    TimeRange previousTime = times.get(0);
    for (int i = 1; i < times.size(); i++) {
      TimeRange currTime = times.get(i);
      if (previousTime.overlaps(currTime)){
        if (previousTime.contains(currTime)) {
          continue;
        }
        previousTime = TimeRange.fromStartEnd(previousTime.start(),
                currTime.end(), false);
      } else {
        merged.add(previousTime);
        previousTime = currTime;
      }
    }
    if (merged.size() == 0 || !merged.get(merged.size() - 1).overlaps(previousTime)) {
      merged.add(previousTime);
    }
    return merged;
  }

  public ArrayList<TimeRange> getExcludedTimes(ArrayList<TimeRange> times) {
    Collections.sort(times, TimeRange.ORDER_BY_START);
    ArrayList<TimeRange> excludedTimes = new ArrayList<>();
    if (times.size() == 0) {
      excludedTimes.add(TimeRange.WHOLE_DAY);
      return excludedTimes;
    }
    TimeRange firstTime = times.get(0);
    if (firstTime.start() != TimeRange.START_OF_DAY) {
      excludedTimes.add(TimeRange.fromStartEnd(TimeRange.START_OF_DAY,
              firstTime.start(), false));
    }
    for (int i = 0; i < times.size() - 1; i++) {
      int startTime = times.get(i).end();
      int endTime = times.get(i + 1).start();
      if (endTime - startTime <= 1) {
        // no gap in between to time slot
        continue;
      }
      TimeRange currTime = TimeRange.fromStartEnd(
              startTime, endTime, false);
      excludedTimes.add(currTime);
    }
    TimeRange lastTime = times.get(times.size() - 1);
    if (lastTime.end() < TimeRange.END_OF_DAY) {
      TimeRange lastExcludedTime = TimeRange.fromStartEnd(
              lastTime.end(), TimeRange.END_OF_DAY, true);
      excludedTimes.add(lastExcludedTime);
    }
    return excludedTimes;
  }

  public ArrayList<TimeRange> filterDuration(ArrayList<TimeRange> times, long duration) {
    ArrayList<TimeRange> filtered = new ArrayList<>();
    if (duration > 24 * 60) {
      return filtered;
    }
    for (TimeRange time: times) {
      if (time.duration() >= duration) {
        filtered.add(time);
      }
    }
    return filtered;
  }

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    HashSet<String> attendees = new HashSet<String>();
    attendees.addAll(request.getAttendees());
    ArrayList<TimeRange> filteredTimes = filterTimes(events, attendees);
    ArrayList<TimeRange> mergedTimes = mergeTimes(filteredTimes);
    ArrayList<TimeRange> freeTimes = getExcludedTimes(mergedTimes);
    ArrayList<TimeRange> filteredDuration = filterDuration(freeTimes, request.getDuration());
    return filteredDuration;
  }
}