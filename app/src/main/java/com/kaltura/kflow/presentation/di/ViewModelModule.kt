package com.kaltura.kflow.presentation.di

import com.kaltura.kflow.presentation.anonymousLogin.AnonymousLoginViewModel
import com.kaltura.kflow.presentation.appToken.AppTokenViewModel
import com.kaltura.kflow.presentation.aws.IotViewModel
import com.kaltura.kflow.presentation.bookmark.BookmarkViewModel
import com.kaltura.kflow.presentation.checkReceipt.CheckReceiptViewModel
import com.kaltura.kflow.presentation.collections.GetCollectionsViewModel
import com.kaltura.kflow.presentation.continueWatching.ContinueWatchingViewModel
import com.kaltura.kflow.presentation.deviceManagement.DeviceManagementViewModel
import com.kaltura.kflow.presentation.epg.EpgViewModel
import com.kaltura.kflow.presentation.favorites.FavoritesViewModel
import com.kaltura.kflow.presentation.ks.KsViewModel
import com.kaltura.kflow.presentation.liveTv.LiveTvViewModel
import com.kaltura.kflow.presentation.login.LoginViewModel
import com.kaltura.kflow.presentation.mediaPage.MediaPageViewModel
import com.kaltura.kflow.presentation.player.PlayerViewModel
import com.kaltura.kflow.presentation.productPrice.ProductPriceViewModel
import com.kaltura.kflow.presentation.recordings.RecordingsViewModel
import com.kaltura.kflow.presentation.registration.RegistrationViewModel
import com.kaltura.kflow.presentation.search.SearchViewModel
import com.kaltura.kflow.presentation.settings.SettingsViewModel
import com.kaltura.kflow.presentation.subscription.SubscriptionViewModel
import com.kaltura.kflow.presentation.transactionHistory.TransactionHistoryViewModel
import com.kaltura.kflow.presentation.vod.GetVodViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Created by alex_lytvynenko on 12.02.2020.
 */
val viewModelModule = module {
    viewModel { AnonymousLoginViewModel(get(), get()) }
    viewModel { CheckReceiptViewModel(get()) }
    viewModel { EpgViewModel(get(), get(), get()) }
    viewModel { FavoritesViewModel(get()) }
    viewModel { LiveTvViewModel(get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { AppTokenViewModel(get(), get()) }
    viewModel { KsViewModel(get(), get()) }
    viewModel { MediaPageViewModel(get()) }
    viewModel { PlayerViewModel(get(), get()) }
    viewModel { ProductPriceViewModel(get()) }
    viewModel { RecordingsViewModel(get()) }
    viewModel { RegistrationViewModel(get(), get()) }
    viewModel { SearchViewModel(get()) }
    viewModel { SettingsViewModel(get(), get()) }
    viewModel { SubscriptionViewModel(get()) }
    viewModel { TransactionHistoryViewModel(get()) }
    viewModel { GetVodViewModel(get(), get()) }
    viewModel { ContinueWatchingViewModel(get()) }
    viewModel { GetCollectionsViewModel(get()) }
    viewModel { BookmarkViewModel(get()) }
    viewModel { IotViewModel(get(), get(), get()) }
    viewModel { DeviceManagementViewModel(get()) }
}