package com.myapplications.mypasswords.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.myapplications.mypasswords.R

@Composable
fun AppMenuTray(closeDrawer: () -> Unit) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.background
    ) {
        Text("MyPasswords", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
        HorizontalDivider()

        // The Settings icon uses the default scale of 1.0
        DrawerItem(
            painter = rememberVectorPainter(Icons.Default.Settings),
            label = "Settings",
            onClick = closeDrawer
        )

        val context = LocalContext.current
        val intent = remember { Intent(Intent.ACTION_VIEW, Uri.parse("https://www.myapplications.store")) }

        // For the logo, we pass a custom scale value to make it visually larger
        DrawerItem(
            painter = painterResource(id = R.mipmap.myapplications_logo_foreground),
            label = "Visit MyApps Website",
            tint = null, // Use original logo colors
            iconScale = 1.5f, // Scale the icon up by 50%
            onClick = { context.startActivity(intent) }
        )
    }
}

/**
 * A unified DrawerItem that uses a "Box and Scale" technique for consistent icon sizing.
 */
@Composable
private fun DrawerItem(
    painter: Painter,
    label: String,
    onClick: () -> Unit,
    tint: Color? = MaterialTheme.colorScheme.onBackground,
    iconScale: Float = 1.0f // New parameter to control visual icon size
) {
    val rowModifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(horizontal = 16.dp, vertical = 12.dp)

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // This Box reserves a fixed 24.dp space in the layout for the icon.
        // This ensures the text alignment is always consistent.
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painter,
                contentDescription = label,
                tint = tint ?: Color.Unspecified,
                modifier = Modifier.scale(iconScale)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
    }
}