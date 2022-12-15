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
package tweetoscope;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import com.twitter.clientlib.model.Tweet;
import com.twitter.twittertext.Extractor;

/**
 * Reacts to the reception of a new Tweet by parsing its text to look for
 * hashtags. For each hashtag found, if any, notifies its subscribers. Tweets
 * are received from the upstream component
 * {@link distributed_tweetoscope.tweetsFilter.TweetFilter} via Java Flow.
 * Hashtags are passed downstream to the
 * {@link distributed_tweetoscope.HashtagCounter} via Java Flow.
 * 
 * @author Virginie Galtier
 *
 */
public class HashtagExtractor implements Subscriber<Tweet>, Publisher<String> {
	/**
	 * List of objects to notify when a hashtag is extracted (downstream component =
	 * HashtagCounter)
	 */
	protected List<Subscriber<? super String>> subscribers;

	/**
	 * Twitter lib utility object
	 */
	final Extractor twitterTextExtractor = new Extractor();

	public HashtagExtractor() {
		subscribers = new ArrayList<Subscriber<? super String>>();
	}

	@Override
	public void subscribe(Subscriber<? super String> subscriber) {
		subscribers.add(subscriber);
	}

	/**
	 * Triggered when the upstream component (TweetFilter) passes a new Tweet via
	 * Java Flow
	 */
	@Override
	public void onNext(Tweet tweet) {
		if (tweet != null) {
			// extracts the hashtags in the Tweet
			List<String> hashtags = twitterTextExtractor.extractHashtags(tweet.getText());

			// passes down the hashtags to the subscribers
			for (String hashtag : hashtags) {
				for (Subscriber<? super String> s : subscribers) {
					s.onNext(hashtag);
				}
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