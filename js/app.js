const fetch = require('node-fetch')

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

async function fetchAndSumConcur(...urls) {
    let sum = 0
    let promises = []
    for (const url of urls) {
        console.log(`FETCHING from ${url}`)
        promises.push(fetch(url))
    }
    for (let i = 0; i < urls.length; i++) {
        const resp = await promises[i]
        const body = await resp.text()
        console.log(`=======> from ${urls[i]}`)
        sum += body.length
    }
    return sum
}


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

async function main() {
    var total = await fetchAndSum("https://stackoverflow.com/", "https://github.com/", "http://dzone.com/")
    console.log(`Total chars = ${total}`)
    total = await fetchAndSumConcur("https://stackoverflow.com/", "https://github.com/", "http://dzone.com/")
    console.log(`Total chars = ${total}`)
    total = await fetchAndSumλ("https://stackoverflow.com/", "https://github.com/", "http://dzone.com/")
    console.log(`Total chars = ${total}`)
}

main()