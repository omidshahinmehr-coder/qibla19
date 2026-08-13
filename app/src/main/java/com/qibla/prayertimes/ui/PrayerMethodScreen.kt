package com.qibla.prayertimes.ui

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qibla.prayertimes.R
import com.qibla.prayertimes.data.PrayerCalculationMethod
import com.qibla.prayertimes.ui.theme.*
import com.qibla.prayertimes.viewmodel.QiblaViewModel

@Composable
fun PrayerMethodScreen(viewModel: QiblaViewModel, onBack: () -> Unit) {
    val current by viewModel.prayerMethod.collectAsState()

    val options = listOf(
        PrayerCalculationMethod.TEHRAN to (stringResource(R.string.prayer_method_tehran) to stringResource(R.string.prayer_method_tehran_desc)),
        PrayerCalculationMethod.JAFARI to (stringResource(R.string.prayer_method_jafari) to stringResource(R.string.prayer_method_jafari_desc))
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
            Text(stringResource(R.string.prayer_method_title), color = AmberText, fontWeight = FontWeight.Bold, fontSize = 19.sp)
        }

        Spacer(Modifier.height(20.dp))

        options.forEach { (method, labelAndDesc) ->
            val (label, desc) = labelAndDesc
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardSurface)
                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                    .clickable { viewModel.setPrayerMethod(method) }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(label, color = AmberText, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(3.dp))
                    Text(desc, color = AmberMuted, fontSize = 12.sp, lineHeight = 17.sp)
                }
                if (current == method) {
                    Spacer(Modifier.width(10.dp))
                    Icon(Icons.Filled.Check, contentDescription = null, tint = BrassLight, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}
