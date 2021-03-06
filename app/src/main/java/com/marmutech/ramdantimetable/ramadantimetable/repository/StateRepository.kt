package com.marmutech.ramdantimetable.ramadantimetable.repository

import androidx.lifecycle.LiveData
import com.marmutech.ramdantimetable.ramadantimetable.AppExecutors
import com.marmutech.ramdantimetable.ramadantimetable.api.StateService
import com.marmutech.ramdantimetable.ramadantimetable.db.StateDao
import com.marmutech.ramdantimetable.ramadantimetable.db.offsetManager
import com.marmutech.ramdantimetable.ramadantimetable.model.State
import com.marmutech.ramdantimetable.ramadantimetable.model.StateResponse
import com.marmutech.ramdantimetable.ramadantimetable.vo.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StateRepository @Inject constructor(
        private val appExecutors: AppExecutors,
        private val stateDao: StateDao,
        private val stateSertice: StateService
) {
    fun loadStateList(countryId: String, limit: Int, page: Int): LiveData<Resource<List<State>>> {
        return object : NetworkBoundResource<List<State>, StateResponse>(appExecutors) {
            override fun shouldFetch(data: List<State>?): Boolean = data == null || data.isEmpty()

            var query = "{\n" +
                    "  states(limit: $limit, page: $page, countryId: \"$countryId\") {\n" +
                    "    data {\n" +
                    "      id\n" +
                    "      objectId\n" +
                    "      nameMmUni\n" +
                    "      nameMmZawgyi\n" +
                    "      countryId\n" +
                    "      createdDate\n" +
                    "      updatedDate\n" +
                    "    }\n" +
                    "  }\n" +
                    "}"

            override fun createCall() = stateSertice.getStateList(query)

            override fun loadFromDb(): LiveData<List<State>> {

                return stateDao.getStateByCountryId(countryId, limit, offsetManager(limit, page))

            }

            override fun saveCallResult(item: StateResponse) {
                stateDao.bulkInsert(item.data.states.data)
            }


        }.asLiveData()
    }

    fun loadState(stateId: String): LiveData<Resource<State>> {
        return object : NetworkBoundResource<State, StateResponse>(appExecutors) {
            override fun shouldFetch(data: State?): Boolean = data == null

            var query = "{\n" +
                    "  state(stateId: \"$stateId\") {\n" +
                    "    id\n" +
                    "    objectId\n" +
                    "    nameMmUni\n" +
                    "    nameMmZawgyi\n" +
                    "    countryId\n" +
                    "    createdDate\n" +
                    "    updatedDate\n" +
                    "  }\n" +
                    "}"

            override fun createCall() = stateSertice.getState(query)

            override fun loadFromDb(): LiveData<State> {

                return stateDao.getStateById(stateId)

            }

            override fun saveCallResult(item: StateResponse) {
                stateDao.insert(item.data.state)
            }


        }.asLiveData()
    }
}
