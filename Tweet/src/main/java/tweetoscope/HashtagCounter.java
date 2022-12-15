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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.stream.Collectors;

/**
 * 
 * Reacts to the reception of a new hashtag by updating how many times it has
 * been seen so far, and sending to its subscribers the updated list of the most
 * popular ones.
 * <p>
 * Hashtags are received via Java Flow from the upstream component
 * {@link distributed_tweetoscope.HashtagExtractor}. Downstream component
 * ({@link distributed_tweetoscope.Visualiazor}) is notified of the new leader
 * board data via Java Flow.
 * 
 * @author Virginie Galtier
 *
 */
public class HashtagCounter implements Subscriber<String>, Publisher<Map<String, Integer>> {
	/**
	 * List of objects to notify when the map is updated (downstream component =
	 * Visualizor)
	 */
	protected List<Subscriber<? super Map<String, Integer>>> subscribers;

	/**
	 * Number of lines to include on the leader board
	 */
	protected int nbLeaders;

	/**
	 * Map <Hashtag text - number of occurrences>
	 */
	protected Map<String, Integer> hashtagOccurrenceMap;

	/**
	 * List of most popular hashtags, used to checked if the list is changed after a
	 * new hashtag is received
	 */
	protected Map<String, Integer> previousLeaderMap;

	/**
	 * 
	 * @param nbLeaders number of hashtags to include on the leader board
	 */
	public HashtagCounter(int nbLeader) {
		this.nbLeaders = nbLeader;
		hashtagOccurrenceMap = new HashMap<String, Integer>();

		subscribers = new ArrayList<Subscriber<? super Map<String, Integer>>>();
	}

	@Override
	public void subscribe(Subscriber<? super Map<String, Integer>> subscriber) {
		subscribers.add(subscriber);
	}

	/**
	 * Triggered by the reception of a new hashtag from a TweetFilter via Java Flow.
	 */
	@Override
	public void onNext(String hashtag) {
		synchronized (hashtagOccurrenceMap) {
			// avoid ConcurrentModificationException when multiple sources are used
			// simultaneously

			// inserts the new tag or increments the number of occurrences if it's not a new
			// tag
			String key = "#" + hashtag;
			if (hashtagOccurrenceMap.containsKey(key)) {
				hashtagOccurrenceMap.replace(key, 1 + hashtagOccurrenceMap.get(key));
			} else {
				hashtagOccurrenceMap.put(key, 1);
			}

			// sorts by number of occurrences and keeps only the top ones
			Map<String, Integer> topHashtagsMap = hashtagOccurrenceMap.entrySet().stream()
					.sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).limit(nbLeaders)
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

			// notifies the subscribers
			if (previousLeaderMap == null || !previousLeaderMap.equals(topHashtagsMap)) {
				for (Subscriber<? super Map<String, Integer>> s : subscribers) {
					s.onNext(topHashtagsMap);
				}
				previousLeaderMap = topHashtagsMap;
			}
			// else the top list is not changed, no need to re-publish it
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