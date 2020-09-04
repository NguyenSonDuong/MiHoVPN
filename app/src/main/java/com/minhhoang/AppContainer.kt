package com.minhhoang

import android.app.NotificationManager
import android.content.Context
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentActivity
import androidx.room.Room
import kotlinx.coroutines.CompletableDeferred
import com.minhhoang.db.AppDatabase
import com.minhhoang.logging.BugReporter
import com.minhhoang.ui.TermsViewModel
import com.minhhoang.service.core.DeferredNode
import com.minhhoang.service.core.MysteriumCoreService
import com.minhhoang.service.core.NodeRepository
import com.minhhoang.ui.AccountViewModel
import com.minhhoang.ui.ProposalsViewModel
import com.minhhoang.ui.SharedViewModel

class AppContainer {
    lateinit var appDatabase: AppDatabase
    lateinit var nodeRepository: NodeRepository
    lateinit var sharedViewModel: SharedViewModel
    lateinit var proposalsViewModel: ProposalsViewModel
    lateinit var termsViewModel: TermsViewModel
    lateinit var accountViewModel: AccountViewModel
    lateinit var bugReporter: BugReporter
    lateinit var deferredMysteriumCoreService: CompletableDeferred<MysteriumCoreService>
    lateinit var drawerLayout: DrawerLayout
    lateinit var appNotificationManager: AppNotificationManager

    fun init(
            ctx: Context,
            deferredNode: DeferredNode,
            mysteriumCoreService: CompletableDeferred<MysteriumCoreService>,
            appDrawerLayout: DrawerLayout,
            notificationManager: NotificationManager
    ) {
        appDatabase = Room.databaseBuilder(
                ctx,
                AppDatabase::class.java, "MiHo VPN Free"
        ).build()

        drawerLayout = appDrawerLayout
        deferredMysteriumCoreService = mysteriumCoreService
        bugReporter = BugReporter()
        nodeRepository = NodeRepository(deferredNode)
        appNotificationManager = AppNotificationManager(notificationManager, deferredMysteriumCoreService)
        accountViewModel = AccountViewModel(nodeRepository, bugReporter)
        sharedViewModel = SharedViewModel(nodeRepository, deferredMysteriumCoreService, appNotificationManager, accountViewModel)
        proposalsViewModel = ProposalsViewModel(sharedViewModel, nodeRepository, appDatabase)
        termsViewModel = TermsViewModel(appDatabase)
    }

    companion object {
        fun from(activity: FragmentActivity?): AppContainer {
            return (activity!!.application as MainApplication).appContainer
        }
    }
}
