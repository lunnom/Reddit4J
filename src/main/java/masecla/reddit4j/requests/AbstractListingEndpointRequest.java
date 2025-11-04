package masecla.reddit4j.requests;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import masecla.reddit4j.client.Reddit4J;
import masecla.reddit4j.exceptions.AuthenticationException;
import masecla.reddit4j.objects.RedditNameable;
import masecla.reddit4j.objects.RedditThing;
import masecla.reddit4j.objects.adapters.DimensionAdapter;
import masecla.reddit4j.objects.preferences.enums.Language;
import masecla.reddit4j.objects.subreddit.UserFlairRichText;
import org.jsoup.Connection;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractListingEndpointRequest<T extends RedditThing & RedditNameable, S extends AbstractListingEndpointRequest<?, ?>> {
    protected String endpointPath;
    protected Reddit4J client;
    protected final Class<T> clazz;
    protected final S self;

    protected AbstractListingEndpointRequest(String endpointPath, Reddit4J client, Class<T> clazz, Class<S> selfClass) {
        this.endpointPath = endpointPath;
        this.client = client;
        this.clazz = clazz;
        this.self = selfClass.cast(this);
    }

    protected int count = 0;
    protected int limit = 25;
    protected boolean show = false;
    protected String before = null;
    protected String after = null;

    public String preprocess(String body) {
        return body;
    }

    public List<T> submit() throws IOException, InterruptedException, AuthenticationException {
        client.ensureConnection();
        Connection conn = createConnection();

        Connection.Response rsp = conn.execute();
        JsonArray array = JsonParser.parseString(preprocess(rsp.body())).getAsJsonObject().getAsJsonObject("data")
                .getAsJsonArray("children");
        Gson gson = getGson();

        List<T> result = new ArrayList<>();
        array.forEach(c -> {
            var jsonObject = c.getAsJsonObject();
            if (jsonObject.has("data") && jsonObject.has("kind")) {
                T value = gson.fromJson(jsonObject.getAsJsonObject("data"), clazz);
                result.add(value);
            } else {
                T value = gson.fromJson(c.getAsJsonObject(), clazz);
                result.add(value);
            }
        });
        return result;
    }

    public S after(T after) {
        this.after = (after == null) ? null : after.getName();
        return self;
    }

    public S after(String after) {
        this.after = after;
        return self;
    }

    public S before(T before) {
        this.before = (before == null) ? null : before.getName();
        return self;
    }

    public S before(String before) {
        this.before = before;
        return self;
    }

    public S count(int count) {
        this.count = count;
        return self;
    }

    public S limit(int limit) {
        if (limit < 25 || limit > 100)
            throw new IllegalArgumentException("Limit must be between 25 and 100");
        this.limit = limit;
        return self;
    }

    public S show(boolean show) {
        this.show = show;
        return self;
    }

    protected Connection createConnection() {
        Connection conn = client.useEndpoint(endpointPath);

        if (after != null)
            conn.data("after", after);
        if (before != null)
            conn.data("before", before);
        if (count != 0)
            conn.data("count", count + "");

        conn.data("limit", limit + "");

        if (show)
            conn.data("show", "all");

        return conn;
    }

    public Gson getGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Language.class, Language.getAdapter());
        builder.registerTypeAdapter(UserFlairRichText.class, UserFlairRichText.getAdapter());
        builder.registerTypeAdapter(Dimension.class, new DimensionAdapter());
        return builder.create();
    }
}
