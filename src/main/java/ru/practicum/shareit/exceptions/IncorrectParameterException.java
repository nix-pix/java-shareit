package ru.practicum.shareit.exceptions;

public class IncorrectParameterException extends RuntimeException {
    public IncorrectParameterException(String parameter) {
        super(parameter);
    }
}
