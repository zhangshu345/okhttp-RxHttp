package rxhttp.wrapper.entity

/**
 * User: ljx
 * Date: 2019-11-15
 * Time: 22:44
 */
data class KeyValuePair(val key: String, val value: Any, val isEncoded: Boolean) {

    constructor(key: String, any: Any) : this(key, any, false)

    fun equals(key: String): Boolean {
        return key == key
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other is String && other == key) {
            return true
        }
        return if (other is KeyValuePair && other.key == key) {
            true
        } else super.equals(other)
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + isEncoded.hashCode()
        return result
    }

}