package worker

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

fun main() {
    val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.getApplicationDefault())
        .setProjectId("cmput301f26-staaks")
        .build()

    val app = FirebaseApp.initializeApp(options)
    val db = FirestoreClient.getFirestore(app)

    val repository = LotteryWorkerRepository(db)
    val scheduler = Executors.newSingleThreadScheduledExecutor()

    // closes resources when the program stops
    Runtime.getRuntime().addShutdownHook(Thread {
        scheduler.shutdownNow()

        try {
            db.close()
        } finally {
            app.delete()
        }
    })

    println("Staaks Lottery Draw Checker is running")
    println("Watching all eligible lotteries")
    println("Press Ctrl+C to stop")

    // checks immediately and waits one minute between completed checks
    scheduler.scheduleWithFixedDelay(
        {
            try {
                println("Checking eligible lotteries...")

                repository.processAll()

                println("Check finished - next check in one minute")
            } catch (error: Exception) {
                System.err.println(
                    "Check failed - will retry next minute: ${error.message}"
                )
            }
        },
        0,
        1,
        TimeUnit.MINUTES
    )
}