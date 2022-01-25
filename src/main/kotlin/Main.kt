import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers


fun main(args: Array<String>) {
    println("Hello World!")

    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    println("Program arguments: ${args.joinToString()}")

    val client: HttpClient = HttpClient.newHttpClient()
    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://www.f2pool.com/mining-user-eth/c2f2a494800ce54d722cf95b77aa2ed4?draw=1&columns%5B0%5D%5Bdata%5D=name&columns%5B0%5D%5Bname%5D=&columns%5B0%5D%5Bsearchable%5D=true&columns%5B0%5D%5Borderable%5D=true&columns%5B0%5D%5Bsearch%5D%5Bvalue%5D=&columns%5B0%5D%5Bsearch%5D%5Bregex%5D=false&columns%5B1%5D%5Bdata%5D=local_hash&columns%5B1%5D%5Bname%5D=&columns%5B1%5D%5Bsearchable%5D=false&columns%5B1%5D%5Borderable%5D=true&columns%5B1%5D%5Bsearch%5D%5Bvalue%5D=&columns%5B1%5D%5Bsearch%5D%5Bregex%5D=false&columns%5B2%5D%5Bdata%5D=hashrate&columns%5B2%5D%5Bname%5D=&columns%5B2%5D%5Bsearchable%5D=false&columns%5B2%5D%5Borderable%5D=true&columns%5B2%5D%5Bsearch%5D%5Bvalue%5D=&columns%5B2%5D%5Bsearch%5D%5Bregex%5D=false&columns%5B3%5D%5Bdata%5D=hashrate_last_day&columns%5B3%5D%5Bname%5D=&columns%5B3%5D%5Bsearchable%5D=false&columns%5B3%5D%5Borderable%5D=true&columns%5B3%5D%5Bsearch%5D%5Bvalue%5D=&columns%5B3%5D%5Bsearch%5D%5Bregex%5D=false&columns%5B4%5D%5Bdata%5D=delayrate_last_day&columns%5B4%5D%5Bname%5D=&columns%5B4%5D%5Bsearchable%5D=false&columns%5B4%5D%5Borderable%5D=true&columns%5B4%5D%5Bsearch%5D%5Bvalue%5D=&columns%5B4%5D%5Bsearch%5D%5Bregex%5D=false&columns%5B5%5D%5Bdata%5D=stalerate_last_day&columns%5B5%5D%5Bname%5D=&columns%5B5%5D%5Bsearchable%5D=false&columns%5B5%5D%5Borderable%5D=true&columns%5B5%5D%5Bsearch%5D%5Bvalue%5D=&columns%5B5%5D%5Bsearch%5D%5Bregex%5D=false&columns%5B6%5D%5Bdata%5D=last_share&columns%5B6%5D%5Bname%5D=&columns%5B6%5D%5Bsearchable%5D=false&columns%5B6%5D%5Borderable%5D=true&columns%5B6%5D%5Bsearch%5D%5Bvalue%5D=&columns%5B6%5D%5Bsearch%5D%5Bregex%5D=false&columns%5B7%5D%5Bdata%5D=tag_name&columns%5B7%5D%5Bname%5D=&columns%5B7%5D%5Bsearchable%5D=false&columns%5B7%5D%5Borderable%5D=false&columns%5B7%5D%5Bsearch%5D%5Bvalue%5D=&columns%5B7%5D%5Bsearch%5D%5Bregex%5D=false&columns%5B8%5D%5Bdata%5D=name&columns%5B8%5D%5Bname%5D=&columns%5B8%5D%5Bsearchable%5D=true&columns%5B8%5D%5Borderable%5D=false&columns%5B8%5D%5Bsearch%5D%5Bvalue%5D=&columns%5B8%5D%5Bsearch%5D%5Bregex%5D=false&start=0&length=50&search%5Bvalue%5D=&search%5Bregex%5D=false&account=hundreds&currency=ETH&tagOnly=0&tagview=true&tab=all&action=get_pagination_workers&filter=&_=1642493099803"))
        //.header("accept", "application/json, text/javascript, */*; q=0.01")
        //.header("referer","https://www.f2pool.com/mining-user-eth/c2f2a494800ce54d722cf95b77aa2ed4")
        //.header("authority", "www.f2pool.com")
        .header("x-requested-with", "XMLHttpRequest").build()
    client.sendAsync(request, BodyHandlers.ofString())
        .thenApply { obj: HttpResponse<*> -> obj.body() }
        //.thenApply(System.out::println)
        .thenApply { body -> getMinerData(body as? String)}
        .thenApply { minerList -> checkAndAlert(minerList) }
        .join()
}


