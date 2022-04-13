package com.alignTech.labelsPrinting.di.modules

import com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.firstView.auth.repo.AuthRepository
import com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.firstView.auth.repo.AuthRepositoryImp
import com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.repo.LabelsPrintingRepository
import com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.repo.LabelsPrintingRepositoryImp
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {

    @Provides
    fun providesAuthRepository(repo: AuthRepositoryImp): AuthRepository {
        return repo
    }

    @Provides
    fun providesLabelsPrintingRepository(repo: LabelsPrintingRepositoryImp): LabelsPrintingRepository {
        return repo
    }


}