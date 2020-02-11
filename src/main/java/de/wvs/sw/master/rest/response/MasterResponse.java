package de.wvs.sw.master.rest.response;

/**
 * Created by Marvin Erkes on 04.02.2020.
 */
public class MasterResponse {

    private Status status;

    private String message;

    public MasterResponse(Status status, String message) {

        this.status = status;
        this.message = message;
    }

    public Status getStatus() {

        return status;
    }

    public String getMessage() {

        return message;
    }

    public enum Status {

        OK,
        ERROR
    }
}
