package madgik.exareme.master.engine.executor.remote.operator.OptiqueStreamQueryEndPoint;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

/**
 * @author Christoforos Svingos
 */
public class HttpResponses {
    public static void badRequest(final HttpResponse response, String message) {
        response.setStatusCode(HttpStatus.SC_BAD_REQUEST);

        if (message != null) {
            StringEntity entity =
                new StringEntity(message, ContentType.create("text/plain", "UTF-8"));
            response.setEntity(entity);
        }
    }

    public static void internalServerError(final HttpResponse response, String message) {
        response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);

        if (message != null) {
            StringEntity entity =
                new StringEntity(message, ContentType.create("text/plain", "UTF-8"));
            response.setEntity(entity);
        }
    }

    public static void notFound(final HttpResponse response, String message) {
        response.setStatusCode(HttpStatus.SC_NOT_FOUND);

        if (message != null) {
            StringEntity entity =
                new StringEntity(message, ContentType.create("text/plain", "UTF-8"));
            response.setEntity(entity);
        }
    }

    public static void conflict(final HttpResponse response, String message) {
        response.setStatusCode(HttpStatus.SC_CONFLICT);

        if (message != null) {
            StringEntity entity =
                new StringEntity(message, ContentType.create("text/plain", "UTF-8"));
            response.setEntity(entity);
        }
    }

    public static void toManyRequests(final HttpResponse response, String message) {
        response.setStatusCode(429);
        response.setReasonPhrase("Too Many Requests");

        if (message != null) {
            StringEntity entity =
                new StringEntity(message, ContentType.create("text/plain", "UTF-8"));
            response.setEntity(entity);
        }
    }

    public static void quickResponse(final HttpResponse response, String message) {
        response.setStatusCode(HttpStatus.SC_OK);

        if (message != null) {
            StringEntity entity =
                new StringEntity(message, ContentType.create("application/json", "UTF-8"));
            response.setEntity(entity);
        }
    }

    public static void serviceUnavailable(final HttpResponse response, String message) {
        response.setStatusCode(HttpStatus.SC_SERVICE_UNAVAILABLE);

        if (message != null) {
            StringEntity entity =
                new StringEntity(message, ContentType.create("text/plain", "UTF-8"));
            response.setEntity(entity);
        }
    }
}
