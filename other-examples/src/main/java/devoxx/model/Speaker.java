package devoxx.model;

import lombok.Value;

@Value
public class Speaker {

    int id;
    String firstName;
    String lastName;
    String fullName;
    String bio;
    String company;
    String imageUrl;
    String twitterHandle;
}