package com.smartfreelance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.FREELANCER;

    @Column(nullable = false)
    private boolean active = true;

    private String phone;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String title;

    private String skillsList;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @jakarta.persistence.OneToMany(mappedBy = "client", fetch = jakarta.persistence.FetchType.LAZY)
    private java.util.List<Project> createdProjects = new java.util.ArrayList<>();

    public User() {
    }

    public User(Long id, String name, String email, String password, String role, boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        setRole(role);
        this.active = active;
    }

    public User(Long id, String name, String email, String password, Role role, boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role != null ? role : Role.FREELANCER;
        this.active = active;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role != null ? role.name() : "FREELANCER";
    }

    public void setRole(String roleStr) {
        if (roleStr == null || roleStr.trim().isEmpty()) {
            this.role = Role.FREELANCER;
        } else {
            try {
                this.role = Role.valueOf(roleStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role: " + roleStr + ". Valid roles are: CLIENT, FREELANCER, ADMIN");
            }
        }
    }

    public Role getRoleEnum() {
        return role;
    }

    public void setRoleEnum(Role role) {
        this.role = role != null ? role : Role.FREELANCER;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public java.util.List<Project> getCreatedProjects() {
        return createdProjects;
    }

    public void setCreatedProjects(java.util.List<Project> createdProjects) {
        this.createdProjects = createdProjects;
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
