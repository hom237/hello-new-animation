package com.daily.new_amime.for_my.daily_anime_app.ui.wiget

import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent

class NewAnimeWidget(private val name: String): GlanceAppWidget() {
    @Composable
    fun Content() {
        Text(text = "Hello $name")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Content()
        }
    }
}
