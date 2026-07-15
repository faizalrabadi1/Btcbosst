package com.example.data

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

data class Kline(
    val openTime: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double,
    val closeTime: Long
)

class KlineAdapter : JsonAdapter<Kline>() {
    override fun fromJson(reader: JsonReader): Kline? {
        if (reader.peek() == JsonReader.Token.NULL) {
            return reader.nextNull()
        }
        reader.beginArray()
        val openTime = reader.nextLong()
        val open = reader.nextString().toDouble()
        val high = reader.nextString().toDouble()
        val low = reader.nextString().toDouble()
        val close = reader.nextString().toDouble()
        val volume = reader.nextString().toDouble()
        val closeTime = reader.nextLong()
        // skip the rest
        while (reader.hasNext()) {
            reader.skipValue()
        }
        reader.endArray()
        return Kline(openTime, open, high, low, close, volume, closeTime)
    }

    override fun toJson(writer: JsonWriter, value: Kline?) {
        // Not needed
    }
}
