package feature.detail.presentation

import androidx.lifecycle.viewModelScope
import base.BaseViewModel
import di.Inject
import feature.detail.domain.DeleteHabitUseCase
import feature.detail.domain.GetDetailInfoUseCase
import feature.detail.domain.UpdateHabitUseCase
import feature.detail.presentation.models.DateSelectionState
import feature.detail.presentation.models.DetailAction
import feature.detail.presentation.models.DetailEvent
import feature.detail.presentation.models.DetailViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import utils.CalendarDays

class DetailViewModel(private val habitId: String) : BaseViewModel<DetailViewState, DetailAction, DetailEvent>(
    initialState = DetailViewState(habitId = habitId)
) {

    private val getDetailInfoUseCase = Inject.instance<GetDetailInfoUseCase>()
    private val deleteHabitUseCase = Inject.instance<DeleteHabitUseCase>()
    private val updateHabitUseCase = Inject.instance<UpdateHabitUseCase>()

    init {
        fetchDetailedInformation()
    }

    override fun obtainEvent(viewEvent: DetailEvent) {
        when (viewEvent) {
            DetailEvent.CloseScreen -> viewAction = DetailAction.CloseScreen
            DetailEvent.DeleteItem -> deleteItem()
            DetailEvent.SaveChanges -> applyChanges()
            DetailEvent.StartDateClicked -> viewState = viewState.copy(dateSelectionState = DateSelectionState.Start)
            DetailEvent.EndDateClicked -> viewState = viewState.copy(dateSelectionState = DateSelectionState.End)
            is DetailEvent.DateSelected -> selectDate(viewEvent.value)
        }
    }

    private fun fetchDetailedInformation() {
        viewModelScope.launch(Dispatchers.Default) {
            val details = getDetailInfoUseCase.execute(habitId)

            withContext(Dispatchers.Main) {
                viewState = viewState.copy(
                    itemTitle = details.habitTitle,
                    startDate = details.startDate,
                    endDate = details.endDeta,
                    start = details.start,
                    end = details.end,
                    daysToCheck = details.daysToCheck,
                    isGood = details.isHabitGood
                )
            }
        }
    }

    private fun deleteItem() {
        viewModelScope.launch(Dispatchers.Default) {
            withContext(Dispatchers.Main) {
                viewState = viewState.copy(isDeleting = true)
            }

            try {
                deleteHabitUseCase.execute(viewState.habitId)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    viewState = viewState.copy(isDeleting = true)
                }
            }

            withContext(Dispatchers.Main) {
                viewAction = DetailAction.CloseScreen
            }
        }
    }

    private fun selectDate(value: LocalDate) {
        when (viewState.dateSelectionState) {
            DateSelectionState.None -> {}
            DateSelectionState.Start -> {
                viewState = viewState.copy(start = value, startDate = CalendarDays.Custom(value.toString()))
            }

            DateSelectionState.End -> viewState =
                viewState.copy(end = value, endDate = CalendarDays.Custom(value.toString()))
        }

        viewState = viewState.copy(dateSelectionState = DateSelectionState.None)
    }

    private fun applyChanges() {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                updateHabitUseCase.execute(
                    habitId = viewState.habitId,
                    habitTitle = viewState.itemTitle,
                    startDate = viewState.start,
                    endDate = viewState.end,
                    daysToCheck = viewState.daysToCheck,
                    isGood = viewState.isGood
                )

                withContext(Dispatchers.Main) {
                    viewAction = DetailAction.CloseScreen
                }
            } catch (e: IllegalStateException) {
                println(e.message)
            } catch (e: NullPointerException) {
                println(e.message)
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }
}