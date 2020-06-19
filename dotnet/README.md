## Csharp sample looking concurrency in FetchAndSum()

Compile with: 
```
csc /r:System.Net.Http.dll app.cs
```
and run `App.exe`.

Comparing the use of `foreach` loop **without** lambdas and the use of a pipeline
(such as `....Select(...).Aggregate(...)`) **with** lambdas and the resulting 
_sequential_ versus _concurrent_ behavior.

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