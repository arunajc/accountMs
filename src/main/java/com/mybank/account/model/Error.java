package com.mybank.account.model;

import java.util.Objects;

public class Error {

    public Error(String code, String description){
        this.code = code;
        this.description = description;
    }

    private String code;
    private String description;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Error error = (Error) o;
        return Objects.equals(code, error.code) &&
                Objects.equals(description, error.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, description);
    }

    @Override
    public String toString() {
        return "Error{" +
                "code='" + code + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
