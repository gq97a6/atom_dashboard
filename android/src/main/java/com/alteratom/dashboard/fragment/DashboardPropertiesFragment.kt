package com.alteratom.dashboard.fragment

import android.app.Activity.RESULT_OK
import android.content.Context
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.activity.MainActivity
import com.alteratom.dashboard.fragment.TileIconFragment.Companion.getIconColorPallet
import com.alteratom.dashboard.fragment.TileIconFragment.Companion.getIconHSV
import com.alteratom.dashboard.fragment.TileIconFragment.Companion.getIconRes
import com.alteratom.dashboard.fragment.TileIconFragment.Companion.setIconHSV
import com.alteratom.dashboard.fragment.TileIconFragment.Companion.setIconKey
import com.alteratom.dashboard.compose_daemon.DashboardPropertiesCompose
import com.alteratom.dashboard.compose_global.BasicButton
import com.alteratom.dashboard.compose_global.EditText
import com.alteratom.dashboard.compose_global.NavigationArrows
import com.alteratom.dashboard.compose_global.composeConstruct
import com.alteratom.dashboard.createToast
import com.alteratom.dashboard.helper_objects.Debug
import com.alteratom.dashboard.helper_objects.FragmentManager.fm
import com.alteratom.dashboard.app.AtomApp.Companion.aps
import com.alteratom.dashboard.switcher.FragmentSwitcher
import java.io.InputStream
import kotlin.math.abs
import kotlin.random.Random

class DashboardPropertiesFragment : Fragment() {

    lateinit var requestAction: (uri: Uri, inputStream: InputStream) -> Unit
    lateinit var request: ActivityResultLauncher<Intent>

    override fun onAttach(context: Context) {
        super.onAttach(context)

        //Set gesture reaction
        MainActivity.onGlobalTouch = { e ->
            FragmentSwitcher.handle(e, DashboardPropertiesFragment())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        request =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    result.data?.data?.also { uri ->
                        try {
                            requireContext().contentResolver.openInputStream(uri)
                                ?.use { inputStream -> requestAction(uri, inputStream) }
                        } catch (e: Exception) {
                            Debug.recordException(e)
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
    ): View = composeConstruct(requireContext()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
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
                        getIconHSV = { aps.dashboard.hsv }
                        getIconRes = { aps.dashboard.iconRes }
                        getIconColorPallet = { aps.dashboard.pallet }

                        setIconHSV = { hsv -> aps.dashboard.hsv = hsv }
                        setIconKey = { key -> aps.dashboard.iconKey = key }

                        fm.replaceWith(TileIconFragment())
                    },
                    border = BorderStroke(1.dp, aps.dashboard.pallet.cc.color),
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        painterResource(aps.dashboard.iconRes),
                        "",
                        tint = aps.dashboard.pallet.cc.color
                    )
                }

                var name by remember { mutableStateOf(aps.dashboard.name) }
                EditText(
                    label = { Text("Dashboard name") },
                    value = name,
                    onValueChange = { it ->
                        name = it
                        it.trim().let {
                            aps.dashboard.name = it.ifBlank {
                                abs(Random.nextInt(1000, 9999)).toString()
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .onFocusChanged {
                            //Update field after unFocus in case user left it blank
                            //and it got generated in the background
                            if (!it.isFocused) name = aps.dashboard.name
                        }
                )
            }

            DashboardPropertiesCompose.Compose(
                aps.dashboard.type,
                this@DashboardPropertiesFragment
            )

            if (!aps.settings.hideNav && aps.dashboards.size > 1) Spacer(modifier = Modifier.height(60.dp))
        }

        if (!aps.settings.hideNav && aps.dashboards.size > 1) {
            NavigationArrows(
                { FragmentSwitcher.switch(false, DashboardPropertiesFragment()) },
                { FragmentSwitcher.switch(true, DashboardPropertiesFragment()) }
            )
        }
    }
}