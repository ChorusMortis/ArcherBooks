package com.mobdeve.s12.mco

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s12.mco.databinding.ActivityBookDetailsBinding
import android.graphics.drawable.ColorDrawable
import android.os.Build
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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.mobdeve.s12.mco.databinding.ComponentDialogConfirmCancelBinding
import com.mobdeve.s12.mco.databinding.ComponentDialogConfirmlogoutBinding
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

        setDataOnViews()
        addListenerAndApiLimitBackBtn()
        styleStatusAndBorrowBtn()
        addListenerBorrowBtn()
        addListenerCancelBtn()
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
        viewBinding.bookDetailsTvStatus.text = this.intent.getStringExtra(STATUS_KEY)
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

            // default styling (for unavailable book status)
            var statusResource = R.drawable.icon_unavailable
            var statusText = viewBinding.root.context.getString(R.string.book_unavailable)
            var textColor = ContextCompat.getColor(this@BookDetailsActivity, R.color.book_unavailable)
            var borrowBtnOpacity = 0.3f
            var borrowBtnText = "Unavailable"
            var borrowBtnEnabled = false

            // modify styling if status is available or borrowed
            if(latestTransactionOfBook == null ||
                latestTransactionOfBook.status == TransactionModel.Status.CANCELLED ||
                latestTransactionOfBook.status == TransactionModel.Status.RETURNED)  {
                statusResource = R.drawable.icon_available
                statusText = viewBinding.root.context.getString(R.string.book_available)
                textColor = ContextCompat.getColor(this@BookDetailsActivity, R.color.book_available)
                borrowBtnOpacity = 1.0f
                borrowBtnText = "Borrow"
                borrowBtnEnabled = true
            } else if(latestTransactionOfBook.user.userId == currentUserId) {
                statusResource = R.drawable.icon_timer
                textColor = ContextCompat.getColor(this@BookDetailsActivity, R.color.book_borrowed)
                borrowBtnText = "Borrowed"
                borrowBtnEnabled = false

                if(latestTransactionOfBook.status == TransactionModel.Status.FOR_PICKUP) {
                    viewBinding.bookDetailsIbCancelBtn.visibility = View.VISIBLE
                    viewBinding.bookDetailsIbBorrowBtn.visibility = View.GONE
                    statusText = "${viewBinding.root.context.getString(R.string.for_pickup)} ${convertTimestampToString(latestTransactionOfBook.expectedPickupDate)}"
                } else {

                   if(latestTransactionOfBook.status == TransactionModel.Status.TO_RETURN) {
                        statusText = "${viewBinding.root.context.getString(R.string.to_return)} ${convertTimestampToString(latestTransactionOfBook.expectedReturnDate)}"
                   } else if(latestTransactionOfBook.status == TransactionModel.Status.OVERDUE) {
                        statusText = "${viewBinding.root.context.getString(R.string.to_return)} ${convertTimestampToString(latestTransactionOfBook.expectedReturnDate)}"
                   }
                }
            }

            // set appropriate styling
            viewBinding.bookDetailsIvStatus.setImageResource(statusResource)
            viewBinding.bookDetailsTvStatus.text = statusText
            viewBinding.bookDetailsTvStatus.setTextColor(textColor)
            viewBinding.bookDetailsIbBorrowBtn.alpha = borrowBtnOpacity
            viewBinding.bookDetailsIbBorrowBtn.text = borrowBtnText
            viewBinding.bookDetailsIbBorrowBtn.isEnabled = borrowBtnEnabled

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

                CoroutineScope(Dispatchers.Main).launch {
                    val firestoreHandler = FirestoreHandler.getInstance(this@BookDetailsActivity)
                    firestoreHandler.createTransaction(bookId!!, transactionDate, expectedPickupDate!!, expectedReturnDate!!)

                    viewBinding.bookDetailsIvStatus.setImageResource(R.drawable.icon_timer)
                    viewBinding.bookDetailsTvStatus.text = "${viewBinding.root.context.getString(R.string.for_pickup)} ${convertTimestampToString(expectedPickupDate)}"
                    viewBinding.bookDetailsTvStatus.setTextColor(ContextCompat.getColor(this@BookDetailsActivity, R.color.book_borrowed))

                    viewBinding.bookDetailsIbBorrowBtn.visibility = View.GONE
                    viewBinding.bookDetailsIbCancelBtn.visibility = View.VISIBLE

                    // hide progress bar
                    viewBinding.bookDetailsLoadingCover.visibility = View.GONE
                    viewBinding.bookDetailsProgressBar.visibility = View.GONE

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

    private fun addListenerCancelBtn() {
        viewBinding.bookDetailsIbCancelBtn.setOnClickListener(View.OnClickListener {
            showConfirmCancelDialog()
        })
    }

    private fun showConfirmCancelDialog() {
        val confirmCancelDialogBinding =
            ComponentDialogConfirmCancelBinding.inflate(LayoutInflater.from(this))
        // use custom style to force dialog to wrap content and not take up entire screen's width
        val dialog = AlertDialog.Builder(this,  R.style.WrapContentDialog)
            .setView(confirmCancelDialogBinding.root)
            .setCancelable(true)
            .create()

        // make background transparent so dialog floats
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        confirmCancelDialogBinding.dialogConfirmCancelBtnCancel.setOnClickListener {
            dialog.dismiss()
        }

        confirmCancelDialogBinding.dialogConfirmCancelBtnConfirm.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                dialog.dismiss()

                // show progress bar
                viewBinding.bookDetailsLoadingCover.visibility = View.VISIBLE
                viewBinding.bookDetailsProgressBar.visibility = View.VISIBLE

                val firestoreHandler = FirestoreHandler.getInstance(this@BookDetailsActivity)
                val authHandler = AuthHandler.getInstance(this@BookDetailsActivity)

                val bookCoverResource = this@BookDetailsActivity.intent.getStringExtra(COVER_KEY).toString()
                val bookId = extractIdFromUrl(bookCoverResource)
                val userId = authHandler.getUserUid()

                if(bookId != null && userId != null) {
                    val transactionId = firestoreHandler.getLatestTransactionId(userId, bookId)
                    if(transactionId != null) {
                        firestoreHandler.updateTransaction(transactionId, "status", TransactionModel.Status.CANCELLED.toString())
                        firestoreHandler.updateTransaction(transactionId, "canceledDate", Timestamp.now())
                    } else {
                        Log.e("BookDetailsActivity", "TransactionID is null when attempting to cancel transaction.")
                    }
                } else {
                    Log.e("BookDetailsActivity", "Either the bookId or userId is null when attempting to cancel transaction.")
                }

                viewBinding.bookDetailsIvStatus.setImageResource(R.drawable.icon_available)
                viewBinding.bookDetailsTvStatus.text = viewBinding.root.context.getString(R.string.book_available)
                viewBinding.bookDetailsTvStatus.setTextColor(ContextCompat.getColor(this@BookDetailsActivity, R.color.book_available))
                viewBinding.bookDetailsIbBorrowBtn.alpha = 1f
                viewBinding.bookDetailsIbBorrowBtn.text = "Book Available"
                viewBinding.bookDetailsIbBorrowBtn.isEnabled = true
                viewBinding.bookDetailsIbCancelBtn.visibility = View.GONE
                viewBinding.bookDetailsIbBorrowBtn.visibility = View.VISIBLE

                val toast = Toast.makeText(this@BookDetailsActivity, "Borrow request was successfully cancelled.", Toast.LENGTH_SHORT)
                toast.show()

                // show progress bar
                viewBinding.bookDetailsLoadingCover.visibility = View.GONE
                viewBinding.bookDetailsProgressBar.visibility = View.GONE
            }
        }

        dialog.show()
    }

    private fun extractIdFromUrl(url: String): String? {
        val regex = Regex("(?<=/books/publisher/content/images/frontcover/)[^?]+")
        val matchResult = regex.find(url)

        return matchResult?.value
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
}