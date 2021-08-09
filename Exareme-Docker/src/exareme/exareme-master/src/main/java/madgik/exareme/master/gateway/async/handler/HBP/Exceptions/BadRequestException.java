package madgik.exareme.master.gateway.async.handler.HBP.Exceptions;

public class BadRequestException extends Exception {
    public BadRequestException(String algorithmName, String message) {
        super(message + "  Algorithm: " + algorithmName);
    }
}