package org.dhis2.usescases.datasets.datasetInitial;

import org.dhis2.commons.di.dagger.PerActivity;
import org.dhis2.commons.schedulers.SchedulerProvider;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by ppajuelo on 24/09/2018.
 */
@Module
public class DataSetInitialModule {
    private final String dataSetUid;
    private final DataSetInitialContract.View view;

    DataSetInitialModule(DataSetInitialContract.View view, String dataSetUid) {
        this.view = view;
        this.dataSetUid = dataSetUid;
    }

    @Provides
    @PerActivity
    DataSetInitialContract.View provideView(DataSetInitialActivity activity) {
        return activity;
    }

    @Provides
    @PerActivity
    DataSetInitialContract.Presenter providesPresenter(D2 d2, DataSetInitialRepository dataSetInitialRepository, SchedulerProvider schedulerProvider) {
        return new DataSetInitialPresenter(view, d2, dataSetInitialRepository, schedulerProvider);
    }

    @Provides
    @PerActivity
    DataSetInitialRepository dataSetInitialRepository(D2 d2) {
        return new DataSetInitialRepositoryImpl(d2, dataSetUid);
    }
}
