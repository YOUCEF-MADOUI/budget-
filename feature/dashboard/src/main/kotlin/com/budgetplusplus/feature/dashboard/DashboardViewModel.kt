package com.budgetplusplus.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetplusplus.core.model.DashboardData
import com.budgetplusplus.domain.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

enum class DashboardPeriod { WEEK, MONTH, YEAR, CUSTOM }
data class DateInterval(val from: LocalDate, val toInclusive: LocalDate)
sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Content(val data: DashboardData, val interval: DateInterval) : DashboardUiState
    data object Error : DashboardUiState
}

@HiltViewModel
class DashboardViewModel @Inject constructor(private val repository: DashboardRepository) : ViewModel() {
    private val period = MutableStateFlow(DashboardPeriod.MONTH)
    private val customInterval = MutableStateFlow(defaultInterval(DashboardPeriod.MONTH))
    private val refresh = MutableStateFlow(0)
    val selectedPeriod: StateFlow<DashboardPeriod> = period

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<DashboardUiState> = combine(period, customInterval, refresh) { selected, custom, _ ->
        if (selected == DashboardPeriod.CUSTOM) custom else defaultInterval(selected)
    }.flatMapLatest { interval ->
        val zone = ZoneId.systemDefault()
        repository.observeDashboard(
            interval.from.atStartOfDay(zone).toInstant().toEpochMilli(),
            interval.toInclusive.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli(),
        ).map<DashboardData, DashboardUiState> { DashboardUiState.Content(it, interval) }
            .onStart { emit(DashboardUiState.Loading) }
            .catch { emit(DashboardUiState.Error) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState.Loading)

    fun selectPeriod(value: DashboardPeriod) { period.value = value }
    fun selectCustom(from: LocalDate, toInclusive: LocalDate) {
        customInterval.value = DateInterval(minOf(from, toInclusive), maxOf(from, toInclusive))
        period.value = DashboardPeriod.CUSTOM
    }
    fun retry() { refresh.value += 1 }

    companion object {
        internal fun defaultInterval(period: DashboardPeriod, today: LocalDate = LocalDate.now()): DateInterval = when (period) {
            DashboardPeriod.WEEK -> DateInterval(today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)), today)
            DashboardPeriod.MONTH -> DateInterval(today.withDayOfMonth(1), today)
            DashboardPeriod.YEAR -> DateInterval(today.withDayOfYear(1), today)
            DashboardPeriod.CUSTOM -> DateInterval(today.withDayOfMonth(1), today)
        }
    }
}
