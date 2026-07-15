package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.analysis.AstroAnalyzer
import com.example.analysis.AstroReport
import com.example.analysis.TemporalAnalyzer
import com.example.analysis.TemporalReport
import com.example.data.Kline
import com.example.data.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val klines: List<Kline> = emptyList(),
    val temporalReport: TemporalReport? = null,
    val astroReport: AstroReport? = null,
    val astroDatesMs: List<Long> = emptyList()
)

class MainViewModel(private val repository: Repository) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun fetchData(symbol: String, interval: String, limit: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repository.getKlines(symbol, interval, limit)
            result.onSuccess { data ->
                val tempReport = TemporalAnalyzer.analyze(data)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    klines = data,
                    temporalReport = tempReport
                )
                analyzeAstro(_uiState.value.astroDatesMs)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "خطأ في جلب البيانات: ${e.message}"
                )
            }
        }
    }

    fun setAstroDates(dates: List<Long>) {
        _uiState.value = _uiState.value.copy(astroDatesMs = dates)
        analyzeAstro(dates)
    }

    private fun analyzeAstro(dates: List<Long>) {
        val state = _uiState.value
        if (state.temporalReport != null && dates.isNotEmpty()) {
            val report = AstroAnalyzer.analyze(dates, state.temporalReport.peaks, state.temporalReport.valleys)
            _uiState.value = state.copy(astroReport = report)
        } else {
            _uiState.value = state.copy(astroReport = null)
        }
    }
    
    fun getCsvData(): String {
        val sb = StringBuilder()
        sb.append("OpenTime,Open,High,Low,Close,Volume\n")
        _uiState.value.klines.forEach {
            sb.append("${it.openTime},${it.open},${it.high},${it.low},${it.close},${it.volume}\n")
        }
        return sb.toString()
    }
}
