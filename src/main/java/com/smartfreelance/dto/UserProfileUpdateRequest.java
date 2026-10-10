package com.smartfreelance.dto;

public class UserProfileUpdateRequest {

    private String name;
    private String phone;
    private String bio;
    private String title;
    private String skillsList;

    public UserProfileUpdateRequest() {
    }

    public UserProfileUpdateRequest(String name, String phone, String bio, String title, String skillsList) {
        this.name = name;
        this.phone = phone;
        this.bio = bio;
        this.title = title;
        this.skillsList = skillsList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSkillsList() {
        return skillsList;
    }

    public void setSkillsList(String skillsList) {
        this.skillsList = skillsList;
    }
}
