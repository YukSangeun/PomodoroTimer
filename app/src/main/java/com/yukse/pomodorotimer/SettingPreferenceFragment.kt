package com.yukse.pomodorotimer

import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.core.net.toUri
import androidx.preference.*

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
        val default_sound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
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
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)

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