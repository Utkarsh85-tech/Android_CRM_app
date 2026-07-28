package com.example.nexoworxcrmapp.ui.common

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

/**
 * A tappable field that opens the native Android date picker.
 * outputFormat: "yyyy-MM-dd" or "dd-MM-yyyy" — controls both how [value] is
 * parsed to preselect the calendar, and how the picked date is written back.
 */
@Composable
fun DatePickerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    outputFormat: String = "dd-MM-yyyy",
    placeholder: String = "Select date",
    labelColor: Color = Color(0xFF5A6A5E),
    borderColor: Color = Color(0xFFDCE5DF),
    surfaceColor: Color = Color.White,
    textColor: Color = Color(0xFF1B2B20),
    accentColor: Color = Color(0xFF1E5B3A),
) {
    val context = LocalContext.current

    fun parseExisting(): Triple<Int, Int, Int> {
        val cal = Calendar.getInstance()
        val parts = value.split("-")
        try {
            if (outputFormat == "yyyy-MM-dd" && parts.size == 3) {
                cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            } else if (outputFormat == "dd-MM-yyyy" && parts.size == 3) {
                cal.set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt())
            }
        } catch (e: Exception) { /* fall back to today */ }
        return Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
    }

    Column {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = labelColor, modifier = Modifier.padding(bottom = 6.dp))
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                .background(surfaceColor).border(1.dp, borderColor, RoundedCornerShape(10.dp))
                .clickable {
                    val (y, m, d) = parseExisting()
                    DatePickerDialog(context, { _, year, month, day ->
                        val mm = (month + 1).toString().padStart(2, '0')
                        val dd = day.toString().padStart(2, '0')
                        val picked = if (outputFormat == "yyyy-MM-dd") "$year-$mm-$dd" else "$dd-$mm-$year"
                        onValueChange(picked)
                    }, y, m, d).show()
                }
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                value.ifBlank { placeholder },
                fontSize = 14.sp,
                color = if (value.isBlank()) borderColor else textColor,
            )
            Icon(Icons.Default.CalendarToday, contentDescription = "Pick date", tint = accentColor, modifier = Modifier.size(18.dp))
        }
    }
}