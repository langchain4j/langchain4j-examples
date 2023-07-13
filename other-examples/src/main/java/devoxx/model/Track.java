package devoxx.model;

import lombok.Value;

@Value
public class Track {

    int id;
    String name;
    String description;
    String imageURL;
}