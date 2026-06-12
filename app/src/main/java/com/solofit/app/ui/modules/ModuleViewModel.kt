package com.solofit.app.ui.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.data.local.UserPreferences
import com.solofit.app.domain.model.SoloFitModule
import com.solofit.app.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ModuleUiState(
    val enabledModules: List<SoloFitModule> = SoloFitModule.DEFAULT_ENABLED,
    val isLoading: Boolean = true
)

data class ModuleSuggestion(
    val module: SoloFitModule,
    val reason: String,
    val detail: String
)

@HiltViewModel
class ModuleViewModel @Inject constructor(
    private val prefs: UserPreferences,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    val moduleSelectionComplete: StateFlow<Boolean> = prefs.moduleSelectionComplete
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val state: StateFlow<ModuleUiState> = combine(
        prefs.enabledModules,
        prefs.moduleOrder
    ) { enabled, order ->
        val ordered = if (order.isNotEmpty()) {
            order.filter { it in enabled } + enabled.filter { it !in order }
        } else enabled
        ModuleUiState(
            enabledModules = ordered,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ModuleUiState())

    val enabledModules: StateFlow<List<SoloFitModule>> = state.map { it.enabledModules }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SoloFitModule.DEFAULT_ENABLED)

    val suggestions: StateFlow<List<ModuleSuggestion>> = combine(
        prefs.enabledModules,
        workoutRepository.observeHistory()
    ) { enabled, history ->
        val count = history.size
        buildList {
            if (SoloFitModule.WALKING !in enabled && count < 3) {
                add(
                    ModuleSuggestion(
                        module = SoloFitModule.WALKING,
                        reason = "Start with Walking",
                        detail = "A short walk each day builds momentum without pressure."
                    )
                )
            }
            if (SoloFitModule.RECOVERY !in enabled && count >= 5) {
                add(
                    ModuleSuggestion(
                        module = SoloFitModule.RECOVERY,
                        reason = "Add Recovery Tracking",
                        detail = "Recovery insights can help you understand how your body responds."
                    )
                )
            }
            if (SoloFitModule.BODY_RECOMPOSITION !in enabled && count >= 10) {
                add(
                    ModuleSuggestion(
                        module = SoloFitModule.BODY_RECOMPOSITION,
                        reason = "Add Body Recomposition",
                        detail = "Track measurements and progress beyond weight."
                    )
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun isEnabled(module: SoloFitModule): Boolean = module in state.value.enabledModules

    fun enableModule(module: SoloFitModule) {
        viewModelScope.launch {
            val current = prefs.enabledModules.first().toMutableList()
            if (module !in current) {
                current.add(module)
                prefs.setEnabledModules(current)
            }
        }
    }

    fun toggle(module: SoloFitModule) {
        viewModelScope.launch {
            val current = prefs.enabledModules.first().toMutableList()
            if (module in current) current.remove(module) else current.add(module)
            prefs.setEnabledModules(current)
        }
    }

    fun selectModules(modules: List<SoloFitModule>) {
        viewModelScope.launch {
            prefs.setEnabledModules(modules)
            prefs.setModuleOrder(SoloFitModule.entries.filter { it in modules })
            prefs.setModuleSelectionComplete(true)
        }
    }

    fun reorder(modules: List<SoloFitModule>) {
        viewModelScope.launch {
            prefs.setModuleOrder(modules)
            prefs.setEnabledModules(modules)
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            prefs.setEnabledModules(SoloFitModule.DEFAULT_ENABLED)
            prefs.setModuleOrder(emptyList())
        }
    }
}
