package com.alteratom.dashboard.activities.fragments.dashboard_properties

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.alteratom.dashboard.EditText
import com.alteratom.dashboard.G.dashboard
import com.alteratom.dashboard.G.dashboards
import com.alteratom.dashboard.G.settings
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.NavigationArrows
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.compose.ComposeTheme
import com.alteratom.dashboard.createToast
import com.alteratom.dashboard.switcher.FragmentSwitcher
import java.io.InputStream
import java.util.*
import kotlin.math.abs
import kotlin.random.Random

class DashboardPropertiesFragment : Fragment() {

    lateinit var requestAction: (uri: Uri, inputStream: InputStream) -> Unit
    lateinit var request: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        theme.apply(context = requireContext())

        request =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    result.data?.data?.also { uri ->
                        try {
                            requireContext().contentResolver.openInputStream(uri)
                                ?.use { inputStream -> requestAction(uri, inputStream) }
                        } catch (e: java.lang.Exception) {
                            createToast(requireContext(), "Certificate error")
                        }
                    }
                }
            }
    }

    inline fun openFile(crossinline action: (String, String?) -> Unit) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }

        requestAction = { uri, inputStream ->
            action(
                inputStream.bufferedReader().use { it.readText() },
                runCatching {
                    requireContext().contentResolver.query(
                        uri,
                        null,
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        cursor.moveToFirst()
                        return@use cursor.getColumnIndexOrThrow(
                            OpenableColumns.DISPLAY_NAME
                        ).let(cursor::getString)
                    }
                }.getOrNull()
            )
        }

        request.launch(intent)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                //Background
                Box(modifier = Modifier.background(Theme.colors.background))

                ComposeTheme(Theme.isDark) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        FragmentSwitcher.handle(
                                            awaitPointerEvent(),
                                            DashboardPropertiesFragment()
                                        )
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(modifier = Modifier.padding(16.dp)) {
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                Text(
                                    text = "Dashboard",
                                    fontSize = 45.sp,
                                    color = Theme.colors.color

                                )
                                Text(
                                    modifier = Modifier.offset(y = (-10).dp),
                                    text = "properties",
                                    fontSize = 35.sp,
                                    color = Theme.colors.a

                                )

                                var name by remember {
                                    mutableStateOf(dashboard.name.lowercase(Locale.getDefault()))
                                }
                                EditText(
                                    label = { Text("Dashboard name") },
                                    value = name,
                                    onValueChange = {
                                        name = it

                                        it.trim().let {
                                            dashboard.name =
                                                it.ifBlank {
                                                    abs(Random.nextInt(0, 100)).toString()
                                                }
                                        }
                                    }
                                )

                                DashboardPropertiesCompose.compose(
                                    dashboard.type,
                                    this@DashboardPropertiesFragment
                                )
                            }
                        }
                    }

                    if (!settings.hideNav && dashboards.size > 1) {
                        NavigationArrows(
                            { FragmentSwitcher.switch(false, DashboardPropertiesFragment()) },
                            { FragmentSwitcher.switch(true, DashboardPropertiesFragment()) }
                        )
                    }
                }
            }
        }
    }
}