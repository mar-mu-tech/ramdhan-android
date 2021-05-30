package com.marmutech.ramdantimetable.ramadantimetable.ui.splash

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.marmutech.ramdantimetable.ramadantimetable.R
import com.marmutech.ramdantimetable.ramadantimetable.di.MainCoroutineDispatcher
import com.marmutech.ramdantimetable.ramadantimetable.domain.country.GetCountryListUseCase
import com.marmutech.ramdantimetable.ramadantimetable.domain.country.GetSelectedCountryIdUseCase
import com.marmutech.ramdantimetable.ramadantimetable.domain.country.SaveSelectedCountryIdUseCase
import com.marmutech.ramdantimetable.ramadantimetable.domain.country.SaveSelectedCountryNameUseCase
import com.marmutech.ramdantimetable.ramadantimetable.domain.fonts.GetIsEnableUnicodeUseCase
import com.marmutech.ramdantimetable.ramadantimetable.domain.fonts.SetIsEnableUnicodeUseCase
import com.marmutech.ramdantimetable.ramadantimetable.domain.state.GetSelectedStateIdUseCase
import com.marmutech.ramdantimetable.ramadantimetable.domain.state.GetStateListBySelectedCountryUseCase
import com.marmutech.ramdantimetable.ramadantimetable.domain.state.SaveSelectedStateIdUseCase
import com.marmutech.ramdantimetable.ramadantimetable.domain.state.SaveSelectedStateNameUseCase
import com.marmutech.ramdantimetable.ramadantimetable.model.Country
import com.marmutech.ramdantimetable.ramadantimetable.model.State
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SplashViewModel @Inject constructor(
    @MainCoroutineDispatcher private val dispatcher: CoroutineDispatcher,
    private val setIsEnableUnicodeUseCase: SetIsEnableUnicodeUseCase,
    private val getIsEnableUnicodeUseCase: GetIsEnableUnicodeUseCase,
    private val saveSelectedCountryIdUseCase: SaveSelectedCountryIdUseCase,
    private val getSelectedCountryIdUseCase: GetSelectedCountryIdUseCase,
    private val saveSelectedStateIdUseCase: SaveSelectedStateIdUseCase,
    private val getSelectedIdStateUseCase: GetSelectedStateIdUseCase,
    private val saveSelectedStateNameUseCase: SaveSelectedStateNameUseCase,
    private val saveSelectedCountryNameUseCase: SaveSelectedCountryNameUseCase,
    private val getCountryListUseCase: GetCountryListUseCase,
    private val getStateListBySelectedCountryUseCase: GetStateListBySelectedCountryUseCase
) : ViewModel() {

    private val _countriesSelectionUiModel = MutableStateFlow<CountrySelectionUiModel?>(null)
    val countrySelectionUiModel: StateFlow<CountrySelectionUiModel?> get() = _countriesSelectionUiModel

    private val _fontSelectionUiModel = MutableStateFlow<FontSelectionUiModel?>(null)
    val fontSelectionUiModel: LiveData<FontSelectionUiModel?> get() = _fontSelectionUiModel.asLiveData()

    private val _stateList: MutableStateFlow<List<State>?> = MutableStateFlow(null)
    private val _countryList: MutableStateFlow<List<Country>?> = MutableStateFlow(null)

    fun onViewCreated() {
        viewModelScope.launch(dispatcher) {

            _fontSelectionUiModel.value = FontSelectionUiModel(
                isUnicodeEnable = getIsEnableUnicodeUseCase.execute(
                    Unit
                )
            )

            initCountrySelectionUiModel()
        }
    }

    fun setEnableUnicode(enable: Boolean) {
        viewModelScope.launch(dispatcher) {
            setIsEnableUnicodeUseCase.execute(enable)
        }
    }

    private suspend fun initCountrySelectionUiModel() {
        combine(
            getCountryListUseCase.execute(Unit),
            flowOf(getSelectedCountryIdUseCase.execute(Unit))
        ) { countries, savedCountryId ->
            val selectedCountriesId = savedCountryId ?: getVeryFirstCountryId(countries)!!
            countries to selectedCountriesId
        }.mapLatest {
            val state = getStateListBySelectedCountryUseCase.execute(it.second).single()
            it.first to state
        }.collect {
            handleCountryAndStateData(it.first, it.second)
        }
    }

    fun onCountrySelected(position: Int) {
        viewModelScope.launch {
            _countryList.value?.let {
                val countryIdAndStateName = getCountryIdAndNameFromCountryByPosition(it, position)
                    ?: return@launch
                saveSelectedCountryIdUseCase.execute(countryIdAndStateName.first)
                saveSelectedCountryNameUseCase.execute(countryIdAndStateName.second)
                Timber.d("banner: country data saved")
            }
        }
    }

    fun onStateSelected(position: Int) {
        viewModelScope.launch {
            _stateList.value?.let {
                val stateIdAndStateName = getStateIdAndNameFromStateByPosition(it, position)
                    ?: return@launch
                saveSelectedStateIdUseCase.execute(stateIdAndStateName.first)
                saveSelectedStateNameUseCase.execute(stateIdAndStateName.second)
                Timber.d("banner: state data saved")
            }
        }
    }

    private fun handleCountryAndStateData(countries: List<Country>, states: List<State>) {
        viewModelScope.launch {
            saveCountryListInVM(countries)
            saveStateListInVM(states)
            Timber.d("banner: _selectedCountryId.value ${getSelectedCountryIdUseCase.execute(Unit)}")
            Timber.d("banner: _selectedStateId.value ${getSelectedIdStateUseCase.execute(Unit)}")
            _countriesSelectionUiModel.value = CountrySelectionUiModel(
                CountryListUiModel(
                    getNameFromCountry(countries),
                    getCountrySelectedIndex(countries, getSelectedCountryIdUseCase.execute(Unit))
                ),
                StateListUiModel(
                    getNameFromState(states),
                    getStateSelectedIndex(states, getSelectedIdStateUseCase.execute(Unit))
                ),
                mapSelectionText(getIsEnableUnicodeUseCase.execute(Unit))
            )
        }
    }

    private fun getVeryFirstCountryId(countries: List<Country>): String? = countries.getOrNull(0)?.objectId

    private fun getNameFromCountry(countries: List<Country>): List<String> = countries.map { it.name }

    //todo fix to one unifine name
    private fun getNameFromState(state: List<State>): List<String> = state.map { it.nameMmUni }

    private fun getStateIdAndNameFromStateByPosition(
        state: List<State>,
        position: Int
    ) = state.getOrNull(position)?.run {
        this.objectId to this.nameMmUni
    }

    private fun getCountryIdAndNameFromCountryByPosition(
        country: List<Country>,
        position: Int
    ) = country.getOrNull(position)?.run {
        this.objectId to this.name
    }

    private fun saveCountryListInVM(country: List<Country>) {
        _countryList.value = country
    }

    private fun saveStateListInVM(state: List<State>) {
        _stateList.value = state
    }

    private fun getCountrySelectedIndex(countries: List<Country>, savedId: String?): Int {
        if (savedId == null) return 0

        return countries.indexOfFirst {
            it.objectId == savedId
        }
    }

    private fun getStateSelectedIndex(state: List<State>, savedId: String?): Int {
        if (savedId == null) return 0

        return state.indexOfFirst {
            it.objectId == savedId
        }
    }

    private suspend fun mapSelectionText(isEnableUnicode: Boolean): SelectionText {
        return SelectionText(
            if (isEnableUnicode) R.string.uni_country_select else R.string.zg_country_select,
            if (isEnableUnicode) R.string.uni_country_mm else R.string.zg_country_mm,
            if (isEnableUnicode) R.string.uni_state_mm else R.string.zg_state_mm
        )
    }
}

data class CountrySelectionUiModel(
    val countryListUiModel: CountryListUiModel,
    val stateListUiModel: StateListUiModel,
    val selectionText: SelectionText
)

data class CountryListUiModel(
    val countries: List<String>,
    val selectedIndex: Int
)

data class StateListUiModel(
    val states: List<String>,
    val selectedIndex: Int
)

data class SelectionText(
    @StringRes val selectionTitleText: Int,
    @StringRes val selectionCountryText: Int,
    @StringRes val selectionStateText: Int
)

data class FontSelectionUiModel(
    val isUnicodeEnable: Boolean
)
