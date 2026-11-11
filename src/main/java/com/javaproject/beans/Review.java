
package com.javaproject.beans;

public class Review {

    private Long id;
    private Long gameId;
    private String text;

    public Review() {
    }

    public Review(Long id, Long gameId, String text) {
        this.id = id;
        this.gameId = gameId;
        this.text = text;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
