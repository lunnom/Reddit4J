package masecla.reddit4j.requests;

import masecla.reddit4j.client.Reddit4J;
import masecla.reddit4j.objects.subreddit.RedditSubreddit;

public class RedditSubredditListingEndpointRequest extends AbstractListingEndpointRequest<RedditSubreddit, RedditSubredditListingEndpointRequest> {
    public RedditSubredditListingEndpointRequest(String endpointPath, Reddit4J client) {
        super(endpointPath, client, RedditSubreddit.class, RedditSubredditListingEndpointRequest.class);
    }
}
