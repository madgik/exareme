package madgik.exareme.master.gateway.async.handler.HBP.Exceptions;

public class RequestException extends Exception {
    public RequestException(String algorithmName, String message) {
        super(message + "  Algorithm: " + algorithmName);
    }
}