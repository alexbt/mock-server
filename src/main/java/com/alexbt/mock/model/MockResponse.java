package com.alexbt.mock.model;

/**
 * @author alexbt
 */
public class MockResponse {

    private int status;
    private Object content;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }
}
