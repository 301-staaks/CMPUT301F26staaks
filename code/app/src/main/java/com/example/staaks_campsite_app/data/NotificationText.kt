package com.example.staaks_campsite_app.data

/*
 * notification titles and text shared by the app and the lottery draw worker.
 * moved here from NotificationLogic.kt unchanged. this file must not import
 * firebase, since the worker compiles it with the Admin SDK instead
 */

const val WIN_TITLE = "You won the lottery!"
const val NOT_SELECTED_TITLE = "Lottery result"
const val CONFIRMATION_TITLE = "Reservation confirmed"

/** the text of a win notification (US 01.03.01) */
fun winMessage(campsiteName: String): String =
    "You were selected for $campsiteName. Open this notification to confirm your reservation."

/** the text of a not-selected notification (US 01.03.02) */
fun notSelectedMessage(campsiteName: String): String =
    "You were not selected for $campsiteName this time, " +
            "but you may still be selected if a winner declines."
