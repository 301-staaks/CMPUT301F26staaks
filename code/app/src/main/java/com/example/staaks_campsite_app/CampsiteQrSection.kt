package com.example.staaks_campsite_app

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * shows and downloads the qr code for one campsite
 */
@Composable
fun CampsiteQrSection(campsiteId: String) {
    val context = LocalContext.current

    var qrImage by remember(campsiteId) {
        mutableStateOf<Bitmap?>(null)
    }

    var message by remember(campsiteId) {
        mutableStateOf<String?>(null)
    }

    // keeps the image being saved if the screen is refreshed
    var imageToSave by remember {
        mutableStateOf<Bitmap?>(null)
    }

    val downloadLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/png")
    ) { uri ->
        val image = imageToSave
        imageToSave = null

        if (uri != null) {
            try {
                checkNotNull(image)

                val output = context.contentResolver
                    .openOutputStream(uri)
                    ?: error("Could not open the selected file.")

                output.use { stream ->
                    val saved = image.compress(
                        Bitmap.CompressFormat.PNG,
                        100,
                        stream
                    )

                    check(saved)
                }

                message = "QR code saved."
            } catch (_: Exception) {
                message = "Could not save the QR code. Please try again."
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = {
                try {
                    // checks that the campsite has an id
                    check(campsiteId.isNotBlank()) {
                        "The campsite ID is empty"
                    }

                    // creates the image for this campsite
                    qrImage = CampsiteQrCode.createImage(campsiteId)
                    message = "QR code generated"
                } catch (error: Exception) {
                    message = "Could not generate QR: ${error.message}"
                }
            }
        ) {
            Text("Generate QR")
        }

        val image = qrImage

        if (image != null) {
            Image(
                bitmap = image.asImageBitmap(),
                contentDescription = "Campsite QR code",
                modifier = Modifier
                    .background(Color.White)
                    .padding(8.dp)
                    .size(220.dp)
            )

            Button(
                onClick = {
                    try {
                        imageToSave = image
                        downloadLauncher.launch("campsite-qr.png")
                    } catch (_: Exception) {
                        imageToSave = null
                        message = "Could not open the download screen."
                    }
                }
            ) {
                Text("Download QR")
            }
        }

        val currentMessage = message

        if (currentMessage != null) {
            Text(currentMessage)
        }
    }
}