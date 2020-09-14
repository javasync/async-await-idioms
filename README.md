## Stretching Async/Await With Lambdas

Comparing the use of `foreach` loop **without** lambdas and the use of a pipeline
(such as `....map(..->...).reduce(..->...)`) **with** lambdas and the resulting 
_sequential_ versus _concurrent_ behavior in different programming languages
Java, Js, C# and Kotlin.

Full article here: [_Stretching Async/Await With Lambdas_](https://dzone.com/articles/lambdas-in-concurrency-with-non-blocking-io)

### Javascript

<table>
<tr>
<td>

```js
async function fetchAndSum(...urls) {
  let sum = 0
  for (const url of urls) {
    console.log(`FETCHING from ${url}`)
    const res = await fetch(url)
    const body = await res.text()
    console.log(`=======> from ${url}`)
    sum += body.length
  }
  return sum
}

```

</td>
<td>

```js
async function fetchAndSumλ(...urls) {
return urls
  .map(async (url, i) => {
    console.log(`FETCHING from ${url}`)
    const resp = await fetch(url)
    const body = await resp.text()
    const length = body.length
    console.log(`=======> from ${urls[i]}`)
    return length
  })
  .reduce(async (prev, curr) => await prev + await curr)
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

### C\#

<table>
<tr>
<td>

```csharp
static async Task<int> FetchAndSum(string[] urls) {
  int sum = 0;
  using(HttpClient httpClient = new HttpClient()) {
    foreach(var url in urls) {
      Console.WriteLine($"FETCHING from {url}");
      var body = await httpClient.GetStringAsync(url);
      Console.WriteLine($"=======> from {url}");
      sum += body.Length;
    }
  }
  return sum;
}
```

</td>
<td>

```csharp
static async Task<int> FetchAndSumλ(string[] urls) {
  using(HttpClient httpClient = new HttpClient()) {
    return await urls
      .Select(async url => {
        Console.WriteLine($"FETCHING from {url}");
        var body = await httpClient.GetStringAsync(url);
        Console.WriteLine($"=======> from {url}");
        return body.Length;
      })
      .Aggregate(async (prev, curr) => await prev + await curr);
  }
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

### Kotlin

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

