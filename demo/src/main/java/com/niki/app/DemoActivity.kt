package com.niki.app

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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
    private var showing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        val userBtn = findViewById<Button>(R.id.user_btn)
        val shizukuBtn = findViewById<Button>(R.id.shizuku_btn)
        val rootBtn = findViewById<Button>(R.id.root_btn)

        val toolBar = findViewById<Toolbar>(R.id.tool_bar)

        val edBtn = findViewById<Button>(R.id.ed_btn)
        val ed = findViewById<EditText>(R.id.ed)
        val output = findViewById<TextView>(R.id.output)

        val manager = ShellManager(this)

        userBtn.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val r = UserShell().test()
                showDialog("User", r)
            }
        }

        shizukuBtn.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val r = ShizukuShell(this@DemoActivity).test()
                showDialog("Shizuku", r)
            }
        }

        rootBtn.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val r = RootShell().test()
                showDialog("Root", r)
            }
        }

        setSupportActionBar(toolBar)
        toolBar.setViewInsets { insets ->
            topMargin = insets.top
        }

        edBtn.setOnClickListener {
            val input = ed.text.toString()
            lifecycleScope.launch(Dispatchers.IO) {
                val r = manager.exec(input)
                withContext(Dispatchers.Main) {
                    output.text = r.toUIString()
                    ed.text.clear()
                }
            }
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
        if (showing) return@withContext
        showing = true
        MaterialAlertDialogBuilder(this@DemoActivity)
            .setTitle(title)
            .setMessage(result.toUIString())
            .setCancelable(true)
            .setPositiveButton("确认") { _, _ ->
                showing = false
            }
            .setNegativeButton("取消") { _, _ ->
                showing = false
            }
            .setOnCancelListener {
                showing = false
            }.create().show()
    }

    private fun ShellResult.toUIString(): String =
        "exit code: $exitCode\noutput: $output"

    override fun onDestroy() {
        super.onDestroy()
        showing = false
    }
}