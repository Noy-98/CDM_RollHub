package com.itech.cdmrollhub.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.EventDay
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.itech.cdmrollhub.R
import com.itech.cdmrollhub.databinding.FragmentPayrollBinding
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class Payroll : Fragment() {

    private lateinit var binding: FragmentPayrollBinding
    private lateinit var calendarView: CalendarView
    private lateinit var databaseRef: DatabaseReference
    private lateinit var databaseRef3: DatabaseReference
    private val databaseRef2 = FirebaseDatabase.getInstance().reference
    private lateinit var auth: FirebaseAuth
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Define color drawables for event day indicators
    private val colorDrawables = listOf(
        R.drawable.green_event,
        R.drawable.blue_event,
        R.drawable.red_event
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPayrollBinding.inflate(inflater, container, false)
        calendarView = binding.payPeriod
        auth = FirebaseAuth.getInstance()

        // Initialize Firebase Database reference
        databaseRef = FirebaseDatabase.getInstance().getReference("EmployeePayrollDataTbl")
        databaseRef3 = FirebaseDatabase.getInstance().getReference("payrollSlipTbl")

        fetchAndHighlightPayPeriods()
        fetchAndDisplayPayrollData()
        displayProfilePicture()
        setupFragmentNavigation()
        checkForNotifications()

        binding.printBttn.setOnClickListener {
            checkPermissionsAndGeneratePdf()
        }

        return binding.root
    }

    private fun checkPermissionsAndGeneratePdf() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                100
            )
        } else {
            fetchDataAndGeneratePdf()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchDataAndGeneratePdf()
        } else {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchDataAndGeneratePdf() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = databaseRef3.child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val employeeId = snapshot.child("EmployeeData/employee_id").getValue(String::class.java) ?: "N/A"
                    val firstName = snapshot.child("EmployeeData/first_name").getValue(String::class.java) ?: "N/A"
                    val lastName = snapshot.child("EmployeeData/last_name").getValue(String::class.java) ?: "N/A"
                    val payPeriod = snapshot.child("PayPeriod/2134353").getValue(String::class.java) ?: "N/A"
                    val totalDeductionPerPay = snapshot.child("TotalDeductionPerPay/total_deduction").getValue(String::class.java) ?: "N/A"
                    val totalDeductionWholeYear = snapshot.child("TotalDeductionWholeYear/total_deduction").getValue(String::class.java) ?: "N/A"
                    val totalHours = snapshot.child("TotalHours/total_hours").getValue(String::class.java) ?: "N/A"
                    val totalSalary = snapshot.child("TotalSalary/total_salary").getValue(String::class.java) ?: "N/A"
                    val totalYear = snapshot.child("TotalYear/total_year").getValue(String::class.java) ?: "N/A"

                    val data = """
                    Employee ID: $employeeId
                    First Name: $firstName
                    Last Name: $lastName
                    Pay Period: $payPeriod
                    Total Deduction Per Pay: $totalDeductionPerPay
                    Total Deduction Whole Year: $totalDeductionWholeYear
                    Total Hours: $totalHours
                    Total Salary: $totalSalary
                    Total Year: $totalYear
                """.trimIndent()

                    generatePdf(data)
                } else {
                    Toast.makeText(requireContext(), "No payroll data found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun generatePdf(data: String) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint()
        var y = 20 // Start from the top of the page
        data.lines().forEach { line ->
            canvas.drawText(line, 10f, y.toFloat(), paint)
            y += 20 // Move to the next line
        }

        pdfDocument.finishPage(page)

        // Save the document to the Documents folder
        val documentsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        if (!documentsFolder.exists()) {
            documentsFolder.mkdirs() // Create the folder if it doesn't exist
        }
        val file = File(documentsFolder, "PayrollSlip.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(requireContext(), "PDF saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error saving PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }


    private fun checkForNotifications() {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return
        val notificationsRef = databaseRef2.child("NotificationTbl").child(currentUserId)

        notificationsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Check if the snapshot has any children (notifications)
                if (snapshot.exists() && snapshot.hasChildren()) {
                    // Change ImageView color to red if there are notifications
                    binding.notificationBttn.setColorFilter(resources.getColor(R.color.red), android.graphics.PorterDuff.Mode.SRC_IN)
                } else {
                    // Reset ImageView color if no notifications
                    binding.notificationBttn.clearColorFilter()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("checkForNotifications", "Error fetching notifications: ${error.message}")
            }
        })
    }

    private fun setupFragmentNavigation() {
        binding.notificationBttn.setOnClickListener {
            val notificationFragment = Notification()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, notificationFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun displayProfilePicture() {
        val currentUserId = firebaseAuth.currentUser?.uid

        if (currentUserId != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("usersTbl").child(currentUserId)

            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val userMap = snapshot.value as? Map<String, Any?>
                    if (userMap != null) {
                        val profileImageUrl = snapshot.child("profilePicture").getValue(String::class.java)

                        if (!profileImageUrl.isNullOrEmpty()) {
                            Picasso.get().load(profileImageUrl).into(binding.profilePic)
                        } else {
                            binding.profilePic.setImageResource(R.drawable.camera_icon)
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "User Profile Picture does not exist!", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to retrieve Profile Picture", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("displayUser", "User is not logged in")
        }
    }

    private fun fetchAndDisplayPayrollData() {
        val userId = auth.currentUser?.uid ?: return

        // Reference to the user's payroll data
        val userRef = databaseRef.child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Fetch and display total hours
                    val totalHoursValue = snapshot.child("TotalHours/total_hours").getValue(Any::class.java)
                    binding.totalHours.text = totalHoursValue.toString() ?: "0"

                    // Fetch and display total overtime hours
                    val totalDeductionValue = snapshot.child("TotalDeduction/total_deduction").getValue(Any::class.java)
                    binding.totalDeduction.text = totalDeductionValue.toString() ?: "0"

                    // Fetch and display total salary
                    val totalSalaryValue = snapshot.child("TotalSalary/total_salary").getValue(Any::class.java)
                    binding.totalSalary.text = totalSalaryValue.toString() ?: "0"

                    // Fetch and display total years
                    val totalYearValue = snapshot.child("TotalYear/total_year").getValue(Any::class.java)
                    binding.totalYear.text = totalYearValue.toString() ?: "0"
                } else {
                    Toast.makeText(context, "No payroll data found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchAndHighlightPayPeriods() {
        val userId = auth.currentUser?.uid ?: return

        // Reference to the user's PayPeriod data
        val payPeriodRef = databaseRef.child(userId).child("PayPeriod")

        payPeriodRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val payDates = mutableListOf<Date>()
                    for (dateSnapshot in snapshot.children) {
                        val dateString = dateSnapshot.getValue(String::class.java)
                        dateString?.let {
                            val date = dateFormat.parse(it)
                            date?.let { parsedDate ->
                                payDates.add(parsedDate)
                            }
                        }
                    }
                    highlightPayPeriods(payDates)
                } else {
                    Toast.makeText(context, "No pay periods found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun highlightPayPeriods(payDates: List<Date>) {
        val events = mutableListOf<EventDay>()
        var colorIndex = 0
        var count = 0

        payDates.sorted().forEach { date ->
            // For each 15-day cycle, change the highlight color
            if (count == 15) {
                colorIndex = (colorIndex + 1) % colorDrawables.size
                count = 0
            }

            // Create Calendar object for each pay date and set EventDay with color drawable
            val calendar = Calendar.getInstance().apply { time = date }
            val eventDay = EventDay(calendar, colorDrawables[colorIndex])
            events.add(eventDay)

            count++
        }

        // Set events in CalendarView
        calendarView.setEvents(events)
    }
}
