package com.marmutech.ramdantimetable.ramadantimetable.ui.setting

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.marmutech.ramdantimetable.ramadantimetable.R
import com.marmutech.ramdantimetable.ramadantimetable.R.id.tool_bar_setting
import com.marmutech.ramdantimetable.ramadantimetable.ui.splash.CountryStateSelectionFragment
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.activity_setting.*
import javax.inject.Inject

class SettingActivity : AppCompatActivity(), HasSupportFragmentInjector {
    private val TAG_COUNTRY_SELECT_FRAG="tag_country"
    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return dispatchingAndroidInjector
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        setSupportActionBar(tool_bar_setting)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            this.supportFragmentManager.beginTransaction()
                    .replace(R.id.fl_setting_container, SettingFragment())
                    .commitAllowingStateLoss()
        }
    }

    fun showLocationSelectFrag(){
        this.supportFragmentManager.beginTransaction()
                .replace(R.id.fl_setting_container, CountryStateSelectionFragment())
                .addToBackStack(TAG_COUNTRY_SELECT_FRAG)
                .commit()
    }
}