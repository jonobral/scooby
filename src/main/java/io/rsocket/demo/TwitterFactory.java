package io.rsocket.demo;

import org.springframework.social.connect.Connection;
import org.springframework.social.oauth1.OAuthToken;
import org.springframework.social.twitter.connect.TwitterConnectionFactory;

public class TwitterFactory {
  public static Connection<org.springframework.social.twitter.api.Twitter> connect() {
    TwitterConnectionFactory c = new TwitterConnectionFactory("I02uZjf5RUtcW6nR8DGAkW1hn", "1JxqLaen2ZYYqJnXDdtpqUpTHQ2OvtdQ5oNJvpodPzFOaXieXM");
    return c.createConnection(new OAuthToken("81008556-zOscgIMW3Dz6ZGHvDo29CUa3XsHtOsoTXOf3A1W46", "P2Lc3fg5TCHb0cBAsG2565daOaH5jiEli4ghR6isqJxB6"));
  }
}
