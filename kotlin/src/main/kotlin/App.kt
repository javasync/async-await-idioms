import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers
import java.util.concurrent.CompletableFuture
import java.util.stream.Stream

import kotlinx.coroutines.*
import kotlinx.coroutines.future.*

val httpClient = HttpClient.newHttpClient()
val requestBuilder = HttpRequest.newBuilder()
    
fun request(url: String): HttpRequest {
     return requestBuilder.uri(URI.create(url)).build()
}

suspend fun fetchAndSum(vararg urls: String): Int {
    var sum = 0
    for (url in urls) {
        println("FETCHING from $url")
        val resp = httpClient
            .sendAsync(request(url), BodyHandlers.ofString())
            .await()
        println("=======> from $url")
        sum = sum + resp.body().length
    }
    return sum
}

suspend fun fetchAndSumλ(vararg urls: String): Int {
    return urls
            .map { url ->
                println("FETCHING from $url")
                val resp = httpClient
                        .sendAsync(request(url), BodyHandlers.ofString())
                        .await()
                println("=======> from $url")
                resp.body().length
            }
            .reduce { prev, curr -> prev + curr}
}


suspend fun CoroutineScope.fetchAndSumλ(urls: Sequence<String>): Int {
    return urls
            .map { url -> async {
                    println("FETCHING from $url")
                    val resp = httpClient
                            .sendAsync(request(url), BodyHandlers.ofString())
                            .await()
                    println("=======> from $url")
                    resp.body().length
            }}
            .reduce { prev, curr -> async {
                prev.await() + curr.await()
            }}
            .await();
}


fun main(args: Array<String>) {
    runBlocking {
        val total = fetchAndSum(
            "https://stackoverflow.com/", 
            "https://github.com/", 
            "http://dzone.com/")
        println("Total chars = $total")
    }
    runBlocking {
        val total = fetchAndSumλ(
            "https://stackoverflow.com/", 
            "https://github.com/", 
            "http://dzone.com/")
        println("Total chars = $total")
    }
    runBlocking {
        val total = fetchAndSumλ(sequenceOf(
            "https://stackoverflow.com/", 
            "https://github.com/", 
            "http://dzone.com/"))
        println("Total chars = $total")
    }
}
