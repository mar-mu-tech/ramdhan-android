package com.marmutech.ramdantimetable.ramadantimetable.di

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.marmutech.ramdantimetable.ramadantimetable.ui.detail.DetailViewModel
import com.marmutech.ramdantimetable.ramadantimetable.ui.schedule.ScheduleViewModel
import com.marmutech.ramdantimetable.ramadantimetable.ui.splash.SplashViewModel
import com.marmutech.ramdantimetable.ramadantimetable.viewmodel.RamdanTimeTableViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap


@Suppress("unused")
@Module
abstract class ViewModelModule {
    //TODO  Declare View Models According to your needs


    @Binds
    @IntoMap
    @ViewModelKey(SplashViewModel::class)
    abstract fun bindSplashViewModel(detailViewModel: SplashViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DetailViewModel::class)
    abstract fun bindDetailViewModel(detailViewModel: DetailViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ScheduleViewModel::class)
    abstract fun bindScheduleListViewModel(scheduleViewModel: ScheduleViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: RamdanTimeTableViewModelFactory): ViewModelProvider.Factory
}