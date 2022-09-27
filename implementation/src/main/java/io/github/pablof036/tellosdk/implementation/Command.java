package io.github.pablof036.tellosdk.implementation;

/**
 * A network command
 */
class Command {
    private final String message;
    private String response;
    private Throwable throwable;

    protected Command(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
