package com.yukse.pomodorotimer

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.net.toUri
import androidx.preference.*
import com.yukse.pomodorotimer.database.GroupEntity
import com.yukse.pomodorotimer.databinding.SetGroupNameDialogBinding

class SettingPreferenceFragment : PreferenceFragmentCompat() {

    // preference에 생기는 좌측 여백 없애기
    private fun Preference.removeIconSpace() {
        isIconSpaceReserved = false
        if (this is PreferenceGroup) {
            for (i in 0 until preferenceCount) {
                getPreference(i).removeIconSpace()
            }
        }
    }

    //시스템 벨소리에서 알림음 선택하기 - intent
    private fun showRingtonePickerDialog(): Boolean {
        val default_sound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val alarm_sound = PreferenceManager.getDefaultSharedPreferences(context)
            .getString("alarm_sound", default_sound.toString())

        /*
        EXTRA_RINGONE_EXISTING_URI 현재 벨소리, 항목 옆에 체크 표시를 하는데 사용
        EXTRA_RINGTONE_SHOW_DEFAULT 기본항목 표시할지 여부
        EXTRA_RINGTONE_SHOW_SILENT 무음항목 표시 여부
        EXTRA_RINGTONE_TYPE 벨소리 유형을 지정 (벨소리, 공지, 알림 모두 )
        EXTRA_RINGTONE_DEFAULT_URI 벨소리 미리 재생
        EXTRA_RINGTONE_TITLE 벨소리 선택기에 표시할 제목
         */
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, alarm_sound?.toUri())
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)

        startActivityForResult(intent, 200)
        return true
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preference, rootKey)

        //좌측여백 없애기
        preferenceScreen.removeIconSpace()
        //알람 선택 intent 실행 리스너
        preferenceScreen.findPreference<Preference>("alarm_intent")?.setOnPreferenceClickListener {
            showRingtonePickerDialog()
        }

        //시간 설정 리스너
        preferenceScreen.findPreference<Preference>("study_time")?.setOnPreferenceClickListener {
            _showDialog("study_time", it.title.toString())
        }
        preferenceScreen.findPreference<Preference>("short_rest_time")
            ?.setOnPreferenceClickListener {
                _showDialog("short_rest_time", it.title.toString())
            }
        preferenceScreen.findPreference<Preference>("long_rest_time")
            ?.setOnPreferenceClickListener {
                _showDialog("long_rest_time", it.title.toString())
            }
        preferenceScreen.findPreference<Preference>("long_rest_pomo")
            ?.setOnPreferenceClickListener {
                _showDialog("long_rest_pomo", it.title.toString())
            }
    }

    fun _showDialog(key: String, title: String): Boolean {
        val TimeSettingBinding =
            SetGroupNameDialogBinding.inflate(LayoutInflater.from(context))
        val addDialog = AlertDialog.Builder(context)
            .setView(TimeSettingBinding.root)
            .create()

        with(TimeSettingBinding) {
            dialogGroupName.inputType = R.attr.number
            when(key){
                "study_time"->{
                    this.dialogGroupName.setText(PreferenceManager.getDefaultSharedPreferences(context)
                        .getInt(key, 25).toString())
                }
                "short_rest_time"->{
                    this.dialogGroupName.setText(PreferenceManager.getDefaultSharedPreferences(context)
                        .getInt(key, 5).toString())
                }
                "long_rest_time"->{
                    this.dialogGroupName.setText(PreferenceManager.getDefaultSharedPreferences(context)
                        .getInt(key, 20).toString())
                }
                else->{
                    this.dialogGroupName.setText(PreferenceManager.getDefaultSharedPreferences(context)
                        .getInt(key, 4).toString())
                }
            }
            this.dialogGroupName.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val number = s.toString()
                    if (number != "") {
                        if (key != "long_rest_pomo" && number.toInt() > 240) {
                            TimeSettingBinding.dialogGroupName.setText("240")
                        }
                        else if(key == "long_rest_pomo" && number.toInt() > 10){
                            TimeSettingBinding.dialogGroupName.setText("10")
                        }
                    }
                }
            })
            this.dialogTitle.setText(title)
            this.btCancle.setOnClickListener {
                addDialog.dismiss()
            }
            this.btOk.setOnClickListener {
                if (this.dialogGroupName.text.isNullOrEmpty()) {
                    Toast.makeText(context, "빈 칸을 채워주세요.", Toast.LENGTH_SHORT).show()
                } else {
                    val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
                    editor.putInt(key, this.dialogGroupName.text.toString().toInt())
                    editor.commit()
                    addDialog.dismiss()
                }
            }
        }
        addDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        addDialog.show()

        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //선택된 알람 소리를 shared preferences에 저장
        if (requestCode == 200) {
            val uri: Uri? = data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            if (uri != null) {
                val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
                editor.putString("alarm_sound", uri.toString())
                editor.commit()
            }
        }
    }
}