package com.qibla.prayertimes.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qibla.prayertimes.R
import com.qibla.prayertimes.ui.theme.*
import com.qibla.prayertimes.util.LocalePrefs

@Composable
fun LanguageScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var current by remember { mutableStateOf(LocalePrefs.get(context)) }

    val options = listOf(
        LocalePrefs.SYSTEM to stringResource(R.string.language_system),
        "en" to stringResource(R.string.language_en),
        "fa" to stringResource(R.string.language_fa),
        "ar" to stringResource(R.string.language_ar)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NightMid)
            .padding(horizontal = 20.dp)
            .padding(top = 28.dp, bottom = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = AmberText)
            }
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.language_title), color = AmberText, fontWeight = FontWeight.Bold, fontSize = 19.sp)
        }

        Spacer(Modifier.height(20.dp))

        options.forEach { (code, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardSurface)
                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                    .clickable {
                        current = code
                        LocalePrefs.set(context, code)
                        (context as? Activity)?.recreate()
                    }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, color = AmberText, fontSize = 15.sp, modifier = Modifier.weight(1f))
                if (current == code) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = BrassLight, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}
