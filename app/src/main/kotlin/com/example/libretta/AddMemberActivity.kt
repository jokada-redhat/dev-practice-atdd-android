package com.example.libretta

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.libretta.member.RegisterMemberRequest
import com.example.libretta.member.RegisterMemberResult
import com.example.libretta.member.RegisterMemberUseCase
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AddMemberActivity : AppCompatActivity() {

    private lateinit var textInputLayoutMemberId: TextInputLayout
    private lateinit var editMemberId: TextInputEditText
    private lateinit var checkBoxAutoGenerate: MaterialCheckBox
    private val app by lazy { application as LibrettaApplication }
    private val registerMemberUseCase by lazy { RegisterMemberUseCase(app.memberRepository) }

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

            val request = RegisterMemberRequest(name = memberName)
            when (val result = registerMemberUseCase.execute(request)) {
                is RegisterMemberResult.Success -> {
                    Toast.makeText(
                        this,
                        getString(R.string.member_added_success) + " (ID: ${result.member.id})",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }

                is RegisterMemberResult.Failure -> {
                    editMemberName.error = result.errorMessage
                }

                is RegisterMemberResult.ValidationError -> {
                    editMemberName.error = result.message
                }
            }
        }
    }
}
