package tweetoscope.tweetsFilter;

import com.twitter.clientlib.model.Tweet;


public class TextLengthFilter extends TweetFilter{
    protected int length;

    public TextLengthFilter(int length) {
        this.length = length;
    }//Constructor of class

    @Override
    protected boolean match(Tweet tweet) {
        String text= tweet.getText();
        return text.length()<=this.length;
    }

}
