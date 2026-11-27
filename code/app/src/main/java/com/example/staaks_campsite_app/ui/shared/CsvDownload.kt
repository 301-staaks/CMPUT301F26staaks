package com.example.staaks_campsite_app.ui.shared

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/**
 * saves CSV text to the entrant's device (US 01.05.02, US 01.07.02)
 *
 * opens Android's "save as" screen so the entrant picks where the file goes.
 * no storage permission is needed.
 *
 * usage on a screen with a "Download CSV" button:
 *
 *     var csvError by remember { mutableStateOf("") }
 *     val downloadCsv = rememberCsvDownloader(onError = { csvError = it })
 *     Button(onClick = {
 *         downloadCsv(csvFileName("reservation", name, start), reservationCsv(...))
 *     }) { Text("Download CSV") }
 *     if (csvError.isNotBlank()) Text(csvError, color = Color.Red)
 *
 * @param onError called with a message to show if the file could not be saved
 * @param onSaved called once the file is written
 * @return a function taking the suggested file name and the CSV text
 */
@Composable
fun rememberCsvDownloader(
    onError: (String) -> Unit = {},
    onSaved: () -> Unit = {}
): (fileName: String, csvText: String) -> Unit {
    val context = LocalContext.current
    var pendingText by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        val text = pendingText
        pendingText = null

        // null uri means the entrant backed out of the save screen
        if (uri == null || text == null) {
            return@rememberLauncherForActivityResult
        }

        try {
            val stream = context.contentResolver.openOutputStream(uri)
            if (stream == null) {
                onError("Could not create the CSV file.")
            } else {
                stream.use { it.write(text.toByteArray(Charsets.UTF_8)) }
                onSaved()
            }
        } catch (e: Exception) {
            onError("Could not save the CSV file.")
        }
    }

    return { fileName, csvText ->
        pendingText = csvText
        try {
            launcher.launch(fileName)
        } catch (e: ActivityNotFoundException) {
            pendingText = null
            onError("No app on this device can save the CSV file.")
        }
    }
}
