package com.example.atdd

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlin.random.Random

class AddMemberActivity : AppCompatActivity() {

    private lateinit var textInputLayoutMemberId: TextInputLayout
    private lateinit var editMemberId: TextInputEditText
    private lateinit var checkBoxAutoGenerate: MaterialCheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_member)

        setupToolbar()
        setupForm()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupForm() {
        textInputLayoutMemberId = findViewById(R.id.textInputLayoutMemberId)
        editMemberId = findViewById(R.id.editMemberId)
        val editMemberName = findViewById<TextInputEditText>(R.id.editMemberName)
        val buttonAddMember = findViewById<MaterialButton>(R.id.buttonAddMember)
        checkBoxAutoGenerate = findViewById(R.id.checkBoxAutoGenerate)

        // チェックボックスの状態に応じてID入力欄を有効/無効化
        checkBoxAutoGenerate.setOnCheckedChangeListener { _, isChecked ->
            editMemberId.isEnabled = !isChecked
            if (isChecked) {
                editMemberId.setText("")
                textInputLayoutMemberId.error = null
            }
        }

        buttonAddMember.setOnClickListener {
            val memberName = editMemberName.text.toString().trim()

            // 自動生成がONの場合はIDは不要
            if (checkBoxAutoGenerate.isChecked) {
                if (memberName.isEmpty()) {
                    editMemberName.error = "Member name is required"
                } else {
                    val generatedId = generateMemberId()
                    Toast.makeText(
                        this,
                        getString(R.string.member_added_success) + " (ID: $generatedId)",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            } else {
                // 手動入力の場合はIDも必須
                val memberId = editMemberId.text.toString().trim()

                when {
                    memberId.isEmpty() -> {
                        editMemberId.error = "Member ID is required"
                    }
                    memberName.isEmpty() -> {
                        editMemberName.error = "Member name is required"
                    }
                    else -> {
                        Toast.makeText(
                            this,
                            getString(R.string.member_added_success),
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            }
        }
    }

    private fun generateMemberId(): String {
        val randomNumber = Random.nextInt(1000, 9999)
        return "DA-$randomNumber"
    }
}
