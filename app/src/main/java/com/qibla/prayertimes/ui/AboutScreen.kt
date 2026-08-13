package com.qibla.prayertimes.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qibla.prayertimes.BuildConfig
import com.qibla.prayertimes.R
import com.qibla.prayertimes.ui.theme.*

private const val DESIGNER_EMAIL = "Omidshahinmehr@gmail.com"

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NightMid)
            .padding(horizontal = 24.dp)
            .padding(top = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = AmberText)
            }
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.about_title), color = AmberText, fontWeight = FontWeight.Bold, fontSize = 19.sp)
        }

        Spacer(Modifier.height(36.dp))

        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = null,
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .border(1.dp, CardBorder, CircleShape)
        )

        Spacer(Modifier.height(18.dp))

        Text(
            stringResource(R.string.home_title),
            color = AmberText,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            stringResource(R.string.version_label, BuildConfig.VERSION_NAME),
            color = AmberMuted,
            fontSize = 12.sp
        )

        Spacer(Modifier.height(28.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(CardSurface)
                .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.designer_label), color = AmberFaint, fontSize = 11.sp)
            Spacer(Modifier.height(6.dp))
            Text(stringResource(R.string.designer_name), color = AmberText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:$DESIGNER_EMAIL")
                        }
                        context.startActivity(intent)
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Filled.Email, contentDescription = null, tint = BrassLight, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(DESIGNER_EMAIL, color = BrassLight, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(22.dp))

        Text(
            stringResource(R.string.about_footer),
            color = AmberFaint,
            fontSize = 11.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center
        )
    }
}
