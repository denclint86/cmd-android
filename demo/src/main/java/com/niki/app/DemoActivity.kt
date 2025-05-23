package com.niki.app

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.niki.cmd.RootShell
import com.niki.cmd.Shell
import com.niki.cmd.ShizukuShell
import com.niki.cmd.UserShell
import com.niki.cmd.model.bean.ShellResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class DemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        val userBtn = findViewById<Button>(R.id.userBtn)
        val shizukuBtn = findViewById<Button>(R.id.shizukuBtn)
        val rootBtn = findViewById<Button>(R.id.rootBtn)
        val toolBar = findViewById<Toolbar>(R.id.tool_bar)

        userBtn.setOnClickListener {
            lifecycleScope.launch {
                val r = UserShell().test()
                showDialog("User", r)
            }
        }

        shizukuBtn.setOnClickListener {
            lifecycleScope.launch {
                val r = ShizukuShell(this@DemoActivity).test()
                showDialog("Shizuku", r)
            }
        }

        rootBtn.setOnClickListener {
            lifecycleScope.launch {
                val r = RootShell().test()
                showDialog("Root", r)
            }
        }

        setSupportActionBar(toolBar)
        toolBar.setViewInsets { insets ->
            topMargin = insets.top
        }
    }

    private suspend fun Shell.test() =
        exec(
            "echo test",
            10_000
        )

    private fun View.setViewInsets(block: ViewGroup.MarginLayoutParams.(Insets) -> Unit) {
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                block(insets) // 将 insets 传递给 block
            }
            WindowInsetsCompat.CONSUMED
        }
    }

    private suspend fun showDialog(
        title: String,
        result: ShellResult
    ) = withContext(Dispatchers.Main) {
        MaterialAlertDialogBuilder(this@DemoActivity)
            .setTitle(title)
            .setMessage("${result.output}\n${result.exitCode}")
            .setCancelable(true)
            .setPositiveButton("确认") { _, _ ->
            }
            .setNegativeButton("取消") { _, _ ->
            }
            .setOnCancelListener {
            }.create().show()
    }
}