import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static java.lang.System.out;
import static java.util.stream.Collectors.joining;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class App {
    static final String[] urls = { "https://stackoverflow.com/", "https://github.com/", "http://dzone.com/" };

    static final String[] urlsFail = { "http://ajsgdjgasd.com", "https://stackoverflow.com/", "https://github.com/",
            "http://dzone.com/", "http://example.com/", "https://developer.mozilla.org/", "https://openjdk.java.net/" };

    static HttpClient httpClient = HttpClient.newHttpClient();
    static HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();

    static HttpRequest request(String url) {
        return requestBuilder.uri(URI.create(url)).build();
    }

    static int fetchAndSumBlocking(String...urls) throws MalformedURLException, IOException {
        int sum = 0;
        for(var url : urls) {
            out.printf("FETCHING from %s\n", url);
            InputStream resp = new URL(url).openStream();                 // 1 - Fetch the url
            String body = new BufferedReader(new InputStreamReader(resp)) // 2 - Read the body
                .lines()
                .collect(joining("\n"));
            int length = body.length();  // 3 – Get body’s length
            out.printf("=======> from %s with %d\n", url, length);
            sum += length;               // 4 - Sum lengths
        }
        return sum;
    }

    static CompletableFuture<Integer> fetchAndSum(String...urls) {
        CompletableFuture<Integer> sum = CompletableFuture.completedFuture(0);
        return Stream
                .of(urls)
                .peek(url -> out.printf("FETCHING from %s\n", url))
                .map(url -> httpClient
                    .sendAsync(request(url), BodyHandlers.ofString()) // 1 - Fetch the url
                    .thenApply(HttpResponse::body)                    // 2 - Read the body
                    .thenApply(String::length)                        // 3 – Get body’s length
                    .whenComplete((l, err) -> out.printf("=======> from %s\n", url)))
                .reduce(sum, (prev, curr) -> prev
                    .thenCombine(curr, (p, c) -> p + c));             // 4 - Sum lengths
    }

    static CompletableFuture<Integer> fetchAndSumStrm(String...urls) {
        return Stream
            .of(urls)
            .peek(url -> out.printf("FETCHING from %s\n", url))
            .map(url -> {
                    var res = httpClient.sendAsync(request(url), BodyHandlers.ofString());
                    return pair(res, res
                        .thenApply(HttpResponse::body)
                        .thenApply(String::length)
                        .whenComplete((size, err) ->
                            out.printf("=======> from %s with %d\n", url, size))  
                        .exceptionally(ex -> { 
                            throw new RuntimeException("response from " + url, ex);
                        }));
            })
            .reduce((sum, curr) -> {
                if(sum.key.isCompletedExceptionally()) {
                    curr.key.cancel(true);
                    return sum;
                } 
                return pair(curr.key, sum.val.thenCompose(s -> curr.val.thenApply(c -> s + c)));
            })
            .get()
            .val;
    }

    public static void main(String[] args) throws Exception{
        int count = fetchAndSumBlocking(urls);
        out.printf("Total chars = %d\n", count);

        // fetchAndSumStrm(urlsFail)
        fetchAndSum(urls)
            .thenAccept(total -> out.printf("Total chars = %d\n", total))
            .exceptionally(ex -> { out.println(ex); return null; })
            .join();
    }

    static <K, V> Pair<K, V> pair(K k, V v) {
        return new Pair<>(k, v);
    }

    static class Pair<K, V> {
        final K key;
        final V val; 
        public Pair(K k, V v) {
            this.key = k;
            this.val = v;
        }
    }

}