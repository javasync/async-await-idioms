## Kotlin sample looking concurrency in fetchAndSum()

Compile with `gradlew build` and run `gradlew run`

Comparing the use of `foreach` loop **without** lambdas and the use of a pipeline
(such as `....asSequence().map(...).reduce(...)`) **with** lambdas and the resulting 
_sequential_ versus _concurrent_ behavior.

<table>
<tr>
<td>

```kotlin
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
```

</td>
<td>

```kotlin
suspend fun CoroutineScope.fetchAndSumλ(vararg urls: String): Int {
  return urls
    .asSequence()
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
```

</td>
</tr>
<tr>
<td>

```
FETCHING from https://stackoverflow.com/
=======> from https://stackoverflow.com/
FETCHING from https://github.com/
=======> from https://github.com/
FETCHING from http://dzone.com/
=======> from http://dzone.com/
Total chars = 470831
```

</td>
<td>

```
FETCHING from https://stackoverflow.com/
FETCHING from https://github.com/
FETCHING from http://dzone.com/
=======> from https://github.com/
=======> from https://stackoverflow.com/
=======> from http://dzone.com/
Total chars = 470831
```

</td>
</tr>
</table>