## JavaScript sample looking concurrency in fetchAndSum()

Install with: `npm install` and run `node app`.

Comparing the use of `foreach` loop **without** lambdas and the use of a
collection pipeline (such as `....map(...).reduce(...)`) **with** lambdas
and the resulting 
_sequential_ versus _concurrent_ behavior.

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