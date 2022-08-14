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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.alteratom.dashboard.*
import com.alteratom.dashboard.G.dashboard
import com.alteratom.dashboard.G.dashboards
import com.alteratom.dashboard.G.settings
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.activities.MainActivity
import com.alteratom.dashboard.activities.fragments.*
import com.alteratom.dashboard.activities.fragments.TileIconFragment.Companion.getIconColorPallet
import com.alteratom.dashboard.activities.fragments.TileIconFragment.Companion.getIconHSV
import com.alteratom.dashboard.activities.fragments.TileIconFragment.Companion.getIconRes
import com.alteratom.dashboard.activities.fragments.TileIconFragment.Companion.setIconHSV
import com.alteratom.dashboard.activities.fragments.TileIconFragment.Companion.setIconKey
import com.alteratom.dashboard.compose.BasicButton
import com.alteratom.dashboard.compose.ComposeTheme
import com.alteratom.dashboard.compose.EditText
import com.alteratom.dashboard.compose.NavigationArrows
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
        theme.apply(context = requireContext())
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

                                Row(
                                    modifier = Modifier.padding(top = 5.dp),
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    BasicButton(
                                        contentPadding = PaddingValues(13.dp),
                                        onClick = {
                                            getIconHSV = { dashboard.hsv }
                                            getIconRes = { dashboard.iconRes }
                                            getIconColorPallet = { dashboard.pallet }

                                            setIconHSV = { hsv -> dashboard.hsv = hsv }
                                            setIconKey = { key -> dashboard.iconKey = key }

                                            MainActivity.fm.replaceWith(TileIconFragment())
                                        },
                                        border = BorderStroke(1.dp, dashboard.pallet.cc.color),
                                        modifier = Modifier.size(52.dp)
                                    ) {
                                        Icon(
                                            painterResource(dashboard.iconRes),
                                            "",
                                            tint = dashboard.pallet.cc.color
                                        )
                                    }

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
                                        },
                                        modifier = Modifier.padding(start = 12.dp)
                                    )
                                }

                                DashboardPropertiesCompose.compose(
                                    dashboard.type,
                                    this@DashboardPropertiesFragment
                                )

                                //var securityLevel by remember { mutableStateOf(dashboard.securityLevel) }
                                //var excludeNavigation by remember { mutableStateOf(dashboard.excludeNavigation) }
//
                                //FrameBox("Security") {
                                //    Column {
                                //        LabeledCheckbox(
                                //            label = {
                                //                Text(
                                //                    "Exclude from quick navigation",
                                //                    fontSize = 15.sp,
                                //                    color = Theme.colors.a
                                //                )
                                //            },
                                //            checked = excludeNavigation,
                                //            onCheckedChange = {
                                //                excludeNavigation = it
                                //                dashboard.excludeNavigation = it
                                //            },
                                //            modifier = Modifier.padding(vertical = 10.dp)
                                //        )
//
                                //        RadioGroup(
                                //            listOf(
                                //                "Never",
                                //                "Once (unlock until app minimized)",
                                //                "Every time",
                                //            ),
                                //            "Access authentication level",
                                //            securityLevel,
                                //            {
                                //                requireContext().buildConfirm(
                                //                    "Confirm change",
                                //                    "CONFIRM",
                                //                    {
                                //                        securityLevel = it
                                //                        dashboard.securityLevel = it
                                //                    }
                                //                )
                                //            },
                                //            modifier = Modifier.padding(top = 5.dp)
                                //        )
                                //    }
                                //}
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