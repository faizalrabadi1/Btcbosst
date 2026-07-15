package com.example.analysis

import com.example.data.Kline
import kotlin.math.abs

data class AstroReport(
    val matchPercentage: Double,
    val matchedDates: List<Long>,
    val totalInputDates: Int
)

object AstroAnalyzer {
    fun analyze(datesMs: List<Long>, peaks: List<Kline>, valleys: List<Kline>, toleranceMs: Long = 86400000): AstroReport {
        if (datesMs.isEmpty()) return AstroReport(0.0, emptyList(), 0)
        
        val keyPoints = (peaks + valleys).map { it.openTime }
        if (keyPoints.isEmpty()) return AstroReport(0.0, emptyList(), datesMs.size)
        
        val matchedDates = mutableListOf<Long>()
        
        for (date in datesMs) {
            val hasMatch = keyPoints.any { point -> abs(point - date) <= toleranceMs }
            if (hasMatch) {
                matchedDates.add(date)
            }
        }
        
        val matchPercentage = (matchedDates.size.toDouble() / datesMs.size) * 100
        return AstroReport(matchPercentage, matchedDates, datesMs.size)
    }
}
