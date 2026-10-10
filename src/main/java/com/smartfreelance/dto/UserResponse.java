package com.smartfreelance.dto;

import com.smartfreelance.entity.User;

public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String role;
    private boolean active;
    private String phone;
    private String bio;
    private String title;
    private String skillsList;

    public UserResponse() {
    }

    public UserResponse(Long id, String name, String email, String role, boolean active,
                        String phone, String bio, String title, String skillsList) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.active = active;
        this.phone = phone;
        this.bio = bio;
        this.title = title;
        this.skillsList = skillsList;
    }

    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getPhone(),
                user.getBio(),
                user.getTitle(),
                user.getSkillsList()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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
