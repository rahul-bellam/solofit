package com.solofit.app.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.core.DateUtils
import com.solofit.app.data.local.entity.DailyLogEntity
import com.solofit.app.data.scanner.BarcodeScanner
import com.solofit.app.data.scanner.ScanOutcome
import com.solofit.app.domain.model.BarcodeLookupResult
import com.solofit.app.domain.model.MealCategory
import com.solofit.app.domain.model.ScannedFood
import com.solofit.app.domain.repository.BarcodeRepository
import com.solofit.app.domain.repository.DailyLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** UI state for the barcode scan + lookup + log flow. */
sealed interface ScanUiState {
    data object Idle : ScanUiState
    data object Scanning : ScanUiState
    data object LookingUp : ScanUiState
    data class Found(val food: ScannedFood) : ScanUiState
    /** Open Food Facts had no usable entry -> show manual fallback form. */
    data class ManualEntry(val barcode: String) : ScanUiState
    data class Error(val message: String) : ScanUiState
    data object Logged : ScanUiState
}

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val scanner: BarcodeScanner,
    private val barcodeRepository: BarcodeRepository,
    private val dailyLogRepository: DailyLogRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val state = _state.asStateFlow()

    fun startScan() {
        _state.update { ScanUiState.Scanning }
        viewModelScope.launch {
            when (val outcome = scanner.scan()) {
                is ScanOutcome.Success -> lookup(outcome.rawValue)
                ScanOutcome.Cancelled -> _state.update { ScanUiState.Idle }
                is ScanOutcome.Failure -> _state.update { ScanUiState.Error(outcome.message) }
            }
        }
    }

    /** Allows manual barcode entry too (e.g., damaged label). */
    fun lookupManual(barcode: String) {
        if (barcode.isBlank()) return
        viewModelScope.launch { lookup(barcode.trim()) }
    }

    private suspend fun lookup(barcode: String) {
        _state.update { ScanUiState.LookingUp }
        when (val result = barcodeRepository.lookup(barcode)) {
            is BarcodeLookupResult.Found -> _state.update { ScanUiState.Found(result.food) }
            is BarcodeLookupResult.NotFound ->
                _state.update { ScanUiState.ManualEntry(result.barcode) }
            is BarcodeLookupResult.Error -> _state.update { ScanUiState.Error(result.message) }
        }
    }

    /** Persist a manually-entered product, then move to the Found (confirm) state. */
    fun submitManual(
        barcode: String,
        name: String,
        kcal: Double,
        protein: Double,
        carbs: Double,
        fats: Double
    ) {
        val food = ScannedFood(barcode, name.trim(), kcal, protein, carbs, fats)
        viewModelScope.launch {
            barcodeRepository.saveScannedFood(food)
            _state.update { ScanUiState.Found(food) }
        }
    }

    /** Final step: save (cache) the product and write the daily log entry. */
    fun logFood(food: ScannedFood, grams: Double, category: MealCategory) {
        viewModelScope.launch {
            val foodId = barcodeRepository.saveScannedFood(food)
            dailyLogRepository.logFood(
                DailyLogEntity(
                    date = DateUtils.today(),
                    foodId = foodId,
                    gramsConsumed = grams,
                    mealCategory = category.name
                )
            )
            _state.update { ScanUiState.Logged }
        }
    }

    fun reset() = _state.update { ScanUiState.Idle }
}
