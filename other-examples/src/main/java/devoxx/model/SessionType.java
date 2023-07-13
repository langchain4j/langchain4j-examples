package devoxx.model;

import lombok.Value;

@Value
public class SessionType {

    int id;
    String name;
    int duration;
    boolean isPause;
    String description;
    String cssColor;
}