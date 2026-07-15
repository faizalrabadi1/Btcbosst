package com.example.analysis

import com.example.data.Kline

data class Cycle(val periodHours: Double, val count: Int)

data class TemporalReport(
    val peaks: List<Kline>,
    val valleys: List<Kline>,
    val dominantCycles: List<Cycle>
)

object TemporalAnalyzer {
    fun analyze(data: List<Kline>, windowSize: Int = 5): TemporalReport {
        if (data.size < windowSize * 2 + 1) return TemporalReport(emptyList(), emptyList(), emptyList())
        val peaks = mutableListOf<Kline>()
        val valleys = mutableListOf<Kline>()

        for (i in windowSize until data.size - windowSize) {
            val current = data[i]
            var isPeak = true
            var isValley = true
            for (j in i - windowSize..i + windowSize) {
                if (i == j) continue
                if (data[j].high > current.high) isPeak = false
                if (data[j].low < current.low) isValley = false
            }
            if (isPeak) peaks.add(current)
            if (isValley) valleys.add(current)
        }

        val cycles = mutableMapOf<Long, Int>()
        for (i in 0 until peaks.size - 1) {
            val diffMs = peaks[i+1].openTime - peaks[i].openTime
            val diffHours = (diffMs / (1000 * 60 * 60)).toLong()
            cycles[diffHours] = cycles.getOrDefault(diffHours, 0) + 1
        }
        val topCycles = cycles.entries.sortedByDescending { it.value }
            .take(3)
            .map { Cycle(it.key.toDouble(), it.value) }

        return TemporalReport(peaks, valleys, topCycles)
    }
}
