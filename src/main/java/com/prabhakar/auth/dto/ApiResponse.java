package com.prabhakar.auth.dto;

public class ApiResponse<T> {

    private boolean success;
    private T data;
    private Integer status;
    private String error;
    private String message;

    // SUCCESS response
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = true;
        r.data = data;
        return r;
    }

    // ERROR response
    public static <T> ApiResponse<T> error(int status, String error, String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = false;
        r.status = status;
        r.error = error;
        r.message = message;
        return r;
    }

    // Getters & setters
    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public Integer getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }

    public void setSuccess(boolean success) { this.success = success; }
    public void setData(T data) { this.data = data; }
    public void setStatus(Integer status) { this.status = status; }
    public void setError(String error) { this.error = error; }
    public void setMessage(String message) { this.message = message; }
}
