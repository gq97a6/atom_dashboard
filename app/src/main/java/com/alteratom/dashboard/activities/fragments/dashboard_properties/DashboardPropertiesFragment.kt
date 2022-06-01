package com.alteratom.dashboard.activities.fragments.dashboard_properties

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.alteratom.R
import com.alteratom.dashboard.DialogBuilder.dialogSetup
import com.alteratom.dashboard.EditText
import com.alteratom.dashboard.G.dashboard
import com.alteratom.dashboard.G.dashboards
import com.alteratom.dashboard.G.settings
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.NavigationArrows
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.activities.MainActivity.Companion.fm
import com.alteratom.dashboard.compose.ComposeTheme
import com.alteratom.dashboard.createToast
import com.alteratom.dashboard.recycler_view.RecyclerViewAdapter
import com.alteratom.dashboard.recycler_view.RecyclerViewItem
import com.alteratom.dashboard.switcher.FragmentSwitcher
import com.alteratom.databinding.DialogCopyBrokerBinding
import java.io.InputStream
import java.util.*
import kotlin.math.abs
import kotlin.random.Random

class DashboardPropertiesFragment : Fragment() {

    var openCert: (action: (uri: Uri, inputStream: InputStream) -> Unit) -> Unit = {}

    private lateinit var requestAction: (uri: Uri, inputStream: InputStream) -> Unit
    private lateinit var request: ActivityResultLauncher<Intent>

    val copyProperties: () -> Unit = {
        if (dashboards.size <= 1) {
            createToast(requireContext(), "No dashboards to copy from")
        } else {
            val dialog = Dialog(requireContext())
            val adapter = RecyclerViewAdapter<RecyclerViewItem>(requireContext())

            val list = MutableList(dashboards.size) {
                RecyclerViewItem(
                    R.layout.item_copy_broker
                )
            }
            list.removeAt(dashboards.indexOf(dashboard))

            dialog.setContentView(R.layout.dialog_copy_broker)
            val binding = DialogCopyBrokerBinding.bind(dialog.findViewById(R.id.root))

            adapter.setHasStableIds(true)
            adapter.onBindViewHolder = { _, holder, pos ->
                val p = pos + if (pos >= dashboards.indexOf(dashboard)) 1 else 0
                val text = holder.itemView.findViewById<TextView>(R.id.icb_text)
                text.text = dashboards[p].name.uppercase(Locale.getDefault())
            }

            adapter.onItemClick = {
                val pos = adapter.list.indexOf(it)
                val p = pos + if (pos >= dashboards.indexOf(dashboard)) 1 else 0

                dashboard.mqtt = dashboards[p].mqtt.copy()
                dashboard.mqtt.clientId = abs(Random.nextInt()).toString()
                dashboard.daemon.notifyOptionsChanged()

                fm.replaceWith(DashboardPropertiesFragment(), false)
                dialog.dismiss()
            }

            binding.dcbRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.dcbRecyclerView.adapter = adapter

            adapter.submitList(list)

            dialog.dialogSetup()
            theme.apply(binding.root)
            dialog.show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        theme.apply(context = requireContext())

        request = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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

        openCert = { action ->
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            requestAction = action
            request.launch(intent)
        }
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
                                    modifier = Modifier.offset(y = -10.dp),
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