fun checkAndAlert(minerList: List<Miner>?) {
    val client: HttpClient = HttpClient.newHttpClient()
    val request = HttpRequest.newBuilder()
        .uri(URI.create("http://yzxyytz.market.alicloudapi.com/yzx/voiceNotifySms?phone=13901658165&variable=sdff&templateId=TP18040817"))
        //.header("accept", "application/json, text/javascript, */*; q=0.01")
        //.header("referer","https://www.f2pool.com/mining-user-eth/c2f2a494800ce54d722cf95b77aa2ed4")
        //.header("authority", "www.f2pool.com")
        .header("Authorization", "APPCODE 13fd44d2dc5044b3a961715175419d89")
        .header("X-Ca-Timestamp", "1642492714753")
        .header("gateway_channel", "http")
        .headers("x-ca-nonce", "9a08c686-f2b3-4a15-91e0-4685b941e6e1")
        .headers("X-Ca-Request-Mode", "DEBUG")
        .headers("X-Ca-Stage", "RELEASE")
        .headers("X-Ca-Supervisor-Token", "eyJhbGciOiJSUzI1NiJ9.eyJpYXQiOjE2NDI0OTI3MTQsIm5iZiI6MTY0MjQ5MjY1NCwiaXNzIjoiQWxpeXVuQXBpR2F0ZXdheSIsInJvbGUiOiJ1c2VyIiwiYXVkIjoiYXBpLXNoYXJlZC12cGMtMDAyIiwiZXhwIjoxNjQyNDkzNjE0LCJqdGkiOiI1ZDhjNDRlYzVmNTk0OTcyYjg1ZDkwODgyOTU1ODhiMiIsImFjdGlvbiI6IkRFQlVHIiwidWlkIjoiMTM1NDkxNTE5NDU1MDc4MCJ9.SmGujprZxJvfP2kOQwz1eLmhQ_ZPTE1zQKdFoyfdQQSfterE7nuA-UFekmUyMVUeYHEyjuCzMgi5efEdzp66oj6lNYgGp-48H5Zsg-ZNf3FMZ9DN_89tenFWhZ_IQtmtDSrDL3PS0xWSgYF77-fZ5MA4Ta5YqX7k_T9CbkF0oTZnrin0tzZSzSwhrGdtWk9XUAyxinEbsSWZwy8k2iK9_aI5ZY4HeVvdTMBzos612WmZ-L_lxWPrx856KpaGuDNjWnLpzq9WPyCe2-FhBbAKijqDqFNfjRH7UR5mDogjZn5Rlbzp36OyEGZCxMg1tncLvQHUyS18E6MLez0B8jHSOQ")
        .headers("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
        .build()

    val httpResponse = client.send(request, BodyHandlers.ofString())
    println(httpResponse.body())

    GsmNotifier.run(115200)
}

fun getMinerData(json: String?): List<Miner> {
    val jsonString = json ?: """{\"k1":"v1","k2":"v2"}"""
    val mapper: ObjectMapper = ObjectMapper()
    val actualObj: JsonNode = mapper.readTree(jsonString)

    // When
    val jsonNode1: JsonNode = actualObj.get("data")
    println(jsonNode1.textValue())
    val miners = (jsonNode1 as? ArrayNode)?.mapIndexed { index, jsonNode ->
        println("$index, $jsonNode")
        val miner: Miner = mapper.treeToValue(jsonNode, Miner::class.java)
        println(miner)
        miner
    }

    return miners ?: emptyList()
}

