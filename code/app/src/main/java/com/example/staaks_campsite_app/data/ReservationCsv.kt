package com.example.staaks_campsite_app.data

/*
 * builds the CSV files an entrant can download (US 01.05.02, US 01.07.02)
 *
 * plain text only, no firestore and no android, so it can be unit tested.
 * the screen that shows the "Download CSV" button passes the text to
 * rememberCsvDownloader in ui/shared/CsvDownload.kt to save it.
 */

/** the CSV columns for a confirmed reservation (US 01.05.02) */
val RESERVATION_CSV_HEADER = listOf(
    "Entrant Name", "Campsite Name", "Location", "Arrival Date", "Departure Date"
)

/** the CSV columns for a cancelled reservation (US 01.07.02) */
val CANCELLATION_CSV_HEADER = listOf(
    "Entrant Name", "Campsite Name", "Location",
    "Original Arrival Date", "Original Departure Date", "Cancellation Date"
)

/**
 * makes one value safe to put in a CSV cell
 *
 * values with a comma, quote or line break are wrapped in quotes, with quotes
 * doubled. values starting with = + - @ get a leading ' so a spreadsheet shows
 * them as text instead of running them as a formula
 *
 * @param value the text for the cell
 * @return the cell as it goes in the file
 */
fun csvField(value: String): String {
    var text = value
    if (text.isNotEmpty() && text[0] in "=+-@\t\r") {
        text = "'$text"
    }
    val needsQuotes = text.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
    return if (needsQuotes) "\"" + text.replace("\"", "\"\"") + "\"" else text
}

/**
 * @param rows the header first, then the data rows
 * @return the CSV text, one line per row, ending with a line break
 */
fun toCsv(rows: List<List<String>>): String =
    rows.joinToString(separator = "\r\n", postfix = "\r\n") { row ->
        row.joinToString(",") { csvField(it) }
    }

/**
 * the CSV for a confirmed reservation (US 01.05.02)
 *
 * @param entrantName the entrant's name
 * @param campsiteName the campsite's name
 * @param location the campsite's location
 * @param arrivalDate the first day of the stay, YYYY-MM-DD
 * @param departureDate the last day of the stay, YYYY-MM-DD
 */
fun reservationCsv(
    entrantName: String,
    campsiteName: String,
    location: String,
    arrivalDate: String,
    departureDate: String
): String = toCsv(
    listOf(
        RESERVATION_CSV_HEADER,
        listOf(entrantName, campsiteName, location, arrivalDate, departureDate)
    )
)

/**
 * the CSV for a cancelled reservation (US 01.07.02)
 *
 * same columns as [reservationCsv] plus the cancellation date
 *
 * @param cancellationDate the day it was cancelled, YYYY-MM-DD
 */
fun cancellationCsv(
    entrantName: String,
    campsiteName: String,
    location: String,
    arrivalDate: String,
    departureDate: String,
    cancellationDate: String
): String = toCsv(
    listOf(
        CANCELLATION_CSV_HEADER,
        listOf(entrantName, campsiteName, location, arrivalDate, departureDate, cancellationDate)
    )
)

/**
 * a file name like "reservation_Pine_Lake_2027-06-01.csv"
 *
 * @param prefix "reservation" or "cancellation"
 * @param campsiteName the campsite's name
 * @param date the arrival date
 */
fun csvFileName(prefix: String, campsiteName: String, date: String): String {
    val safeName = campsiteName.trim()
        .replace(Regex("[^A-Za-z0-9-]+"), "_")
        .trim('_')
        .ifBlank { "campsite" }
        .take(40)
    val safeDate = date.replace(Regex("[^0-9-]"), "")
    return if (safeDate.isBlank()) "${prefix}_$safeName.csv" else "${prefix}_${safeName}_$safeDate.csv"
}
