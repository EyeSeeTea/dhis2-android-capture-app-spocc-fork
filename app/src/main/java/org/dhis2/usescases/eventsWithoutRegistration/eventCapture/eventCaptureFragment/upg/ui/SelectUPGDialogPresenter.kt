package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment.upg.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment.upg.domain.GetUPGItems
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment.upg.domain.UPGItem

class SelectUPGDialogPresenter(
    private val getUPGItems: GetUPGItems
) : CoroutineScope by MainScope() {
    private var view: SelectUPGDialogView? = null

    fun init(
        view: SelectUPGDialogView,
        orgUnitUid: String
    ) {
        this.view = view

        loadData(orgUnitUid)
    }

    private fun loadData(orgUnitUid: String)= launch()  {
        view?.showLoading()

        val items  = withContext(Dispatchers.IO) {  getUPGItems(orgUnitUid)}


        view?.renderList(items)

        view?.hideLoading()
    }

    fun onDetach() {
        this.view = null
        cancel()

    }

    fun onItemClick(item: UPGItem) {
        view?.onUPGSelected(item)
    }
}
