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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.twitter.clientlib.ApiException;
import com.twitter.clientlib.model.Place;
import com.twitter.clientlib.model.SingleTweetLookupResponse;
import com.twitter.clientlib.model.Tweet;

/**
 * Filters Tweets according to their country code
 * 
 * @author Virginie Galtier
 *
 */
public class CountryCodeTweetFilter extends OnlineTweetFilter {

	/**
	 * The targeted country code to match
	 */
	protected String countryCode;

	/**
	 * Creates a filter that queries the Twitter API to retrieve the country code
	 * associated with the "place" tag of a Tweet (if it is set) and tests whether
	 * its value equals a given code.
	 * 
	 * @param countryCode targeted country code to match (example: "us")
	 */
	public CountryCodeTweetFilter(String countryCode) {
		this.countryCode = countryCode;
	}

	@Override
	protected boolean match(Tweet tweet) {
		String tweetCountryCode = getCountryCode2(tweet);
		return (tweetCountryCode != null) && (tweetCountryCode.equals(countryCode));
	}

	/**
	 * Returns the country code of the place tagged in a Tweet, if any. Version 1:
	 * manually, provided as an example
	 * 
	 * @param tweet
	 * @return the country code of the place tagged in a Tweet if the information is
	 *         available, null otherwise
	 */
	protected String getCountryCode(Tweet tweet) {
		if (tweet.getGeo().getPlaceId() == null)
			return null;
		try {
			// to get the country_code from the placeId set in the Tweet, a new request to
			// the Twitter API is required
			// see
			// https://developer.twitter.com/en/docs/twitter-api/data-dictionary/object-model/place
			HttpClient httpClient = HttpClients.custom()
					.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
					.build();
			URIBuilder uriBuilder = new URIBuilder(
					"https://api.twitter.com/2/tweets?ids=" + tweet.getId() + "&expansions=geo.place_id"
							+ "&place.fields=contained_within,country,country_code,full_name,geo,id,name,place_type");
			HttpGet httpGet = new HttpGet(uriBuilder.build());
			httpGet.setHeader("Authorization", String.format("Bearer %s", BEARER_TOKEN));

			HttpResponse response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			if (null != entity) {
				Gson gson = new GsonBuilder()
						.registerTypeAdapter(OffsetDateTime.class, (JsonDeserializer<OffsetDateTime>) (json, type,
								context) -> OffsetDateTime.parse(json.getAsString()))
						.create();
				BufferedReader reader = new BufferedReader(new InputStreamReader((entity.getContent())));
				String line = reader.readLine();
				while (line != null) {
					JsonElement include = gson.fromJson(line, JsonObject.class).get("includes");
					if (include != null) {
						JsonElement jsonPlace = include.getAsJsonObject().get("places").getAsJsonArray().get(0);
						Place place = gson.fromJson(jsonPlace, Place.class);
						return place.getCountryCode();
					}
					line = reader.readLine();
				}
			}
		} catch (URISyntaxException | IOException e) {
			System.err.println("error while retreiving the country code: " + e);
		}
		return null;
	}

	/**
	 * Returns the country code of the place tagged in a Tweet, if any. Version 2:
	 * using the findTweetId function of the Twitter API
	 * 
	 * @param tweet
	 * @return the country code of the place tagged in a Tweet if the information is
	 *         available, null otherwise
	 */
	private String getCountryCode2(Tweet tweet) {
		if (tweet.getGeo().getPlaceId() != null) {
			try {
				Set<String> expansions = new HashSet<>(Arrays.asList("geo.place_id"));
				Set<String> tweetFields = new HashSet<>(
						Arrays.asList("created_at", "text", "author_id", "conversation_id", "geo", "lang"));
				Set<String> userFields = null;
				Set<String> mediaFields = null;
				Set<String> placeFields = new HashSet<>(Arrays.asList("country_code"));
				Set<String> pollFields = null;
				SingleTweetLookupResponse response = twitterApiInstance.tweets().findTweetById(tweet.getId(),
						expansions, tweetFields, userFields, mediaFields, placeFields, pollFields);
				if (response.getIncludes() != null && response.getIncludes().getPlaces() != null) {
					return response.getIncludes().getPlaces().get(0).getCountryCode();
				}
			} catch (ApiException e) {
				System.err.println("error while retreiving the country code: " + e.getResponseBody());
			}
		}
		return null;
	}

}
