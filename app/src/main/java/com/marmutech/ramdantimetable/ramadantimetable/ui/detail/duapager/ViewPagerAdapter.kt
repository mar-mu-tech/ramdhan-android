package com.marmutech.ramdantimetable.ramadantimetable.ui.detail.duapager

import com.marmutech.ramdantimetable.ramadantimetable.model.TimeTableDay

class ViewPagerAdapter(internal var fragManager: androidx.fragment.app.FragmentManager, internal var mtitles: Array<String>, timeTableDay: TimeTableDay) : androidx.fragment.app.FragmentPagerAdapter(fragManager) {
    internal var ft: androidx.fragment.app.FragmentTransaction
    internal lateinit var _timetableDay: TimeTableDay

    init {
        this.ft = fragManager.beginTransaction()
        this._timetableDay = timeTableDay
    }

    override fun getItem(position: Int): androidx.fragment.app.Fragment? {
        // getItem is called to instantiate the fragment for the given page.
        when (position) {
            0 -> return DuaInfoFragment.newInstance("mm", _timetableDay.duaMmUni)
            1 -> return DuaInfoFragment.newInstance("en", _timetableDay.duaEn)
            2 -> return DuaInfoFragment.newInstance("ar", _timetableDay.duaAr)
        }
        return null

    }

    override fun getCount(): Int {
        return mtitles.size
    }

    override fun getPageTitle(position: Int): CharSequence? {

        return mtitles[position]
    }
}
