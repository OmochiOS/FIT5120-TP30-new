package com.fomopotato.amityspace.exception;

public class SensoryProfileNotFoundException extends RuntimeException {
    public SensoryProfileNotFoundException(Long locationId) {
        super("Sensory profile not found for location id: " + locationId);
    }
}