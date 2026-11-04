package com.example.staaks_campsite_app

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.staaks_campsite_app.data.CampsiteRepository
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import com.example.staaks_campsite_app.data.model.Campsite
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CampsiteSearchBar(
    modifier: Modifier = Modifier,
    campsites: CampsiteRepository = CampsiteRepository(),
    onChangeCampsites: (List<Campsite>) -> Unit = {}
){
    var keyword by remember() { mutableStateOf("") }
    var location by remember() { mutableStateOf("") }
    var date by remember() { mutableStateOf(emptyList<String>()) }
    var capacity by remember() { mutableIntStateOf(0) }

    var startDateInput by remember { mutableStateOf("") }
    var endDateInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showFilter by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(8.dp).fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // SearchBar
            OutlinedTextField(
                value = keyword,
                onValueChange = { keyword = it },
                label = { Text("Keyword") },
                modifier = Modifier.weight(0.6f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // search button
            FloatingActionButton(
                onClick = {
                    onChangeCampsites(campsites.filterCampsites(keyword, location, date, capacity))
                },
                modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp).weight(0.1f)
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }

            // filter button
            FloatingActionButton(
                onClick = { showFilter = !showFilter },
                modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp).weight(0.1f)
            ) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter")
            }
        }

        if (showFilter) {
            Popup(
                alignment = Alignment.TopEnd,
                onDismissRequest = { showFilter = false },
                properties = PopupProperties(
                    focusable = true
                )
            ) {
                Surface(
                    modifier = Modifier
                        .padding(top = 64.dp, end = 16.dp)
                        .fillMaxWidth(0.95f),
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 16.dp,
                    color = Color(0xFFF5F5F5),
                ) {
                    Column(
                        modifier = Modifier, verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Location") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = startDateInput,
                                onValueChange = { input ->
                                    startDateInput = input
                                    validateAndGenerateDates(startStr = input, endStr = endDateInput)
                                },
                                label = { Text("Start Date") },
                                isError = errorMessage != null,
                                supportingText = {
                                    if (errorMessage != null) {
                                        Text(
                                            text = errorMessage!!,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                            OutlinedTextField(
                                value = endDateInput,
                                onValueChange = { input ->
                                    endDateInput = input
                                    val (err, d) = validateAndGenerateDates(startStr = startDateInput, endStr = input)
                                    errorMessage = err
                                    date = d
                                },
                                label = { Text("End Date") },
                                isError = errorMessage != null,
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                        }

                        OutlinedTextField(
                            value = capacity.toString(),
                            onValueChange = { capacity = it.toIntOrNull() ?: 0 },
                            label = { Text("Number of Guests") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )
                        Row(modifier = Modifier.fillMaxWidth()) {
                            // clear filter button
                            FloatingActionButton(
                                onClick = {
                                    location = ""
                                    date = emptyList()
                                    startDateInput = ""
                                    endDateInput = ""
                                    errorMessage = null
                                    capacity = 0
                                    showFilter = false
                                },
                                modifier = Modifier.padding(start = 8.dp).weight(1f)
                            ) {
                                Text("Clear Filter")
                            }

                            // Applies filter button
                            FloatingActionButton(
                                onClick = {
                                    onChangeCampsites(campsites.filterCampsites(keyword, location, date, capacity))
                                    showFilter = false
                                },
                                modifier = Modifier.padding(start = 8.dp).weight(1f)
                            ) {
                                Text("Apply Filter")
                            }
                        }

                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(name = "Search Screen Light", showBackground = true)
@Composable
fun CampsiteSearchScreenPreview() {
    MaterialTheme {
        Surface {
            CampsiteSearchBar()
        }
    }
}

fun validateAndGenerateDates(startStr: String, endStr: String): Pair<String?, List<String>> {
    var errorMessage: String? = null
    var date: List<String> = emptyList()

    if (startStr.isBlank() && endStr.isBlank()) {
        errorMessage = null
        date = emptyList()
    }

    if (startStr.isBlank()) {
        errorMessage = "Start date is required"
        date = emptyList()
    }

    if (endStr.isBlank()) {
        errorMessage = "End date is required"
        date = emptyList()
    }

    val dateRegex = Regex("""\d{4}-\d{2}-\d{2}""")
    if (!dateRegex.matches(startStr) || !dateRegex.matches(endStr)) {
        errorMessage = "Invalid date format (YYYY-MM-DD)"
        date = emptyList()
    }

    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
        isLenient = false
    }

    try {
        val start = format.parse(startStr)
        val end = format.parse(endStr)

        if (start != null && end != null) {
            if (end.before(start)) {
                errorMessage = "End date cannot be before start date"
                date = emptyList()
                return Pair(errorMessage, date)
            } else {
                errorMessage = null
                val cal = Calendar.getInstance().apply { time = start }
                val calend = Calendar.getInstance().apply { time = end }
                val dateList = mutableListOf<String>()

                while (!cal.after(calend)) {
                    dateList.add(format.format(cal.time))
                    cal.add(Calendar.DATE, 1)
                }
                date = dateList
            }
        }
    } catch (e: Exception) {
        errorMessage = "Invalid date format (YYYY-MM-DD)"
        date = emptyList()
    }
    return Pair(errorMessage, date)
}