package com.daymark.app.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val palette = listOf(
    "#E91E63", "#F44336", "#FF9800", "#FFEB3B",
    "#4CAF50", "#2196F3", "#9C27B0", "#00BCD4",
)

@Composable
fun ChangeColorDialog(
    activity: com.daymark.app.data.Activity,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var color by remember { mutableStateOf(activity.color) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Change color") },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    palette.forEach { c ->
                        val selected = c == color
                        Box(
                            Modifier
                                .size(if (selected) 32.dp else 28.dp)
                                .background(Color(android.graphics.Color.parseColor(c)), CircleShape)
                                .border(
                                    width = if (selected) 3.dp else 0.dp,
                                    color = Color.White,
                                    shape = CircleShape,
                                )
                                .clickable { color = c },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(color) }) {
                Text("Save", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
fun AddActivityDialog(onConfirm: (String, String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var color by remember { mutableStateOf(palette[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("New activity") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.size(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    palette.forEach { c ->
                        val selected = c == color
                        Box(
                            Modifier
                                .size(if (selected) 32.dp else 28.dp)
                                .background(Color(android.graphics.Color.parseColor(c)), CircleShape)
                                .border(
                                    width = if (selected) 3.dp else 0.dp,
                                    color = Color.White,
                                    shape = CircleShape,
                                )
                                .clickable { color = c },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim(), color) },
                enabled = name.isNotBlank(),
            ) { Text("Add", color = if (name.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
