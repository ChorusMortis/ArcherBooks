package com.mobdeve.s12.mco

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s12.mco.databinding.ActivityBookDetailsBinding
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.mobdeve.s12.mco.databinding.ComponentPopupBorrowBinding
import com.mobdeve.s12.mco.databinding.ComponentPopupTncBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class BookDetailsActivity : AppCompatActivity() {

    companion object {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityBookDetailsBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        setDataOnViews()
        addListenerAndApiLimitBackBtn()
        addListenerBorrowBtn()
    }

    private fun setDataOnViews() {
        viewBinding.bookDetailsIvCover.setImageResource(this.intent.getIntExtra(COVER_KEY, R.drawable.book_grinch))
        viewBinding.bookDetailsIvCover2.setImageResource(this.intent.getIntExtra(COVER_KEY, R.drawable.book_grinch))
        viewBinding.bookDetailsTvTitle.text = this.intent.getStringExtra(TITLE_KEY)
        viewBinding.bookDetailsTvAuthors.text = this.intent.getStringExtra(AUTHORS_KEY)
        viewBinding.bookDetailsTvYear.text = this.intent.getIntExtra(YEAR_PUBLISHED_KEY, -1).toString()
        viewBinding.bookDetailsTvPublisher.text = "Published by ${this.intent.getStringExtra(PUBLISHER_KEY)}"
        viewBinding.bookDetailsTvShelfLocation.text = this.intent.getStringExtra(SHELF_LOCATION_KEY)
        this.intent.getStringExtra(STATUS_KEY)?.let { setStatusIcon(it) }
        viewBinding.bookDetailsTvStatus.text = this.intent.getStringExtra(STATUS_KEY)
        viewBinding.bookDetailsTvPages.text = "${this.intent.getIntExtra(PAGES_KEY, 0).toString()} pages"
        viewBinding.bookDetailsTvDescription.text = this.intent.getStringExtra(DESCRIPTION_KEY)
    }

    private fun setStatusIcon(status: String) {
        val statusResource : Int

        if(status == "Book Available") {
            statusResource = R.drawable.icon_available
        } else if(status == "Book Unavailable") {
            statusResource = R.drawable.icon_unavailable
        } else if(status == "Overdue") {
            statusResource = R.drawable.icon_overdue
            viewBinding.bookDetailsTvStatus.setTextColor(ContextCompat.getColor(this, R.color.book_borrowed))
        } else {
            statusResource = R.drawable.icon_timer
            viewBinding.bookDetailsTvStatus.setTextColor(ContextCompat.getColor(this, R.color.book_borrowed))
        }
        viewBinding.bookDetailsIvStatus.setImageResource(statusResource)
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
            addListenerConfirmBtn(borrowPopupBinding.borrowPopupTvStartDateValue.text.toString(), borrowPopupBinding.borrowPopupTvEndDateValue.text.toString(), window, borrowPopupBinding.borrowPopupConfirmBtn)

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
        val dateFormat = "MMM d, yyyy"
        val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.US)
        dateHolder.text = simpleDateFormat.format(calendar.time)
    }

    private fun addListenerCancelBtn(cancelBtn: Button, window: PopupWindow) {
        cancelBtn.setOnClickListener(View.OnClickListener {
            window.dismiss()
        })
    }

    private fun addListenerConfirmBtn(pickupDate: String, returnDate: String, window: PopupWindow, confirmBtn: Button) {
        // TODO MCO3: Handle adding of transactions to users
        confirmBtn.setOnClickListener(View.OnClickListener {
            window.dismiss()
        })
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