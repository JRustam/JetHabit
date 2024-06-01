package utils

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import tech.mobiledeveloper.jethabit.resources.Res
import tech.mobiledeveloper.jethabit.resources.not_selected

sealed class CalendarDays {
    data object NotSelected : CalendarDays()
    class Custom(val value: String) : CalendarDays()
}

@Composable
fun CalendarDays.title(): String = when (this) {
    CalendarDays.NotSelected -> stringResource(Res.string.not_selected)
    is CalendarDays.Custom -> value
}