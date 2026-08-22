package com.budgetplusplus.feature.accounts

import android.content.Context
import android.content.res.Configuration
import android.text.TextUtils
import android.view.View
import androidx.test.core.app.ApplicationProvider
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class) @Config(sdk=[35])
class ArabicRtlScreenTest {
 @Test fun `arabic resources and platform direction are RTL`(){val locale=Locale.forLanguageTag("ar-DZ");val configuration=Configuration(ApplicationProvider.getApplicationContext<Context>().resources.configuration).apply{setLocale(locale);setLayoutDirection(locale)};val localized=ApplicationProvider.getApplicationContext<Context>().createConfigurationContext(configuration);assertEquals("لا توجد حسابات",localized.getString(R.string.accounts_empty_title));assertEquals(View.LAYOUT_DIRECTION_RTL,TextUtils.getLayoutDirectionFromLocale(locale));assertEquals(View.LAYOUT_DIRECTION_RTL,configuration.layoutDirection)}
}
