package com.mobdeve.s12.mco

import android.app.DatePickerDialog
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.mobdeve.s12.mco.databinding.ActivityBookDetailsBinding
import com.mobdeve.s12.mco.databinding.ComponentDialogConfirmCancelBinding
import com.mobdeve.s12.mco.databinding.ComponentPopupBorrowBinding
import com.mobdeve.s12.mco.databinding.ComponentPopupTncBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class BookDetailsActivity : AppCompatActivity() {

    companion object {
        const val ID_KEY = "ID_KEY"
        const val TITLE_KEY = "TITLE_KEY"
        const val YEAR_PUBLISHED_KEY = "YEAR_PUBLISHED_KEY"
        const val AUTHORS_KEY = "AUTHORS_KEY"
        const val COVER_KEY = "COVER_KEY"
        const val STATUS_KEY = "STATUS_KEY"
        const val PUBLISHER_KEY = "PUBLISHER_KEY"
        const val SHELF_LOCATION_KEY = "SHELF_LOCATION_KEY"
        const val PAGES_KEY = "PAGES_KEY"
        const val DESCRIPTION_KEY = "DESCRIPTION_KEY"
    }

    private lateinit var viewBinding : ActivityBookDetailsBinding
    private val dateFormat = "MMM d, yyyy"
    private val shortenedDateFormat = "MMM d"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityBookDetailsBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        addBookToRecentlyViewed()
        setDataOnViews()
        styleStatusAndBorrowBtn()
        setInitialFavButtonUI()
        addListenerAndApiLimitBackBtn()
        addListenerBorrowBtn()
        addListenerCancelBtn()
        addListenerFavoriteBtn()
    }

    private fun setDataOnViews() {
        Glide.with(viewBinding.root.context)
            .load(this.intent.getStringExtra(COVER_KEY))
            .into(viewBinding.bookDetailsIvCover)
        Glide.with(viewBinding.root.context)
            .load(this.intent.getStringExtra(COVER_KEY))
            .into(viewBinding.bookDetailsIvCover2)
        viewBinding.bookDetailsTvTitle.text = this.intent.getStringExtra(TITLE_KEY)
        viewBinding.bookDetailsTvAuthors.text = this.intent.getStringExtra(AUTHORS_KEY)
        viewBinding.bookDetailsTvYear.text = this.intent.getStringExtra(YEAR_PUBLISHED_KEY)
        viewBinding.bookDetailsTvPublisher.text = "Published by ${this.intent.getStringExtra(PUBLISHER_KEY)}"
        viewBinding.bookDetailsTvShelfLocation.text = this.intent.getStringExtra(SHELF_LOCATION_KEY)
//        viewBinding.bookDetailsTvStatus.text = this.intent.getStringExtra(STATUS_KEY)
        viewBinding.bookDetailsTvPages.text = "${this.intent.getStringExtra(PAGES_KEY)} pages"
        viewBinding.bookDetailsTvDescription.text = this.intent.getStringExtra(DESCRIPTION_KEY)
    }

    private fun styleStatusAndBorrowBtn() {
        CoroutineScope(Dispatchers.Main).launch {
            // show progress bar
            viewBinding.bookDetailsLoadingCover.visibility = View.VISIBLE
            viewBinding.bookDetailsProgressBar.visibility = View.VISIBLE

            // get backend handlers
            val firestoreHandler = FirestoreHandler.getInstance(this@BookDetailsActivity)
            val authHandler = AuthHandler.getInstance(this@BookDetailsActivity)

            // get transaction and user data
            val latestTransactionOfBook = firestoreHandler.getLatestTransaction(this@BookDetailsActivity.intent.getStringExtra(ID_KEY)!!)
            val currentUserId = authHandler.getCurrentUser()?.uid

            // default
            setUIToUnavailable()

            // modify styling if status is available or borrowed
            if(latestTransactionOfBook == null ||
                latestTransactionOfBook.status == TransactionModel.Status.CANCELLED ||
                latestTransactionOfBook.status == TransactionModel.Status.RETURNED)  {
                setUIToAvailable()
            } else if(latestTransactionOfBook.user.userId == currentUserId) {
                setUIToBorrowed(latestTransactionOfBook.status, latestTransactionOfBook.expectedPickupDate, latestTransactionOfBook.expectedReturnDate)
            }

            // modify styling if user already is within limit of books
            val activeTransactionsCount = firestoreHandler.getActiveTransactionsCount()
            Log.d("BookDetailsActivity", "Number of active transactions is $activeTransactionsCount")
            if(activeTransactionsCount >= 10) {
                setUIToBorrowLimitReached()
            }

            // hide progress bar
            viewBinding.bookDetailsLoadingCover.visibility = View.GONE
            viewBinding.bookDetailsProgressBar.visibility = View.GONE
        }
    }

    private fun addListenerAndApiLimitBackBtn() {
        viewBinding.bookDetailsBackBtn.setOnClickListener(View.OnClickListener {
            finish()
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            viewBinding.bookDetailsBackBtn.visibility = View.VISIBLE
        } else {
            viewBinding.bookDetailsBackBtn.visibility = View.GONE
        }
    }

    private fun addListenerPopupBg(outerLayout: ConstraintLayout, content: ConstraintLayout, window: PopupWindow) {
        outerLayout.setOnClickListener(View.OnClickListener {
            window.dismiss()
        })

        content.setOnClickListener(View.OnClickListener {
            // Do nothing to prevent clicking on the content to dismiss window
        })
    }

    /*** Borrow Popup Functions ***/
    private fun addListenerBorrowBtn() {
        viewBinding.bookDetailsIbBorrowBtn.setOnClickListener(View.OnClickListener {
            val window = PopupWindow(this)
            val borrowPopupBinding = ComponentPopupBorrowBinding.inflate(layoutInflater)
            window.contentView = borrowPopupBinding.root
            window.height = ViewGroup.LayoutParams.MATCH_PARENT
            window.width = ViewGroup.LayoutParams.MATCH_PARENT
            window.setBackgroundDrawable(ColorDrawable(0x99000000.toInt()))

            addListenerPopupBg(borrowPopupBinding.borrowPopupCl, borrowPopupBinding.borrowPopupClContent, window)
            addListenerBorrowPopupDates(borrowPopupBinding, window)
            addListenerBorrowPopupTNC(borrowPopupBinding)
            addListenerCancelBtn(borrowPopupBinding.borrowPopupCancelBtn, window)
            addListenerConfirmBtn(window, borrowPopupBinding)

            window.showAtLocation(borrowPopupBinding.root, Gravity.CENTER, 0, 0)
        })
    }

    private fun addListenerBorrowPopupDates(borrowPopupBinding: ComponentPopupBorrowBinding, window: PopupWindow) {
        // pickup date
        val pickupCalendar = Calendar.getInstance()
        val pickupDatePicker = DatePickerDialog.OnDateSetListener {view, year, month, day ->
            pickupCalendar.set(Calendar.YEAR, year)
            pickupCalendar.set(Calendar.MONTH, month)
            pickupCalendar.set(Calendar.DAY_OF_MONTH, day)
            updateDate(borrowPopupBinding.borrowPopupTvStartDateValue, pickupCalendar)
        }

        borrowPopupBinding.borrowPopupClStartDateBtn.setOnClickListener(View.OnClickListener {
            val datePickerDialog = DatePickerDialog(borrowPopupBinding.root.context, R.style.DialogTheme, pickupDatePicker, pickupCalendar.get(Calendar.YEAR), pickupCalendar.get(Calendar.MONTH), pickupCalendar.get(Calendar.DAY_OF_MONTH))
            val rangeCalendar = Calendar.getInstance()
            datePickerDialog.datePicker.minDate = rangeCalendar.timeInMillis
            datePickerDialog.show()
        })

        // return date
        val returnCalendar = Calendar.getInstance()
        val returnDatePicker = DatePickerDialog.OnDateSetListener {view, year, month, day ->
            returnCalendar.set(Calendar.YEAR, year)
            returnCalendar.set(Calendar.MONTH, month)
            returnCalendar.set(Calendar.DAY_OF_MONTH, day)
            updateDate(borrowPopupBinding.borrowPopupTvEndDateValue, returnCalendar)
        }

        borrowPopupBinding.borrowPopupClEndDateBtn.setOnClickListener(View.OnClickListener {
            val datePickerDialog = DatePickerDialog(borrowPopupBinding.root.context, R.style.DialogTheme, returnDatePicker, returnCalendar.get(Calendar.YEAR), returnCalendar.get(Calendar.MONTH), returnCalendar.get(Calendar.DAY_OF_MONTH))
            val rangeCalendar = Calendar.getInstance()
            rangeCalendar.set(pickupCalendar.get(Calendar.YEAR), pickupCalendar.get(Calendar.MONTH), pickupCalendar.get(Calendar.DAY_OF_MONTH) + 1)
            datePickerDialog.datePicker.minDate = rangeCalendar.timeInMillis

            rangeCalendar.set(pickupCalendar.get(Calendar.YEAR), pickupCalendar.get(Calendar.MONTH), pickupCalendar.get(Calendar.DAY_OF_MONTH) + 7)
            datePickerDialog.datePicker.maxDate = rangeCalendar.timeInMillis

            datePickerDialog.show()
        })
    }

    private fun updateDate(dateHolder: TextView, calendar: Calendar) {
        val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.US)
        dateHolder.text = simpleDateFormat.format(calendar.time)
    }

    private fun addListenerCancelBtn(cancelBtn: Button, window: PopupWindow) {
        cancelBtn.setOnClickListener(View.OnClickListener {
            window.dismiss()
        })
    }

    private fun addListenerConfirmBtn(window: PopupWindow, borrowPopupBinding: ComponentPopupBorrowBinding) {
        borrowPopupBinding.borrowPopupConfirmBtn.setOnClickListener(View.OnClickListener {
            if(allBorrowFieldsFilled(borrowPopupBinding)) {
                // show progress bar
                viewBinding.bookDetailsLoadingCover.visibility = View.VISIBLE
                viewBinding.bookDetailsProgressBar.visibility = View.VISIBLE

                val expectedPickupDate = convertStringToTimestamp(borrowPopupBinding.borrowPopupTvStartDateValue.text.toString())
                val expectedReturnDate = convertStringToTimestamp(borrowPopupBinding.borrowPopupTvEndDateValue.text.toString())
                val transactionDate = Timestamp.now()
                val bookId = this.intent.getStringExtra(ID_KEY)

                val bookTitle = intent.getStringExtra(TITLE_KEY)
                val pickupDateString = borrowPopupBinding.borrowPopupTvStartDateValue.text.toString()
                val returnDateString = borrowPopupBinding.borrowPopupTvEndDateValue.text.toString()
                val bookPickupReminder = "Reminder to pick up by $pickupDateString"
                val bookReturnReminder = "Reminder to return by $returnDateString"
                val transReceipt = "Pick up by $pickupDateString and return by $returnDateString"
                val millisIn24Hours = 24 * 60 * 60 * 1000L
                val oneDayBeforePickup = expectedPickupDate!!.toDate().time - millisIn24Hours - transactionDate.toDate().time
                val oneDayBeforeReturn = expectedReturnDate!!.toDate().time - millisIn24Hours - transactionDate.toDate().time
                CoroutineScope(Dispatchers.Main).launch {
                    val firestoreHandler = FirestoreHandler.getInstance(this@BookDetailsActivity)
                    val transId = firestoreHandler.createTransaction(bookId!!, transactionDate, expectedPickupDate!!, expectedReturnDate!!)
                    setUIToBorrowed(TransactionModel.Status.FOR_PICKUP, expectedPickupDate, expectedReturnDate)

                    // hide progress bar
                    viewBinding.bookDetailsLoadingCover.visibility = View.GONE
                    viewBinding.bookDetailsProgressBar.visibility = View.GONE

                    val pickupNotifId = "{$transId}_pickup"
                    val returnNotifId = "{$transId}_return"
                    val borrowNotifId = "{$transId}_borrow"
                    Log.d("BookDetailsActivity", "Sending notification with pick up date $pickupDateString and return date $returnDateString")
                    NotificationReceiver.sendNotification(this@BookDetailsActivity, "Borrowed $bookTitle", transReceipt, borrowNotifId, borrowNotifId, 3)
                    NotificationReceiver.sendNotification(this@BookDetailsActivity, "Pick up $bookTitle", bookPickupReminder, pickupNotifId, pickupNotifId, oneDayBeforePickup)
                    NotificationReceiver.sendNotification(this@BookDetailsActivity, "Return $bookTitle", bookReturnReminder, returnNotifId, returnNotifId, oneDayBeforeReturn)
//                    NotificationReceiver.sendNotification(this@BookDetailsActivity, "Pick up $bookTitle", bookPickupReminder, pickupNotifId, pickupNotifId, 7 * 1000)
//                    NotificationReceiver.sendNotification(this@BookDetailsActivity, "Return $bookTitle", bookReturnReminder, returnNotifId, returnNotifId, 10 * 1000)

                    val toast = Toast.makeText(this@BookDetailsActivity, "Borrow transaction successfully made.", Toast.LENGTH_SHORT)
                    toast.show()
                    window.dismiss()
                }
            }
        })
    }

    private fun allBorrowFieldsFilled(borrowPopupBinding: ComponentPopupBorrowBinding) : Boolean {
        val defaultDateValue = "Select a date"
        if(borrowPopupBinding.borrowPopupTvStartDateValue.text.toString() == defaultDateValue ||
            borrowPopupBinding.borrowPopupTvEndDateValue.text.toString() == defaultDateValue ) {
            borrowPopupBinding.borrowPopupWarning.visibility = View.VISIBLE
            borrowPopupBinding.borrowPopupWarning.text = viewBinding.root.context.getString(R.string.warning_incomplete_fields)
        } else if(!borrowPopupBinding.borrowPopupCbTermsAndConditions.isChecked) {
            borrowPopupBinding.borrowPopupWarning.visibility = View.VISIBLE
            borrowPopupBinding.borrowPopupWarning.text = viewBinding.root.context.getString(R.string.warning_unchecked_tnc)
        } else {
            borrowPopupBinding.borrowPopupWarning.visibility = View.GONE
            return true
        }

        return false
    }

    private fun convertStringToTimestamp(strDate: String) : Timestamp? {
        val formatter = SimpleDateFormat(dateFormat, Locale.ENGLISH)

        try {
            val date: Date = formatter.parse(strDate)!!
            return Timestamp(date)
        } catch (e: Exception) {
            Log.e("BookDetailsActivity", "Error converting string to date: $e")
            return null
        }
    }

    private fun convertTimestampToString(timestamp: Timestamp) : String {
        val formatter = SimpleDateFormat(shortenedDateFormat, Locale.ENGLISH)
        return formatter.format(timestamp.toDate())

    }

    /*** Cancel Button Functions ***/

    private fun addListenerCancelBtn() {
        viewBinding.bookDetailsIbCancelBtn.setOnClickListener(View.OnClickListener {
            showConfirmCancelDialog()
        })
    }

    private fun showConfirmCancelDialog() {
        val confirmCancelDialogBinding =
            ComponentDialogConfirmCancelBinding.inflate(LayoutInflater.from(this))

        val dialog = AlertDialog.Builder(this,  R.style.WrapContentDialog)
            .setView(confirmCancelDialogBinding.root)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        confirmCancelDialogBinding.dialogConfirmCancelBtnCancel.setOnClickListener {
            dialog.dismiss()
        }

        confirmCancelDialogBinding.dialogConfirmCancelBtnConfirm.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                dialog.dismiss()

                viewBinding.bookDetailsLoadingCover.visibility = View.VISIBLE
                viewBinding.bookDetailsProgressBar.visibility = View.VISIBLE

                val firestoreHandler = FirestoreHandler.getInstance(this@BookDetailsActivity)
                val authHandler = AuthHandler.getInstance(this@BookDetailsActivity)

                val bookId = getBookId()
                val userId = authHandler.getUserUid()

                if(bookId != null && userId != null) {
                    val transactionId = firestoreHandler.getLatestTransactionId(userId, bookId)
                    if(transactionId != null) {
                        firestoreHandler.updateTransaction(transactionId, "status", TransactionModel.Status.CANCELLED.toString())
                        firestoreHandler.updateTransaction(transactionId, "canceledDate", Timestamp.now())

                        val bookTitle = intent.getStringExtra(TITLE_KEY)
                        val transaction = firestoreHandler.getLatestTransaction(bookId)
                        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
                        val pickupDateString = outputFormat.format(transaction!!.expectedPickupDate.toDate())
                        val returnDateString = outputFormat.format(transaction!!.expectedReturnDate.toDate())
                        val bookPickupReminder = "Reminder to pick up by $pickupDateString"
                        val bookReturnReminder = "Reminder to return by $returnDateString"
                        val pickupNotifId = "{$transactionId}_pickup"
                        val returnNotifId = "{$transactionId}_return"
                        Log.d("BookDetailsActivity", "Canceling notification with pick up date $pickupDateString and return date $returnDateString")
                        NotificationReceiver.cancelNotification(this@BookDetailsActivity, "Pick up $bookTitle", bookPickupReminder, pickupNotifId, pickupNotifId)
                        NotificationReceiver.cancelNotification(this@BookDetailsActivity, "Return $bookTitle", bookReturnReminder, returnNotifId, returnNotifId)

                    } else {
                        Log.e("BookDetailsActivity", "TransactionID is null when attempting to cancel transaction.")
                    }
                } else {
                    Log.e("BookDetailsActivity", "Either the bookId or userId is null when attempting to cancel transaction.")
                }

                setUIToAvailable()

                val toast = Toast.makeText(this@BookDetailsActivity, "Borrow request was successfully cancelled.", Toast.LENGTH_SHORT)
                toast.show()

                viewBinding.bookDetailsLoadingCover.visibility = View.GONE
                viewBinding.bookDetailsProgressBar.visibility = View.GONE
            }
        }

        dialog.show()
    }

    private fun getBookId() : String? {
        val bookCoverResource = this@BookDetailsActivity.intent.getStringExtra(COVER_KEY).toString()
        return extractIdFromUrl(bookCoverResource)
    }

    private fun extractIdFromUrl(url: String): String? {
        val regex = Regex("(?<=/books/publisher/content/images/frontcover/)[^?]+")
        val matchResult = regex.find(url)

        return matchResult?.value
    }

    /*** UI Update Functions ***/
    private fun setUIToAvailable() {
        viewBinding.bookDetailsIvStatus.setImageResource(R.drawable.icon_available)
        viewBinding.bookDetailsTvStatus.text = viewBinding.root.context.getString(R.string.book_available)
        viewBinding.bookDetailsTvStatus.setTextColor(ContextCompat.getColor(this@BookDetailsActivity, R.color.book_available))
        viewBinding.bookDetailsIbBorrowBtn.alpha = 1f
        viewBinding.bookDetailsIbBorrowBtn.text = viewBinding.root.context.getString(R.string.borrow_btn_available)
        viewBinding.bookDetailsIbBorrowBtn.isEnabled = true
        viewBinding.bookDetailsIbCancelBtn.visibility = View.GONE
        viewBinding.bookDetailsIbBorrowBtn.visibility = View.VISIBLE
    }

    private fun setUIToBorrowed(status: TransactionModel.Status, expectedPickupDate: Timestamp, expectedReturnDate: Timestamp) {
        viewBinding.bookDetailsIvStatus.setImageResource(R.drawable.icon_timer)
        viewBinding.bookDetailsTvStatus.setTextColor(ContextCompat.getColor(this@BookDetailsActivity, R.color.book_borrowed))
        viewBinding.bookDetailsIbBorrowBtn.alpha = 0.3f
        viewBinding.bookDetailsIbBorrowBtn.text = viewBinding.root.context.getString(R.string.borrow_btn_borrowed)
        viewBinding.bookDetailsIbBorrowBtn.isEnabled = false

        if(status == TransactionModel.Status.FOR_PICKUP) {
            viewBinding.bookDetailsIbCancelBtn.visibility = View.VISIBLE
            viewBinding.bookDetailsIbBorrowBtn.visibility = View.GONE
            viewBinding.bookDetailsTvStatus.text = "${viewBinding.root.context.getString(R.string.for_pickup)} ${convertTimestampToString(expectedPickupDate)}"
        } else if(status == TransactionModel.Status.TO_RETURN) {
            viewBinding.bookDetailsIbCancelBtn.visibility = View.GONE
            viewBinding.bookDetailsIbBorrowBtn.visibility = View.VISIBLE
            viewBinding.bookDetailsTvStatus.text = "${viewBinding.root.context.getString(R.string.to_return)} ${convertTimestampToString(expectedReturnDate)}"
        } else if(status == TransactionModel.Status.OVERDUE) {
            viewBinding.bookDetailsIbCancelBtn.visibility = View.GONE
            viewBinding.bookDetailsIbBorrowBtn.visibility = View.VISIBLE
            viewBinding.bookDetailsTvStatus.text = "${viewBinding.root.context.getString(R.string.to_return)} ${convertTimestampToString(expectedReturnDate)}"
        }
    }

    private fun setUIToUnavailable() {
        viewBinding.bookDetailsIvStatus.setImageResource(R.drawable.icon_unavailable)
        viewBinding.bookDetailsTvStatus.text = viewBinding.root.context.getString(R.string.book_unavailable)
        viewBinding.bookDetailsTvStatus.setTextColor(ContextCompat.getColor(this@BookDetailsActivity, R.color.book_unavailable))
        viewBinding.bookDetailsIbBorrowBtn.alpha = 0.3f
        viewBinding.bookDetailsIbBorrowBtn.text = viewBinding.root.context.getString(R.string.borrow_btn_unavailable)
        viewBinding.bookDetailsIbBorrowBtn.isEnabled = false
        viewBinding.bookDetailsIbCancelBtn.visibility = View.GONE
        viewBinding.bookDetailsIbBorrowBtn.visibility = View.VISIBLE
    }

    private fun setUIToBorrowLimitReached() {
        viewBinding.bookDetailsIbBorrowBtn.alpha = 0.3f
        viewBinding.bookDetailsIbBorrowBtn.text = "Borrow Limit Reached"
        viewBinding.bookDetailsIbBorrowBtn.isEnabled = false
        viewBinding.bookDetailsIbCancelBtn.visibility = View.GONE
        viewBinding.bookDetailsIbBorrowBtn.visibility = View.VISIBLE
    }

    /*** Favorite Button Functions ***/
    private fun setInitialFavButtonUI() {
        CoroutineScope(Dispatchers.Main).launch {
            viewBinding.bookDetailsLoadingCover.visibility = View.VISIBLE
            viewBinding.bookDetailsProgressBar.visibility = View.VISIBLE

            val firestoreHandler = FirestoreHandler.getInstance(this@BookDetailsActivity)
            val bookId = getBookId()

            if(bookId != null) {
                val isBookFavorited = firestoreHandler.isBookFavorited(bookId)
                if(isBookFavorited != null) {
                    updateFavButton(isBookFavorited)
                }
                else {
                    Log.e("BookDetailsActivity", "There was an error in checking if book was part of current user's favorites in Firestore.")
                }
            } else {
                Log.w("BookDetailsActivity", "Book ID is null when called in setInitialFavButtonUI()")
            }
            viewBinding.bookDetailsLoadingCover.visibility = View.GONE
            viewBinding.bookDetailsProgressBar.visibility = View.GONE
        }
    }

    private fun addListenerFavoriteBtn() {
        viewBinding.bookDetailsIbFavBtn.setOnClickListener(View.OnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val firestoreHandler = FirestoreHandler.getInstance(this@BookDetailsActivity)
                val bookId = getBookId()

                if(bookId != null) {

                    viewBinding.bookDetailsIbFavBtn.visibility = View.INVISIBLE
                    viewBinding.bookDetailsPbFavbtn.visibility = View.VISIBLE
                    val isBookFavorited = firestoreHandler.isBookFavorited(bookId)
                    if(isBookFavorited != null) {
                        if(isBookFavorited == true) {
                            firestoreHandler.removeFromFavorites(bookId)
                        } else if(isBookFavorited == false) {
                            firestoreHandler.addToFavorites(bookId)
                        }
                        updateFavButton(!isBookFavorited)
                    } else {
                        Log.e("BookDetailsActivity", "There was an error in checking if book was part of current user's favorites in Firestore.")
                    }
                    viewBinding.bookDetailsIbFavBtn.visibility = View.VISIBLE
                    viewBinding.bookDetailsPbFavbtn.visibility = View.GONE
                    
                } else {
                    Log.w("BookDetailsActivity", "Book ID is null when called in addListenerFavoriteBtn()")
                }
            }
        })
    }

    private fun updateFavButton(newIsBookFavorited : Boolean) {
        if(newIsBookFavorited) {
            viewBinding.bookDetailsIbFavBtn.setImageResource(R.drawable.icon_favorite_filled)
        } else {
            viewBinding.bookDetailsIbFavBtn.setImageResource(R.drawable.icon_favorite_outlined)
        }
    }

    /*** Terms and Conditions Popup Functions ***/
    private fun addListenerBorrowPopupTNC(borrowPopupBinding: ComponentPopupBorrowBinding) {
        borrowPopupBinding.borrowPopupTvTermsAndConditionsBtn.setOnClickListener(View.OnClickListener {
            val popupWindow = PopupWindow(borrowPopupBinding.root.context)
            val tncPopupBinding = ComponentPopupTncBinding.inflate(layoutInflater)
            popupWindow.contentView = tncPopupBinding.root
            popupWindow.height = ViewGroup.LayoutParams.MATCH_PARENT
            popupWindow.width = ViewGroup.LayoutParams.MATCH_PARENT
            popupWindow.setBackgroundDrawable(ColorDrawable(0x00000000.toInt()))

            addListenerPopupBg(tncPopupBinding.tncPopupCl, tncPopupBinding.tncPopupContent, popupWindow)
            addListenerTNCBackBtn(tncPopupBinding, popupWindow)

            popupWindow.showAtLocation(tncPopupBinding.root, Gravity.CENTER, 0, 0)
        })
    }

    private fun addListenerTNCBackBtn(tncPopupBinding: ComponentPopupTncBinding, popupWindow: PopupWindow) {
        tncPopupBinding.tncPopupBtn.setOnClickListener(View.OnClickListener {
            popupWindow.dismiss()
        })
    }

    private fun addBookToRecentlyViewed() {
        CoroutineScope(Dispatchers.Main).launch {
            val firestoreHandler = FirestoreHandler.getInstance(this@BookDetailsActivity)
            intent.getStringExtra(ID_KEY)?.let { firestoreHandler.addBookToRecentlyViewed(it) }
        }
    }
}