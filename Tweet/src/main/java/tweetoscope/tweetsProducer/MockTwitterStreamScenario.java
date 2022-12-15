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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.Flow.Subscriber;

import com.twitter.clientlib.model.Tweet;

/**
 * Mimics the TwitterStreamSampleReaderSingleton class. To be used when the
 * Twitter sampled stream rate limit is exceeded for instance. Creates and
 * publishes a predefined set of Tweets.
 * 
 * @author Virginie Galtier
 *
 */
public final class MockTwitterStreamScenario extends OfflineTweetsProducer {

	/**
	 * Creates a MockTwitterStreamScenario.
	 * 
	 */
	public MockTwitterStreamScenario() {
		super();
	}

	/**
	 * Crafts a predefined set of Tweets and posts them to the Java Flow
	 */
	@Override
	public void run() {
		Tweet[] tweets = new Tweet[10];
		
		tweets[0] = new Tweet();
		tweets[0].setId("001");
		tweets[0].setCreatedAt(OffsetDateTime.of(2022, 6, 20, 11, 46, 23, 0, ZoneOffset.UTC));
		tweets[0].setText("Choisissez un #travail que vous aimez et vous n'aurez pas à travailler un seul jour de votre vie");
		tweets[0].setAuthorId("31");
		tweets[0].setConversationId("01");
		tweets[0].getGeo();
		tweets[0].setLang("fr");
		
		tweets[1] = new Tweet();
		tweets[1].setId("002");
		tweets[1].setCreatedAt(OffsetDateTime.of(2022, 6, 20, 11, 48, 20, 0, ZoneOffset.UTC));
		tweets[1].setText("Si on travaille pour gagner sa vie, pourquoi se tuer au #travail ?");
		tweets[1].setAuthorId("31");
		tweets[1].setConversationId("01");
		tweets[1].getGeo();
		tweets[1].setLang("fr");
		
		tweets[2] = new Tweet();
		tweets[2].setId("003");
		tweets[2].setCreatedAt(OffsetDateTime.of(2022, 6, 24, 6, 6, 13, 0, ZoneOffset.UTC));
		tweets[2].setText("#Failure is not the opposite of #success: it’s part of success.");
		tweets[2].setAuthorId("32");
		tweets[2].setConversationId("01");
		tweets[2].getGeo();
		tweets[2].setLang("en");

		tweets[3] = new Tweet();
		tweets[3].setId("004");
		tweets[3].setCreatedAt(OffsetDateTime.of(2022, 7, 2, 4, 44, 17, 0, ZoneOffset.UTC));
		tweets[3].setText("You are not your resume, you are your #work.");
		tweets[3].setAuthorId("34");
		tweets[3].setConversationId("01");
		tweets[3].getGeo();
		tweets[3].setLang("en");

		tweets[4] = new Tweet();
		tweets[4].setId("005");
		tweets[4].setCreatedAt(OffsetDateTime.of(2022, 8, 20, 10, 49, 23, 0, ZoneOffset.UTC));
		tweets[4].setText("People who wonder if the glass is half empty or half full miss the point. The glass is refillable.");
		tweets[4].setAuthorId("35");
		tweets[4].setConversationId("03");
		tweets[4].getGeo();
		tweets[4].setLang("en");

		tweets[5] = new Tweet();
		tweets[5].setId("006");
		tweets[5].setCreatedAt(OffsetDateTime.of(2022, 8, 20, 11, 23, 31, 0, ZoneOffset.UTC));
		tweets[5].setText("If you think you are too small to make a difference, try sleeping with a mosquito.");
		tweets[5].setAuthorId("33");
		tweets[5].setConversationId("04");
		tweets[5].getGeo();
		tweets[5].setLang("en");

		tweets[6] = new Tweet();
		tweets[6].setId("007");
		tweets[6].setCreatedAt(OffsetDateTime.of(2022, 8, 30, 2, 15, 0, 0, ZoneOffset.UTC));
		tweets[6].setText("Nothing will #work unless you do.");
		tweets[6].setAuthorId("34");
		tweets[6].setConversationId("03");
		tweets[6].getGeo();
		tweets[6].setLang("en");

		tweets[7] = new Tweet();
		tweets[7].setId("008");
		tweets[7].setCreatedAt(OffsetDateTime.of(2022, 9, 1, 8, 30, 20, 0, ZoneOffset.UTC));
		tweets[7].setText("If you get tired, learn to rest, not to quit.");
		tweets[7].setAuthorId("35");
		tweets[7].setConversationId("05");
		tweets[7].getGeo();
		tweets[7].setLang("en");

		tweets[8] = new Tweet();
		tweets[8].setId("009");
		tweets[8].setCreatedAt(OffsetDateTime.of(2022, 9, 2, 7, 55, 56, 0, ZoneOffset.UTC));
		tweets[8].setText("#Failure is #success in progress.");
		tweets[8].setAuthorId("33");
		tweets[8].setConversationId("02");
		tweets[8].getGeo();
		tweets[8].setLang("en");

		tweets[9] = new Tweet();
		tweets[9].setId("010");
		tweets[9].setCreatedAt(OffsetDateTime.of(2022, 9, 3, 12, 40, 25, 0, ZoneOffset.UTC));
		tweets[9].setText("The only place #success comes before #work is in the dictionary.");
		tweets[9].setAuthorId("36");
		tweets[9].setConversationId("07");
		tweets[9].getGeo();
		tweets[9].setLang("en");

		for (int i = 0; i < tweets.length; i++) {
			for (Subscriber<? super Tweet> s : subscribers) {
				s.onNext(tweets[i]);//subscribe each tweet.
			}
		}
	}
}