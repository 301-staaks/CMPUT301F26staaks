package com.example.staaks_campsite_app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Textsms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.staaks_campsite_app.data.model.Campsite
import com.example.staaks_campsite_app.data.model.Lottery
import com.example.staaks_campsite_app.session.SessionManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import com.example.staaks_campsite_app.data.model.LotteryTime
import androidx.compose.runtime.DisposableEffect

// same tabs as the main page so the bottom bar matches
private val lotteryEditTabs = listOf(
    Tab("owner_main", "Main", Icons.Default.Home),
    Tab("owner_reservations_active", "Reservations", Icons.Default.CalendarMonth),
    Tab("owner_notifications", "Notifications", Icons.Default.Notifications),
    Tab("owner_messages", "Messages", Icons.Default.Textsms),
    Tab("owner_lotteries", "Lotteries", Icons.Default.ConfirmationNumber)
)

// keys used to attach an error message to a field
private const val ERR_CAMPSITES = "campsites"
private const val ERR_REG_START = "regStart"
private const val ERR_REG_END = "regEnd"
private const val ERR_BOOK_START = "bookStart"
private const val ERR_BOOK_END = "bookEnd"
private const val ERR_WINNERS = "winners"
private const val ERR_CAPACITY = "capacity"

/**
 * The owner's screen for creating a new lottery (US 02.04.01) or editing an existing one.
 *
 * Covers the fields from US 02.04.02 (multiple campsites), 02.04.03 (registration period),
 * 02.04.04 (booking period), 02.04.05 (number of winners) and 02.04.07 (waitlist capacity).
 * Validation runs here so the owner sees errors right away. Saving is handed to [onSave].
 *
 * TODO (back end): save the lottery, re-check the overlap rule against the database, and
 *                  draw winners automatically when registration ends (US 02.05.01).
 * TODO (US 02.04.06): load an existing lottery from its id for the edit flow.
 *
 * @param navController controls navigation to other screens
 * @param isNew true when creating a lottery, which changes the title and button text
 * @param ownerId the id of the owner creating the lottery
 * @param ownerCampsites the owner's own campsites (only these can be picked)
 * @param existingLotteries lotteries already loaded, used to catch overlapping booking periods
 * @param existing the lottery being edited, or null when creating
 * @param currentWaitlistCount entrants already on the waitlist of [existing]; the capacity
 * cannot be set lower than this
 * @param onSave called with the finished lottery once every field is valid
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerLotteryEdit(
    navController: NavController,
    isNew: Boolean,
    ownerId: String = "",
    ownerCampsites: List<Campsite> = emptyList(),
    existingLotteries: List<Lottery> = emptyList(),
    existing: Lottery? = null,
    currentWaitlistCount: Int = 0,
    onSave: (Lottery, (String?) -> Unit) -> Unit =
        { _, done -> done("Saving is not connected") }
) {
    val session by SessionManager.state.collectAsState()

    val initialIds: Set<String> = existing?.let { lottery ->
        if (lottery.campsiteIds.isNotEmpty()) lottery.campsiteIds.toSet()
        else setOfNotNull(lottery.campsiteId.ifBlank { null })
    } ?: emptySet()

    var selectedIds by remember { mutableStateOf(initialIds) }
    var regStart by remember { mutableStateOf(existing?.registrationStartDate ?: "") }
    var regEnd by remember { mutableStateOf(existing?.registrationEndDate ?: "") }
    var bookStart by remember { mutableStateOf(existing?.bookingStartDate ?: "") }
    var bookEnd by remember { mutableStateOf(existing?.bookingEndDate ?: "") }
    var winners by remember { mutableStateOf(existing?.numberOfWinners?.toString() ?: "") }
    // waitlist capacity is off by default
    var waitlistOn by remember { mutableStateOf(existing?.waitlistCapacity != null) }
    var capacity by remember { mutableStateOf(existing?.waitlistCapacity?.toString() ?: "") }

    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var screenActive by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        onDispose {
            screenActive = false
        }
    }

    // the system back button asks before throwing the form away
    BackHandler {
        if (!saving) {
            showDiscardDialog = true
        }
    }

    Scaffold(
        topBar = {
            UiTopRow(
                navController = navController,
                title = if (isNew) "Create Lottery" else "Edit Lottery",
                homeRoute = "owner_main",
                isAdmin = session?.isAdmin == true
            )
        },
        bottomBar = { UiBottomRow(navController = navController, tabs = lotteryEditTabs) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (errors.isNotEmpty()) {
                Text(
                    text = errors["save"]
                        ?: "Some fields are invalid. Please fix the fields marked below.",
                    color = MaterialTheme.colorScheme.error
                )
            }

            // ---------- Campsites (US 02.04.02) ----------
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Campsites", fontSize = 20.sp)
                    Text("Select one or more of your campsites.")
                    if (ownerCampsites.isEmpty()) {
                        Text("You have no campsites yet. Add one from the Main page first.")
                    }
                    ownerCampsites.forEach { campsite ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedIds = if (campsite.id in selectedIds) {
                                        selectedIds - campsite.id
                                    } else {
                                        selectedIds + campsite.id
                                    }
                                }
                        ) {
                            Checkbox(
                                checked = campsite.id in selectedIds,
                                onCheckedChange = { checked ->
                                    selectedIds = if (checked) selectedIds + campsite.id
                                    else selectedIds - campsite.id
                                }
                            )
                            Text(campsite.name)
                        }
                    }
                    FieldError(errors[ERR_CAMPSITES])
                }
            }

            // ---------- Registration period (US 02.04.03) ----------
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Registration Period", fontSize = 20.sp)
                    Text("Opens at the start of the first day and closes at the end of the last day.")
                    Spacer(modifier = Modifier.height(8.dp))
                    DateField("Start date", regStart, errors[ERR_REG_START]) { regStart = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    DateField("End date", regEnd, errors[ERR_REG_END]) { regEnd = it }
                }
            }

            // ---------- Booking period (US 02.04.04) ----------
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Booking Period", fontSize = 20.sp)
                    Text("Winners can only book dates inside this period.")
                    Spacer(modifier = Modifier.height(8.dp))
                    DateField("Start date", bookStart, errors[ERR_BOOK_START]) { bookStart = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    DateField("End date", bookEnd, errors[ERR_BOOK_END]) { bookEnd = it }
                }
            }

            // ---------- Number of winners (US 02.04.05) ----------
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Number of Winners", fontSize = 20.sp)
                    Text("Applies to the whole lottery, not to each campsite.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = winners,
                        onValueChange = { winners = it },
                        label = { Text("Number of Winners") },
                        isError = errors[ERR_WINNERS] != null,
                        supportingText = { errors[ERR_WINNERS]?.let { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ---------- Waitlist capacity (US 02.04.07) ----------
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Waitlist Capacity",
                            fontSize = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = waitlistOn,
                            onCheckedChange = { waitlistOn = it }
                        )
                    }
                    Text(
                        if (waitlistOn) "Set the maximum number of entrants on the waitlist."
                        else "Off: there is no limit on the waitlist."
                    )
                    if (waitlistOn) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = capacity,
                            onValueChange = { capacity = it },
                            label = { Text("Maximum entrants") },
                            isError = errors[ERR_CAPACITY] != null,
                            supportingText = { errors[ERR_CAPACITY]?.let { Text(it) } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // ---------- Buttons ----------
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    enabled = !saving,
                    onClick = { showDiscardDialog = true },
                    modifier = Modifier.weight(1f)
                ) { Text("Cancel") }

                Button(
                    enabled = !saving,
                    onClick = {
                        val today = todayString()
                        val result = validateLottery(
                            selectedIds = selectedIds,
                            campsiteNames = ownerCampsites.associate { it.id to it.name },
                            regStart = regStart,
                            regEnd = regEnd,
                            bookStart = bookStart,
                            bookEnd = bookEnd,
                            winners = winners,
                            waitlistOn = waitlistOn,
                            capacity = capacity,
                            today = today,
                            checkStartIsTodayOrLater =
                                isNew || regStart != existing?.registrationStartDate,
                            minCapacity = if (isNew) 0 else currentWaitlistCount,
                            existingLotteries = existingLotteries,
                            editingId = existing?.lotteryId ?: ""
                        )
                        errors = result

                        if (result.isEmpty()) {
                            // keep the campsites in the same order the owner sees them
                            val ids = ownerCampsites
                                .filter { it.id in selectedIds }
                                .map { it.id }
                                .toMutableList()

                            // TODO (back end): persist this lottery
                            val deadline = try {
                                // preserves the exact deadline when the end date is unchanged
                                if (
                                    existing != null &&
                                    regEnd == existing.registrationEndDate &&
                                    existing.registrationClosesAt > 0L
                                ) {
                                    existing.registrationClosesAt
                                } else {
                                    LotteryTime.closingTime(regEnd)
                                }
                            } catch (_: Exception) {
                                errors = mapOf(
                                    ERR_REG_END to "Choose a valid registration end date"
                                )
                                return@Button
                            }

                            saving = true

                            onSave(
                                Lottery(
                                    lotteryId = existing?.lotteryId ?: "",
                                    campsiteId = ids.firstOrNull() ?: "",
                                    registrationStartDate = regStart,
                                    registrationEndDate = regEnd,
                                    bookingStartDate = bookStart,
                                    bookingEndDate = bookEnd,
                                    status = existing?.status ?: "Open",
                                    ownerId = ownerId,
                                    campsiteIds = ids,
                                    waitlistCapacity =
                                        if (waitlistOn) capacity.trim().toInt() else null,
                                    numberOfWinners = winners.trim().toInt(),
                                    winnerIds = existing?.winnerIds ?: mutableListOf(),
                                    registrationClosesAt = deadline,
                                    drawCompleted = existing?.drawCompleted ?: false,
                                    drawnAt = existing?.drawnAt ?: 0L
                                )
                            ) { error ->
                                if (screenActive) {
                                    saving = false

                                    if (error == null) {
                                        navController.popBackStack(
                                            "owner_lotteries",
                                            inclusive = false
                                        )
                                    } else {
                                        errors = mapOf("save" to error)
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text(
                    if (saving) "Saving..."
                    else if (isNew) "Create Lottery"
                    else "Save Changes"
                ) }
            }
        }
    }

    // Cancel asks for confirmation before discarding what was entered
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard lottery?") },
            text = { Text("Everything you entered will be lost.") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    navController.popBackStack()
                }) { Text("Discard") }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("Keep editing") }
            }
        )
    }
}

/** red error text under a field; shows nothing when [message] is null */
@Composable
private fun FieldError(message: String?) {
    if (message != null) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            fontSize = 12.sp
        )
    }
}

