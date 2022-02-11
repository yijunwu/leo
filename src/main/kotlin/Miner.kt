// import com.fasterxml.jackson.databind.ObjectMapper; // version 2.11.1
// import com.fasterxml.jackson.annotation.JsonProperty; // version 2.11.1
/* ObjectMapper om = new ObjectMapper();
Root root = om.readValue(myJsonString), Root.class); */
class Miner {
    var name: String = ""
    var local_hash: String? = null
    var last_share = 0
    var hashrate: String? = null
    var hashes_accepted: Long = 0L
    var shares_accepted: Long = 0L
    var hashes_last_day: Long = 0L
    var stale_hashes_rejected: Long = 0L
    var stale_shares_rejected: Long = 0L
    var currency: String? = null
    var stale_hashes_last_day: Long = 0L
    var local_hashes_last_day: Long = 0L
    var delay_hashes_last_day: Long = 0L
    var ip: String? = null
    var group_id: Any? = null
    var group_name: Any? = null
    var status = 0
    var delayrate_last_day: String? = null
    var hashrate_last_day: String? = null
    var stalerate = 0.0
    var stalerate_last_day: String? = null
    var localrate_last_day: String? = null
    var origin_name: String? = null
    var tag: Any? = null
    var tag_name: Any? = null
}