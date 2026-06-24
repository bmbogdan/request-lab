package eu.mihaibadea.requestlab.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Top-level tab destinations
@Serializable data object BuilderDestination : NavKey
@Serializable data object HistoryDestination : NavKey
@Serializable data object CollectionsDestination : NavKey
@Serializable data object DocsDestination : NavKey

// Builder sub-screens
@Serializable data object EnvironmentsDestination : NavKey
@Serializable data class EnvironmentDetailDestination(val id: String) : NavKey
@Serializable data object SettingsDestination : NavKey

// History sub-screens
@Serializable data class HistoryDetailDestination(val id: String) : NavKey

// Collections sub-screens
@Serializable data class CollectionDetailDestination(val id: String) : NavKey

// Docs sub-screens
@Serializable data class DocsArticleDestination(val id: String) : NavKey
