package com.example.staaks_campsite_app


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.firebase.Firebase
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.firestore
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory


/**
 * Scans campsite QR codes and opens the matching campsite details.
 *
 * @param navController controls navigation to other screens.
 */
@Composable
fun QRScannerScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var message by remember { mutableStateOf<String?>(null) }
    var scanFinished by remember { mutableStateOf(false) }
    var checkingCampsite by remember { mutableStateOf(false) }
    var screenActive by remember { mutableStateOf(false) }

    val scanner = remember(context) {
        DecoratedBarcodeView(context).apply {
            setDecoderFactory(DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE)))
            setStatusText("")
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
    }

    val scanCallback = remember(scanner, navController) {
        object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult) {
                if (!screenActive || scanFinished) return

                scanFinished = true
                val campsiteId = CampsiteQrCode.readCampsiteId(result.text.orEmpty())

                if (campsiteId == null) {
                    message = "This is not a valid campsite QR code."
                    return
                }

                checkingCampsite = true
                message = "Checking campsite..."

                Firebase.firestore.collection("campsites")
                    .document(campsiteId)
                    .get(Source.SERVER)
                    .addOnCompleteListener { task ->
                        if (!screenActive) return@addOnCompleteListener

                        checkingCampsite = false

                        when {
                            !task.isSuccessful -> {
                                message = "Could not check the campsite. Try again."
                            }

                            task.result?.exists() != true -> {
                                message = "This campsite is no longer available."
                            }

                            else -> {
                                navController.navigate(
                                    "entree_campsite/${Uri.encode(campsiteId)}"
                                ) {
                                    popUpTo("qr_scanner_screen") {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
            }
        }
    }

    DisposableEffect(scanner, lifecycleOwner, permissionGranted) {
        screenActive = true

        fun updateCamera() {
            val resumed = lifecycleOwner.lifecycle.currentState
                .isAtLeast(Lifecycle.State.RESUMED)

            if (permissionGranted && resumed) {
                scanner.resume()
            } else {
                scanner.pause()
            }
        }

        val observer = LifecycleEventObserver { _, _ -> updateCamera() }
        lifecycleOwner.lifecycle.addObserver(observer)
        updateCamera()

        onDispose {
            screenActive = false
            lifecycleOwner.lifecycle.removeObserver(observer)
            scanner.pause()
        }
    }

    LaunchedEffect(Unit) {
        if (!permissionGranted) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(permissionGranted) {
        if (permissionGranted) {
            scanner.decodeSingle(scanCallback)
        }
    }
    Column(

    ) {
        /*
        Pinned Top Row
         */
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()            // Makes the row span the full width
                .background(color = Color(0xFF1B5E20))
                .padding(16.dp)

        ){
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                Button(
                    onClick = {
                        navController.navigate("start")

                    },

                    ) {
                    Text("Back")
                }
            }


            Text(
                text = "QR Scanner",
                fontSize = 25.sp,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {

                Button(
                    onClick = { navController.navigate("profile_screen") }
                ) {
                    Text("Profile")
                }

            }
        }

        // This is for when the admin is spoofing a different user. Logic must be implemented here to allow this to pop up
        if (false){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()            // Makes the row span the full width
                    .background(color = Color(0xFF63B267))
                    .heightIn(max = 45.dp)
                    .padding(5.dp),
                horizontalArrangement = Arrangement.SpaceEvenly

            ) {
                Text(
                    text = "View:",
                    fontSize = 15.sp,
                    color = Color.White
                )
                Button(onClick = { /* ... */ }) { Text("Admin") }
                Button(onClick = { /* ... */ }) { Text("Owner") }
                Button(onClick = { /* ... */ }) { Text("Entree") }


            }
        }







        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center

        ) {
            /*
            -------------------------------------------------------------------------------------------------------------------------------------
            Create Main Content of Page Here
            -------------------------------------------------------------------------------------------------------------------------------------
             */
            if (permissionGranted) {
                AndroidView(
                    factory = { scanner },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Camera access is needed to scan QR codes.")

                    Button(
                        onClick = {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    ) {
                        Text("Allow camera")
                    }
                }
            }

        }






        message?.let { Text(it) }

        if (permissionGranted && scanFinished && !checkingCampsite) {
            Button(
                onClick = {
                    message = null
                    scanFinished = false
                    scanner.decodeSingle(scanCallback)
                }
            ) {
                Text("Scan again")
            }
        }

        // Pinned Bottom Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.LightGray)
                .padding(5.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { navController.navigate("entree_res_screen") }) { Text("1") }
            Button(onClick = { navController.navigate("entree_inbox_screen") }) { Text("2") }
            Button(onClick = { navController.navigate("messages_screen") }) { Text("3") }
            Button(onClick = { /* ... */ }) { Text("4") }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.LightGray)
                .heightIn(max = 16.dp)
                .heightIn(min = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

        }
    }






}