package com.msc24x.configkit

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*
import kotlinx.android.synthetic.main.settings_activity.*
import java.io.File

//LIST OF ALL SETTINGS AND INDICES TO BE ADDED RUNTIME
var options = arrayOf<String>("UserQualitySetting", "MobileContentScaleFactor", "DefaultFeature.AntiAliasing", "UserMSAASetting" ,"MaterialQualityLevel", "MobileMSAA", "MSAACount", "UserShadowSwitch" ,"ShadowQuality")
//LIST OF SETTINGS NOT FOUND
var exceptionsIndex  = arrayOf<String>()
//STORING DEFAULT VALUES
var def_values = arrayOf<String>()
//NEW VALS
var new_values = arrayOf<String>()
//OPENING FILE
var file = File("/storage/emulated/0/UserCustom-2.ini").inputStream().bufferedReader().use { it.readLines() }
    .toMutableList()

class Settings : AppCompatActivity() {




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)



        //KEY TO DECRYPT
        val key = "01111001".toInt(2)
        var encrypted : List<String>
        var decrypted : String = ""

        //DECRYPTION OF ALL LINES
        for (I in file.indices){
            if( ! file[I].contains("+CVars=" )) continue
            encrypted = file[I].replace("+CVars=", "", ignoreCase = false).chunked(2)
            for (i in encrypted.indices)
                decrypted += (encrypted[i].toInt(16).xor(key)).toChar()
            file[I] = "+CVars=$decrypted"
            decrypted = ""
        }
        //test.text = file.toString()

        //BACKUP TO REATORE EVERYTHING
        var backup_file = file

        var optionsindex:Int = 0
        var notFound : Boolean = false

        //ANALYSING FILE
        while ( optionsindex < 9) {
            //NOTE DOWN THE INDEX OF LINE IN FILE OF REQUIRED SETTING
            for (i in file.indices) {
                if (file[i].contains(options[optionsindex])) {
                    options += "$i"
                    optionsindex++
                    notFound = false
                    break
                }
                notFound = true
            }
            //ADDING EXCEPTION IF NOT FOUND
            if(notFound)
                exceptionsIndex += "$optionsindex"
        }
        //test.text = "${options.toList() + exceptionsIndex.toList()}"

        //TO NOTIFIY USER ABOUT EXCEPTION
        if(exceptionsIndex.size != 0 || options.size != 18)
            Toast.makeText(applicationContext, "Some settings missing!", Toast.LENGTH_LONG)



        //EXTRACTING THE VALUES OF SETTINGS AND STORING
        for (i in 9..17)
            def_values += file[options[i].toInt()].filter { it.isDigit() || it == '.' }
        for (i in 0..def_values.lastIndex)
            def_values[i] = def_values[i].replace("^(\\.)*".toRegex(),"")

        new_values= def_values

       // test.text = "${def_values.toList()}"


        val s : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val editing : SharedPreferences.Editor = s.edit()

        editing.putString(options[0], def_values[0])
        editing.putString(options[1],  def_values[1])
        editing.putString(options[2],  def_values[2])
        editing.putBoolean(options[3], ( def_values[3] == "1.0"))
        editing.putString(options[4],  def_values[4])
        editing.putBoolean(options[5],  def_values[5] == "1.0")
        editing.putString(options[6],  def_values[6])
        editing.putBoolean(options[7],  def_values[7] == "1.0")
        editing.putString(options[8],  def_values[8])
        editing.apply()

        save.setOnClickListener{
            decrypted =""

            for (I in file.indices){
                if( ! file[I].contains("+CVars=" )) continue
                encrypted = file[I].replace("+CVars=", "", ignoreCase = false).chunked(1)
                for (i in encrypted.indices)
                    decrypted += Integer.toHexString(Character.getNumericValue(encrypted[i].toCharArray()[0]).xor(key))
                file[I] = "+CVars=$decrypted"
                decrypted = ""
            }

            File("/storage/emulated/0/UserCustom-2.ini").writeText(file.joinToString(separator = "\n") { it -> it })

            /*for (I in file.indices){
                if( ! file[I].contains("+CVars=" )) continue
                encrypted = file[I].replace("+CVars=", "", ignoreCase = false).chunked(2)
                for (i in encrypted.indices)
                    decrypted += (encrypted[i].toInt(16).xor(key)).toChar()
                file[I] = "+CVars=$decrypted"
                decrypted = ""
            }*/
        }



    }

    /*
        * ShadowQuality
        * UserShadowSwitch
        * MSAACount
        * MobileMSAA
        * MaterialQualityLevel
        * MaterialQualityLevel
        * UserMSAASetting
        * DefaultFeature.AntiAliasing
        * MobileContentScaleFactor
        * UserQualitySetting
        */

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val quality = findPreference<ListPreference>("UserQualitySetting")
            quality?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference, newValue ->

                    if (quality.findIndexOfValue(newValue.toString()) != 0) {
                        findPreference<ListPreference>("MobileContentScaleFactor")?.isEnabled  = false
                        findPreference<ListPreference>("DefaultFeature.AntiAliasing")?.isEnabled  = false
                        findPreference<SwitchPreferenceCompat>("UserMSAASetting")?.isEnabled  = false
                        findPreference<ListPreference>("MaterialQualityLevel")?.isEnabled  = false
                        findPreference<SwitchPreferenceCompat>("MobileMSAA")?.isEnabled  = false
                        findPreference<SwitchPreferenceCompat>("UserShadowSwitch")?.isEnabled  = false
                    } else{
                        findPreference<ListPreference>("MobileContentScaleFactor")?.isEnabled  = true
                        findPreference<ListPreference>("DefaultFeature.AntiAliasing")?.isEnabled  = true
                        findPreference<SwitchPreferenceCompat>("UserMSAASetting")?.isEnabled  = true
                        findPreference<ListPreference>("MaterialQualityLevel")?.isEnabled  = true
                        findPreference<SwitchPreferenceCompat>("MobileMSAA")?.isEnabled  = true
                        findPreference<SwitchPreferenceCompat>("UserShadowSwitch")?.isEnabled  = true
                    }
                    new_values[0] = newValue.toString()
                    file[options[0+9].toInt()].replace(def_values[0], new_values[0])
                    def_values[0] = new_values[0]

                    true
                }
        }
    }
}