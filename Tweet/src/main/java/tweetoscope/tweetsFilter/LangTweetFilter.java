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

import com.twitter.clientlib.model.Tweet;

/**
 * Filters Tweets according to the detected language.
 * 
 * @author Virginie Galtier
 *
 */
public class LangTweetFilter extends TweetFilter {

	/**
	 * target language to match (examples: "fr", "en"...)
	 */
	protected String language;

	/**
	 * Creates a filter that tests whether the "language" tag of a Tweet (if it is
	 * set) equals a given code.
	 * 
	 * @param language target language to match (example: "en")
	 */
	public LangTweetFilter(String language) {
		this.language = language;
	}

	@Override
	protected boolean match(Tweet tweet) {
		return tweet.getLang().equals(language);
	}

}
