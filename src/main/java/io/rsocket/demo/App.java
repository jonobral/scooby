package io.rsocket.demo;

import io.rsocket.*;
import io.rsocket.transport.netty.server.WebsocketRouteTransport;
import io.rsocket.transport.netty.server.WebsocketServerTransport;
import io.rsocket.util.DefaultPayload;
import org.springframework.social.connect.Connection;
import org.springframework.social.twitter.api.Stream;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.Twitter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRoutes;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

public class App {
  public static void main(String[] args) {
    Connection<org.springframework.social.twitter.api.Twitter> twitter = TwitterFactory.connect();

    Consumer<HttpServerRoutes> routeSetup =
        routes -> {
          routes.file("/", webPath("src/main/resources/web/index.html"));
          routes.directory("/public", webPath("src/main/resources/web/public"));
        };
    Closeable server =
        RSocketFactory.receive()
            .acceptor(
                (setupPayload, reactiveSocket) ->
                    createServerRequestHandler(twitter.getApi(), setupPayload))
            .transport(WebsocketServerTransport.create("localhost",7000))
            .start()
            .block();

    System.out.println("running on " + port());

    server.onClose().block(Duration.ofDays(3650));
  }

  private static Path webPath(String pathname) {
    return new File(pathname).getAbsoluteFile().toPath();
  }

  private static int port() {
    return Integer.getInteger("server.port", 8080);
  }

  private static Mono<RSocket> createServerRequestHandler(
      Twitter twitter, ConnectionSetupPayload setupPayload) {

    return Mono.just(
        new AbstractRSocket() {
          @Override
          public Mono<Payload> requestResponse(Payload payload) {
            return Mono.just(payload);
          }

          @Override
          public Flux<Payload> requestStream(Payload payload) {
            String query = StandardCharsets.UTF_8.decode(payload.getData()).toString();
            return twitterSearch(query, twitter).take(10000);
          }
        });
  }

  private static Flux<Payload> twitterSearch(String query, Twitter twitter) {
    return Flux.create(
        s -> {
          System.out.println("Opening " + query);
          Stream stream =
              twitter
                  .streamingOperations()
                  .filter(
                      query,
                    Collections.singletonList(
                      new StreamAdapter() {
                        @Override
                        public void onTweet(Tweet tweet) {
                          s.next(DefaultPayload.create("@" + tweet.getFromUser() + " - " + tweet.getText()));
                          System.out.println(tweet.getText());
                        }
                      }));

          s.onCancel(
              () -> {
                System.out.println("Closing " + query);
                stream.close();
              });
        },
        FluxSink.OverflowStrategy.DROP);
  }
}