/**
 * a button that shows the chosen date and opens a calendar to pick one
 *
 * @param label what the date is for, e.g. "Start date"
 * @param value the chosen date as YYYY-MM-DD, or empty if none is chosen
 * @param error an error message for this field, or null
 * @param onDateSelected called with the picked date as YYYY-MM-DD
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    label: String,
    value: String,
    error: String?,
    onDateSelected: (String) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    Column {
        OutlinedButton(
            onClick = { showPicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (value.isBlank()) "$label: select a date" else "$label: $value")
        }
        FieldError(error)
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = dateStringToMillis(value)
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { onDateSelected(millisToDateString(it)) }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

/**
 * checks every field of the lottery form
 *
 * dates are YYYY-MM-DD strings, so comparing them as text compares them as dates
 * (this is also how the Lottery model compares dates)
 *
 * @return a map of field key to error message; empty when everything is valid
 */
private fun validateLottery(
    selectedIds: Set<String>,
    campsiteNames: Map<String, String>,
    regStart: String,
    regEnd: String,
    bookStart: String,
    bookEnd: String,
    winners: String,
    waitlistOn: Boolean,
    capacity: String,
    today: String,
    checkStartIsTodayOrLater: Boolean,
    minCapacity: Int,
    existingLotteries: List<Lottery>,
    editingId: String
): Map<String, String> {
    val errors = mutableMapOf<String, String>()

    // US 02.04.02: at least one campsite
    if (selectedIds.isEmpty()) {
        errors[ERR_CAMPSITES] = "Select at least one campsite."
    }

    // US 02.04.03: registration period
    if (regStart.isBlank()) {
        errors[ERR_REG_START] = "Pick a registration start date."
    } else if (checkStartIsTodayOrLater && regStart < today) {
        errors[ERR_REG_START] = "The start date must be today or later."
    }
    if (regEnd.isBlank()) {
        errors[ERR_REG_END] = "Pick a registration end date."
    } else if (regStart.isNotBlank() && regEnd <= regStart) {
        errors[ERR_REG_END] = "The end date must be after the start date."
    }

    // US 02.04.04: booking period (must start after registration ends)
    if (bookStart.isBlank()) {
        errors[ERR_BOOK_START] = "Pick a booking start date."
    } else if (regEnd.isNotBlank() && bookStart <= regEnd) {
        errors[ERR_BOOK_START] = "Booking must start after registration ends."
    }
    if (bookEnd.isBlank()) {
        errors[ERR_BOOK_END] = "Pick a booking end date."
    } else if (bookStart.isNotBlank() && bookEnd <= bookStart) {
        errors[ERR_BOOK_END] = "The end date must be after the start date."
    }

    // US 02.04.05: whole number of at least 1
    val winnerCount = winners.trim().toIntOrNull()
    if (winnerCount == null || winnerCount < 1) {
        errors[ERR_WINNERS] = "Enter a whole number of at least 1."
    }

    // US 02.04.07: optional waitlist capacity
    if (waitlistOn) {
        val capacityNumber = capacity.trim().toIntOrNull()
        if (capacityNumber == null || capacityNumber < 1) {
            errors[ERR_CAPACITY] = "Enter a whole number of at least 1."
        } else if (capacityNumber < minCapacity) {
            errors[ERR_CAPACITY] =
                "Capacity cannot be lower than the $minCapacity entrants already on the waitlist."
        }
    }

    // US 02.04.01 / 02.04.02: no overlapping booking periods for a campsite.
    // TODO (back end): repeat this check against the database before saving.
    if (bookStart.isNotBlank() && bookEnd.isNotBlank() && bookStart < bookEnd) {
        val clashing = mutableListOf<String>()
        for (id in selectedIds) {
            val clash = existingLotteries.any { lottery ->
                lottery.lotteryId != editingId &&
                        lottery.status != "Closed" &&
                        lottery.includesCampsite(id) &&
                        lottery.bookingOverlaps(bookStart, bookEnd)
            }
            if (clash) {
                clashing.add(campsiteNames[id] ?: id)
            }
        }
        if (clashing.isNotEmpty()) {
            val message = clashing.joinToString(", ") +
                    " already ${if (clashing.size == 1) "has" else "have"} a lottery with an " +
                    "overlapping booking period."
            // shown under the campsites, next to the checkboxes that caused it
            errors[ERR_CAMPSITES] = message
        }
    }

    return errors
}

/** today's date in the device time zone as YYYY-MM-DD */
/** today's date in the lottery timezone */
internal fun todayString(): String = LotteryTime.today()

// the date picker works in UTC milliseconds, so convert in UTC
private fun utcFormat(): SimpleDateFormat =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }

private fun millisToDateString(millis: Long): String = utcFormat().format(Date(millis))

private fun dateStringToMillis(date: String): Long? =
    if (date.isBlank()) null else try {
        utcFormat().parse(date)?.time
    } catch (_: Exception) {
        null
    }