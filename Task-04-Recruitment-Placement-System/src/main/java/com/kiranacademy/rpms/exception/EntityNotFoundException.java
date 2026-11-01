package com.kiranacademy.rpms.exception;

public class EntityNotFoundException extends RPMSException {

    public EntityNotFoundException(String entity, Object id) {
        super(entity + " not found for id: " + id);
    }

    public EntityNotFoundException(String message) {
        super(message);
    }
}
