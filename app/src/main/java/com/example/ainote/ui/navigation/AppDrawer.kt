package com.example.ainote.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ainote.ui.theme.GradientEnd
import com.example.ainote.ui.theme.GradientStart

@Composable
fun AppDrawerContent(
    userName: String,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onClose: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(300.dp)
    ) {
        // Header
        Box(
            Modifier.fillMaxWidth().height(160.dp)
                .background(Brush.horizontalGradient(listOf(GradientStart, GradientEnd)))
                .padding(20.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(userName.take(1).uppercase(), fontSize = 20.sp,
                        fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(userName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                    Text("SmartNotes AI", style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f))
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        val items = listOf(
            Triple(Icons.Outlined.Dashboard, "Dashboard", Screen.Dashboard.route),
            Triple(Icons.Outlined.DocumentScanner, "Scan Document", Screen.Scanner.route),
            Triple(Icons.Outlined.Description, "My Notes", Screen.Notes.route),
            Triple(Icons.Outlined.Alarm, "Reminders", Screen.Reminder.route),
            Triple(Icons.Outlined.Person, "Profile", Screen.Profile.route)
        )

        items.forEach { (icon, label, route) ->
            val selected = currentRoute == route
            NavigationDrawerItem(
                icon = { Icon(icon, label) },
                label = { Text(label, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal) },
                selected = selected,
                onClick = { onNavigate(route); onClose() },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}
