package tweetoscope.tweetsProducer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;

import com.twitter.clientlib.model.Tweet;

/**
 * Introduces Tweets into the system. The Tweets might be crafted ones, Tweets
 * read from an archive, or Tweets retrieved live from the Twitter API.
 * 
 * @author Virginie Galtier
 *
 */
public abstract class TweetsProducer implements Runnable, Publisher<Tweet> {
	/**
	 * List of objects to notify when a new Tweet is received (downstream components
	 * = TweetFilter)
	 */
	protected List<Subscriber<? super Tweet>> subscribers;

	public TweetsProducer() {
		subscribers = new ArrayList<Subscriber<? super Tweet>>();
	}

	@Override
	public void subscribe(Subscriber<? super Tweet> subscriber) {
		subscribers.add(subscriber);
	} //Define how to subscribe a message

}
