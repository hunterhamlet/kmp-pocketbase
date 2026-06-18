package com.hamon.kmp_pocketbase.network.pocketbase

data class FileUpload(
    val field: String,
    val filename: String,
    val data: ByteArray,
    val mimeType: String = "application/octet-stream",
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FileUpload) return false
        return field == other.field &&
            filename == other.filename &&
            data.contentEquals(other.data) &&
            mimeType == other.mimeType
    }

    override fun hashCode(): Int {
        var result = field.hashCode()
        result = 31 * result + filename.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + mimeType.hashCode()
        return result
    }
}
