package com.example.data

class Repository(private val api: BinanceApi) {
    suspend fun getKlines(
        symbol: String,
        interval: String,
        limit: Int = 500,
        startTime: Long? = null,
        endTime: Long? = null
    ): Result<List<Kline>> {
        return try {
            val response = api.getKlines(symbol, interval, limit, startTime, endTime)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
