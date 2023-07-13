package devoxx.model;

import lombok.Value;

import java.util.List;

@Value
public class Talk {

    int id;
    String title;
    String description;
    String summary;
    String afterVideoURL;
    String audienceLevel;
    Track track;
    SessionType sessionType;
    List<Speaker> speakers;
    List<Tag> tags;
    List<TimeSlot> timeSlots;
}









