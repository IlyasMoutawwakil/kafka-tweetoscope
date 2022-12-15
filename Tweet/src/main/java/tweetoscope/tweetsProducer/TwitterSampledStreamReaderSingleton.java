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
package tweetoscope.tweetsProducer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.concurrent.Flow.Subscriber;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
//import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
//import com.twitter.clientlib.model.Place;
import com.twitter.clientlib.model.Tweet;
//import com.twitter.clientlib.model.TweetGeo;

/**
 * Connects to the Twitter sampled stream endpoint that delivers a roughly 1%
 * random sample of publicly available Tweets in read-time. As Tweets are
 * received, Tweet subscribers are notified.
 * <p>
 * Requires the BEARER_TOKEN environment variable to be set to connect to the
 * Twitter API endpoint.
 * <p>
 * To ensure only one instance of this class is created at any time, it
 * implements the Singleton pattern.
 * 
 * @author Virginie Galtier
 *
 */
public final class TwitterSampledStreamReaderSingleton extends OnlineTweetsProducer {

	/**
	 * Thread-safe implementation of the Singleton pattern
	 */
	private static volatile TwitterSampledStreamReaderSingleton instance;

	/**
	 * The constructor declares 'private' access to implement the Singleton pattern
	 */
	public TwitterSampledStreamReaderSingleton() {
		super();
	}

	/**
	 * 
	 * @return single instance of the sampled Tweets producer
	 */
	public static TwitterSampledStreamReaderSingleton getInstance() {
		// double-checked locking (DCL)
		// for a thread-safe implementation of the Singleton pattern
		// read more about DLC: https://refactoring.guru/java-dcl-issue
		TwitterSampledStreamReaderSingleton result = instance;
		if (result != null) {
			return result;
		}
		synchronized (TwitterSampledStreamReaderSingleton.class) {
			if (instance == null) {
				instance = new TwitterSampledStreamReaderSingleton();
			}
			return instance;
		}
	}

	@Override
	public void run() {
		try {
			connectStream(BEARER_TOKEN);
		} catch (IOException | URISyntaxException e) {
			System.err.println(e);
			System.exit(-1);
		}
	}

	/**
	 * Calls the sample stream endpoint and reads Tweets from it. Requested Tweet
	 * fields: text and id, author_id, conversation_id, created_at, geo, lang Read
	 * more about the Tweet object model:
	 * https://developer.twitter.com/en/docs/twitter-api/data-dictionary/object-model/tweet
	 * Received Tweets are forwarded to a Java Flow.
	 * 
	 * @param bearerToken Twitter API access token
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private void connectStream(String bearerToken) throws IOException, URISyntaxException {

		HttpClient httpClient = HttpClients.custom()
				.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build()).build();

		URIBuilder uriBuilder = new URIBuilder("https://api.twitter.com/2/tweets/sample/stream?"
				+ "tweet.fields=author_id,conversation_id,created_at,geo,lang");
		// to request less fields:
		/*
		 * URIBuilder uriBuilder = new
		 * URIBuilder("https://api.twitter.com/2/tweets/sample/stream");
		 */
		// to request more fields:
		/*
		 * URIBuilder uriBuilder = new URIBuilder(
		 * "https://api.twitter.com/2/tweets/sample/stream?" +
		 * "tweet.fields=author_id,conversation_id,created_at,geo,lang" +
		 * "&expansions=geo.place_id&place.fields=contained_within,country,country_code,full_name,geo,id,name,place_type"
		 * );
		 */
		HttpGet httpGet = new HttpGet(uriBuilder.build());
		httpGet.setHeader("Authorization", String.format("Bearer %s", bearerToken));

		HttpResponse response = httpClient.execute(httpGet);
		HttpEntity entity = response.getEntity();
		if (null != entity) {
			// TypeAdapter required to deserialize OffsetDateTime
			// see
			// https://stackoverflow.com/questions/60723739/offsetdatetime-deserialization-using-gson
			Gson gson = new GsonBuilder()
					.registerTypeAdapter(OffsetDateTime.class, (JsonDeserializer<OffsetDateTime>) (json, type,
							context) -> OffsetDateTime.parse(json.getAsString()))
					.create();

			Tweet tweet;

			BufferedReader reader = new BufferedReader(new InputStreamReader((entity.getContent())));
			String line = reader.readLine();
			int nbTweets = 0;
			long startTime = System.currentTimeMillis();
			long observingPeriod = 60000;
			while (line != null) {
				if (System.currentTimeMillis() - startTime >= observingPeriod) {
					System.out.println("Sampled stream: " + nbTweets + " tweets received in " + observingPeriod
							+ " milliseconds.");
					nbTweets = 0;
					startTime = System.currentTimeMillis();
				}
				if (!line.equals("")) {
					if (line.equals("Rate limit exceeded")) {
						System.out.println(
								"Twitter sampled stream rate limit exceeded (50 requests per 15-minute window, "
										+ "see https://developer.twitter.com/en/docs/twitter-api/rate-limits). "
										+ "Try again in 15 minutes.");
						System.exit(0);
					}
					/*
					 * Example of Tweet: { "data": { "author_id":"1502984699761008641",
					 * "conversation_id":"1526569705150238721",
					 * "created_at":"2022-05-17T14:25:54.000Z",
					 * "geo":{"place_id":"8dedbfcdb8337d67"}, "id":"1526569705150238721",
					 * "lang":"ja",
					 * "text":"Photoshopをようやくダウンロード出来たけど編集した写真がどこに保存されてるのかがわからない…( ´∀｀)ﾜｶﾗﾝ\n\n#Photoshop初心者"
					 * },
					 * "includes":{ "places":[ { "country":"Japan", "country_code":"JP",
					 * "full_name":"Kamagaya-shi, Chiba", "geo":{ "type":"Feature",
					 * "bbox":[139.96826,35.738088,140.028681,35.804155], "properties":{}},
					 * "id":"8dedbfcdb8337d67", "name":"Kamagaya-shi", "place_type":"city" }] }}
					 */
					tweet = gson.fromJson(gson.fromJson(line, JsonObject.class).get("data"), Tweet.class);
					for (Subscriber<? super Tweet> s : subscribers) {
						s.onNext(tweet);
					}
					nbTweets++;
					/*
					 * JsonElement include = gson.fromJson(line, JsonObject.class).get("includes");
					 * if (include != null) { JsonElement jsonPlace =
					 * include.getAsJsonObject().get("places").getAsJsonArray().get(0); Place place
					 * = gson.fromJson(jsonPlace, Place.class); System.out.println(place); }
					 */
				}
				line = reader.readLine();
			}
		}
	}
}