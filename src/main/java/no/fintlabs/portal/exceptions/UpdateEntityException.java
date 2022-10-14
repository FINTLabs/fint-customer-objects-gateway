package no.fintlabs.portal.exceptions;

@SuppressWarnings("ALL")
public class UpdateEntityException extends RuntimeException {
    public UpdateEntityException() {
    }

    public UpdateEntityException(String message) {
        super(message);
    }
}
