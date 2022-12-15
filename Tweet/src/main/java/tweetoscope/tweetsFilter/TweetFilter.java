/*
Copyright 2022 Virginie Galtier

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program. If not, see <https://www.gnu.org/licenses/>
 */
package tweetoscope.tweetsFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import com.twitter.clientlib.model.Tweet;

/**
 * Reacts to the reception of a new Tweet, if the Tweet matches the filter
 * condition, downstream subscribers are notified, otherwise the process is
 * silent. Tweets are received from
 * {@link distributed_tweetoscope.tweetsProducer.TweetsProducer} via Java Flow.
 * Filtered Tweets are passes down to the
 * {@link distributed_tweetoscope.HashtagCounter} via Java Flow.
 * 
 * @author Virginie Galtier
 *
 */
public abstract class TweetFilter implements Subscriber<Tweet>, Publisher<Tweet> {
	/**
	 * List of objects to notify when a new Tweet passes the filter (downstream
	 * component = HashtagExtractor)
	 */
	protected List<Subscriber<? super Tweet>> subscribers;
	/**
	 * Creates a new Tweet filter
	 **/
	public TweetFilter() {
		subscribers = new ArrayList<Subscriber<? super Tweet>>();
	}
	/**
	 * Tests the filter conditions
	 * 
	 * @param tweet Tweet to examine
	 * @return true if the Tweet complies with the filter, false if it doesn't match
	 *         the filter conditions
	 */
	protected abstract boolean match(Tweet tweet);
	@Override
	public void subscribe(Subscriber<? super Tweet> subscriber) {
		subscribers.add(subscriber);
	}

	@Override
	public void onNext(Tweet tweet) {
		if (match(tweet)) {
			// Notify the subscribers of the non-filtered out Tweet
			for (Subscriber<? super Tweet> s : subscribers) {
				s.onNext(tweet);
			}
		}
	}


	@Override
	public void onSubscribe(Subscription subscription) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onError(Throwable throwable) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onComplete() {
		// TODO Auto-generated method stub
	}
}