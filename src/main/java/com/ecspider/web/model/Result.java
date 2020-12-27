package com.ecspider.web.model;

/**
 * @author lyifee
 * on 2020/12/27
 */
public class Result {
    private String message;

    private Boolean success;

    private Result(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    public static Result success(String message) {
        return new Result(message, true);
    }

    public static Result fail(String message) {
        return new Result(message, false);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}
