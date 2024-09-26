package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment.upg.data

import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment.upg.domain.UPGItem
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment.upg.domain.UPGRepository
import org.hisp.dhis.android.core.D2

private const val upgProgram = "lu4r3oPCg6v"
private const val upgStatusDE = "bCGs7tqWUdd"
private const val upgNameDE = "rn2YpLCqksT"

class UPGD2Repository(
    private val d2: D2,
) : UPGRepository {

    override fun get(orgUnitUid: String): List<UPGItem> {
        val countryUid = getCountry(orgUnitUid)

        //https://cpr-test.samaritanspurse.org/api/trackedEntityInstances.json?totalPages=true&program=lu4r3oPCg6v&ou=D6eqt1FJ7e8&ouMode=SELECTED&fields=trackedEntityInstance,attributes,enrollments&filter=bCGs7tqWUdd:eq:eligible&pageSize=1000000&skipPaging=false
        val teis = d2.trackedEntityModule().trackedEntityInstances()
            .withTrackedEntityAttributeValues()
            .byProgramUids(listOf(upgProgram))
            .byOrganisationUnitUid().eq(countryUid)
            .get()
            .blockingGet()

        return teis.asSequence()
            .filter { tei ->
                tei.trackedEntityAttributeValues()?.any { attribute ->
                    attribute.trackedEntityAttribute() == upgStatusDE &&
                            attribute.value()?.lowercase() == "eligible"
                } ?: false
            }
            .mapNotNull { tei ->
                val upgName = tei.trackedEntityAttributeValues()
                    ?.find { attribute -> attribute.trackedEntityAttribute() == upgNameDE }
                    ?.value()

                if (upgName.isNullOrEmpty()) null
                else UPGItem(tei.uid(), upgName)
            }
            .sortedBy { it.name }
            .toList()
    }

    private fun getCountry(orgUnitUid: String): String {
        val orgUnit = d2.organisationUnitModule().organisationUnits().byUid().eq(orgUnitUid).one()
            .blockingGet()

        val countryUid = orgUnit?.path()?.split("/")?.get(4) ?: ""

        return countryUid
    }
}

