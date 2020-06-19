using System;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Threading.Tasks;

static class App {

    static readonly string[] urls = {
        "https://stackoverflow.com/",
        "https://github.com/",
        "http://dzone.com/"
    };

    static async Task<int> FetchAndSum(string[] urls) {
        int sum = 0;
        using(HttpClient httpClient = new HttpClient()) {
            ServicePointManager.SecurityProtocol = SecurityProtocolType.Tls12;
            foreach(var url in urls) {
                Console.WriteLine($"FETCHING from {url}");
                var body = await httpClient.GetStringAsync(url);
                Console.WriteLine($"=======> from {url}");
                sum += body.Length;
            }
        }
        return sum;
    }

    static async Task<int> FetchAndSumλ(string[] urls) {
        using(HttpClient httpClient = new HttpClient()) {
            ServicePointManager.SecurityProtocol = SecurityProtocolType.Tls12;
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
    static void Main() {
        FetchAndSum(urls)
            .ContinueWith(total => Console.WriteLine($"Total chars = {total.Result}"))
            .Wait();
        FetchAndSumλ(urls)
            .ContinueWith(total => Console.WriteLine($"Total chars = {total.Result}"))
            .Wait();

    }
}



