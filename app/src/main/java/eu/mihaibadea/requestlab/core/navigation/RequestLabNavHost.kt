package eu.mihaibadea.requestlab.core.navigation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import eu.mihaibadea.requestlab.R
import eu.mihaibadea.requestlab.core.designsystem.components.dotGridBackground
import eu.mihaibadea.requestlab.feature.builder.ui.BuilderScreen
import eu.mihaibadea.requestlab.feature.collections.ui.CollectionsScreen
import eu.mihaibadea.requestlab.feature.docs.ui.DocsScreen
import eu.mihaibadea.requestlab.feature.history.ui.HistoryScreen

private enum class TopTab(
    val label: String,
    @DrawableRes val iconRes: Int,
    val destination: NavKey,
) {
    BUILDER("Builder", R.drawable.ic_nav_builder, BuilderDestination),
    HISTORY("History", R.drawable.ic_nav_history, HistoryDestination),
    COLLECTIONS("Collections", R.drawable.ic_nav_collections, CollectionsDestination),
    DOCS("Docs", R.drawable.ic_nav_docs, DocsDestination),
}

private fun NavKey.topTab(): TopTab? = when (this) {
    is BuilderDestination, is EnvironmentsDestination,
    is EnvironmentDetailDestination, is SettingsDestination -> TopTab.BUILDER
    is HistoryDestination, is HistoryDetailDestination -> TopTab.HISTORY
    is CollectionsDestination, is CollectionDetailDestination -> TopTab.COLLECTIONS
    is DocsDestination, is DocsArticleDestination -> TopTab.DOCS
    else -> null
}

@Composable
fun RequestLabNavHost() {
    val backStack = rememberNavBackStack(BuilderDestination)
    val currentTab = backStack.lastOrNull()?.topTab() ?: TopTab.BUILDER

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            TopTab.entries.forEach { tab ->
                item(
                    icon = {
                        Icon(
                            painter = painterResource(tab.iconRes),
                            contentDescription = null,
                        )
                    },
                    label = { Text(tab.label) },
                    selected = currentTab == tab,
                    onClick = {
                        if (currentTab != tab) {
                            backStack.clear()
                            backStack.add(tab.destination)
                        }
                    },
                )
            }
        },
    ) {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier
                .fillMaxSize()
                .dotGridBackground(),
            entryProvider = entryProvider {
                entry<BuilderDestination> {
                    BuilderScreen(
                        onNavigateToEnvironments = { backStack.add(EnvironmentsDestination) },
                        onNavigateToSettings = { backStack.add(SettingsDestination) },
                    )
                }
                entry<HistoryDestination> {
                    HistoryScreen(
                        onNavigateToDetail = { id -> backStack.add(HistoryDetailDestination(id)) },
                    )
                }
                entry<HistoryDetailDestination> { dest ->
                    eu.mihaibadea.requestlab.feature.history.ui.HistoryDetailScreen(
                        entryId = dest.id,
                        onReplay = { backStack.clear(); backStack.add(BuilderDestination) },
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
                entry<CollectionsDestination> {
                    CollectionsScreen(
                        onNavigateToDetail = { id -> backStack.add(CollectionDetailDestination(id)) },
                    )
                }
                entry<CollectionDetailDestination> { dest ->
                    eu.mihaibadea.requestlab.feature.collections.ui.CollectionDetailScreen(
                        collectionId = dest.id,
                        onOpenRequest = { backStack.clear(); backStack.add(BuilderDestination) },
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
                entry<EnvironmentsDestination> {
                    eu.mihaibadea.requestlab.feature.environments.ui.EnvironmentsScreen(
                        onNavigateToDetail = { id -> backStack.add(EnvironmentDetailDestination(id)) },
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
                entry<EnvironmentDetailDestination> { dest ->
                    eu.mihaibadea.requestlab.feature.environments.ui.EnvironmentDetailScreen(
                        environmentId = dest.id,
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
                entry<SettingsDestination> {
                    eu.mihaibadea.requestlab.feature.settings.ui.SettingsScreen(
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
                entry<DocsDestination> {
                    DocsScreen(
                        onNavigateToArticle = { id -> backStack.add(DocsArticleDestination(id)) },
                    )
                }
                entry<DocsArticleDestination> { dest ->
                    eu.mihaibadea.requestlab.feature.docs.ui.DocsArticleScreen(
                        articleId = dest.id,
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
            },
        )
    }
}
