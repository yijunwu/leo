import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import java.lang.Exception
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

var urlMap: Map<String, String> = sortedMapOf(
)

var minerAccountSpecs: Map<String, List<Miner>> = mapOf(
    "hundreds" to listOf(
    ),
    "ktminer" to listOf(
    )
)

var contact = "13901234567"

fun main(args: Array<String>) {
    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    println("Program arguments: ${args.joinToString()}")

    if (args.isNotEmpty()) {
        configParams(args)
    }

    while (true) {
        urlMap.keys.forEach { account ->
            fetchCheckAndAlert(account)
            waitForMinutes(1)
        }

        waitForMinutes(5)
    }
}

fun configParams(args: Array<String>) {
    val configFile: String = if (args.size == 2 && args[0] == "-C") {
        args[1]
    } else {
        return
    }
    val mapper = ObjectMapper()
    val config: JsonNode = mapper.readTree(Files.readAllLines(Paths.get(configFile)).joinToString(""))

    // When
    val jsonNode1: JsonNode = config.get("contact")
    contact = jsonNode1.textValue()

    val minerAccountSpecsNode : JsonNode = config.get("minerAccountSpecs")

    val urlMapNode: JsonNode = config.get("urlMap")
    urlMap = mapper.convertValue(urlMapNode, object: TypeReference<Map<String, String>>(){})

    minerAccountSpecs = mapper.convertValue(minerAccountSpecsNode, object: TypeReference<Map<String, List<Miner>>>(){})

    val miners = (jsonNode1 as? ArrayNode)?.mapIndexed { index, jsonNode ->
        println("$index, $jsonNode")
        val miner: Miner = mapper.treeToValue(jsonNode, Miner::class.java)
        println(miner)
        miner
    }
}

private fun waitForMinutes(minutes: Int) {
    try {
        Thread.sleep(1000L)
        Thread.yield()
    } catch (ignored: InterruptedException) {
    }

    try {
        println("[${Date()}]Waiting for $minutes minute${if (minutes > 1) "s" else ""}...")
        Thread.sleep(minutes * 60 * 1000L)
    } catch (ignored: InterruptedException) {
    }
}

private fun fetchCheckAndAlert(account: String) {

    try {
        val client: HttpClient = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(urlMap[account]!!))
            //.header("accept", "application/json, text/javascript, */*; q=0.01")
            //.header("referer","https://www.f2pool.com/mining-user-eth/c2f2a494800ce54d722cf95b77aa2ed4")
            //.header("authority", "www.f2pool.com")
            .header("x-requested-with", "XMLHttpRequest").build()
        client.sendAsync(request, BodyHandlers.ofString())
            .thenApply { obj: HttpResponse<*> -> obj.body() }
            //.thenApply(System.out::println)
            .thenApply { body -> getMinerData(body as? String) }
            .thenApply { minerList ->
                checkAndAlert(minerList ?: emptyList(), minerAccountSpecs[account]?.associateBy { it.name } ?: emptyMap())
            }.join()
    } catch (e: Exception) {
        e.printStackTrace()
    }


}


fun checkAndAlert(minerList: List<Miner>, expected: Map<String, Miner>) {
    if (false) { alertByAliyun() }

    if (minerList.size < expected.size
        || minerList.filter { it.name in expected.keys }
            .any { miner -> miner.hashrate?.toDoubleOrNull()?.let { it < 20.0 } == true }) {
        GsmNotifier.notify(contact, "")
    }
}

private fun alertByAliyun() {
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
        .headers(
            "X-Ca-Supervisor-Token",
            "eyJhbGciOiJSUzI1NiJ9.eyJpYXQiOjE2NDI0OTI3MTQsIm5iZiI6MTY0MjQ5MjY1NCwiaXNzIjoiQWxpeXVuQXBpR2F0ZXdheSIsInJvbGUiOiJ1c2VyIiwiYXVkIjoiYXBpLXNoYXJlZC12cGMtMDAyIiwiZXhwIjoxNjQyNDkzNjE0LCJqdGkiOiI1ZDhjNDRlYzVmNTk0OTcyYjg1ZDkwODgyOTU1ODhiMiIsImFjdGlvbiI6IkRFQlVHIiwidWlkIjoiMTM1NDkxNTE5NDU1MDc4MCJ9.SmGujprZxJvfP2kOQwz1eLmhQ_ZPTE1zQKdFoyfdQQSfterE7nuA-UFekmUyMVUeYHEyjuCzMgi5efEdzp66oj6lNYgGp-48H5Zsg-ZNf3FMZ9DN_89tenFWhZ_IQtmtDSrDL3PS0xWSgYF77-fZ5MA4Ta5YqX7k_T9CbkF0oTZnrin0tzZSzSwhrGdtWk9XUAyxinEbsSWZwy8k2iK9_aI5ZY4HeVvdTMBzos612WmZ-L_lxWPrx856KpaGuDNjWnLpzq9WPyCe2-FhBbAKijqDqFNfjRH7UR5mDogjZn5Rlbzp36OyEGZCxMg1tncLvQHUyS18E6MLez0B8jHSOQ"
        )
        .headers("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
        .build()

    val httpResponse = client.send(request, BodyHandlers.ofString())
    println(httpResponse.body())
}

fun getMinerData(json: String?): List<Miner> {
    json ?: return emptyList()

    return try {
        val mapper = ObjectMapper()
        val actualObj: JsonNode = mapper.readTree(json)

        // When
        val jsonNode1: JsonNode = actualObj.get("data")
        println(jsonNode1.textValue())
        val miners = (jsonNode1 as? ArrayNode)?.mapIndexed { index, jsonNode ->
            println("$index, $jsonNode")
            val miner: Miner = mapper.treeToValue(jsonNode, Miner::class.java)
            println(miner)
            miner
        }
        miners
    } catch (e: Exception) {
        e.printStackTrace()
        null
    } ?: emptyList()
}

