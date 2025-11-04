package masecla.reddit4j.http.clients;

import io.github.bucket4j.Bucket;
import masecla.reddit4j.http.GenericHttpClient;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;

import java.io.IOException;
import java.time.Duration;

public class RateLimitedClient extends GenericHttpClient {

    /**
     * This is the maximum amount of requests per minute a client is allowed to do.
     * This is according to the https://github.com/reddit-archive/reddit/wiki/API
     */
    private final Bucket bucket;

    public RateLimitedClient() {
        super();
        this.bucket = Bucket.builder()
                .addLimit(limit -> limit.capacity(10000).refillIntervally(10000, Duration.ofMinutes(10)))
                .build();
    }

    public boolean allowRequest() {
        return bucket.tryConsume(1);
    }

    @Override
    public Response execute(Connection connection) throws IOException, InterruptedException {
        while (!allowRequest()) {
            //wait until we can execute the request
            Thread.sleep(1000);
        }
        return connection.execute();
    }

}
