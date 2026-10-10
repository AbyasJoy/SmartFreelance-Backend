package com.smartfreelance.dto;

import java.util.Set;

public class KnowledgeChunk {

    private String id;
    private String title;
    private String category;
    private String content;
    private Set<String> keywords;

    public KnowledgeChunk() {
    }

    public KnowledgeChunk(String id, String title, String category, String content, Set<String> keywords) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.content = content;
        this.keywords = keywords;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Set<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(Set<String> keywords) {
        this.keywords = keywords;
    }
}
