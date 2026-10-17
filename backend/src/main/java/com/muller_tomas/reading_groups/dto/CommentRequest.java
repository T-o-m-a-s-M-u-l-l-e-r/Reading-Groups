package com.muller_tomas.reading_groups.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CommentRequest {

    private Integer categoryId;

    @NotBlank(message = "Text section cannot be blank")
    @Size(max = 2000, message = "Text section is too long")
    private String textSection;

    @NotBlank(message = "Comment content cannot be blank")
    @Size(max = 2000, message = "Comment content is too long")
    private String commentContent;

    @NotNull(message = "Page number is required")
    @Min(value = 1, message = "Page number must be at least 1")
    private Integer commentPageNumber;

    @NotNull(message = "Page occurrence is required")
    @Min(value = 1, message = "Page occurrence must be at least 1")
    private Integer commentPageOccurrence;

    public CommentRequest() {
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getTextSection() {
        return textSection;
    }

    public void setTextSection(String textSection) {
        this.textSection = textSection;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }

    public Integer getCommentPageNumber() {
        return commentPageNumber;
    }

    public void setCommentPageNumber(Integer commentPageNumber) {
        this.commentPageNumber = commentPageNumber;
    }

    public Integer getCommentPageOccurrence() {
        return commentPageOccurrence;
    }

    public void setCommentPageOccurrence(Integer commentPageOccurrence) {
        this.commentPageOccurrence = commentPageOccurrence;
    }
}