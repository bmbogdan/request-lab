package eu.mihaibadea.requestlab.feature.builder.domain.usecase

import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.core.common.map
import eu.mihaibadea.requestlab.feature.builder.domain.RequestDraftRepository
import eu.mihaibadea.requestlab.feature.builder.domain.model.DraftSource
import eu.mihaibadea.requestlab.feature.builder.domain.model.RequestDraft
import eu.mihaibadea.requestlab.feature.builder.domain.model.emptyDraft
import eu.mihaibadea.requestlab.feature.collections.domain.CollectionsRepository
import eu.mihaibadea.requestlab.feature.history.domain.HistoryRepository
import java.util.UUID
import javax.inject.Inject

class LoadDraftUseCase @Inject constructor(
    private val draftRepository: RequestDraftRepository,
    private val historyRepository: HistoryRepository,
    private val collectionsRepository: CollectionsRepository,
) {
    suspend operator fun invoke(source: DraftSource): AppResult<RequestDraft> =
        when (source) {
            is DraftSource.New -> draftRepository.loadDraft(source)
            is DraftSource.FromHistory -> historyRepository.getDetail(source.id).map { detail ->
                RequestDraft(
                    id = UUID.randomUUID().toString(),
                    method = detail.request.method,
                    url = detail.request.resolvedUrl,
                    headers = detail.request.headers,
                    params = detail.request.params,
                    body = detail.request.body,
                    auth = detail.request.auth,
                    sourceSavedRequestId = null,
                    isDirty = true,
                )
            }
            is DraftSource.FromSavedRequest -> collectionsRepository.getRequestDetail(source.id).map { detail ->
                RequestDraft(
                    id = UUID.randomUUID().toString(),
                    method = detail.savedRequest.method,
                    url = detail.savedRequest.url,
                    headers = detail.headers,
                    params = detail.params,
                    body = detail.body,
                    auth = detail.auth,
                    sourceSavedRequestId = detail.savedRequest.id,
                    isDirty = false,
                )
            }
            is DraftSource.Duplicate -> {
                val fromHistory = historyRepository.getDetail(source.historyOrSavedId)
                if (fromHistory is AppResult.Success) {
                    fromHistory.map { detail ->
                        RequestDraft(
                            id = UUID.randomUUID().toString(),
                            method = detail.request.method,
                            url = detail.request.resolvedUrl,
                            headers = detail.request.headers,
                            params = detail.request.params,
                            body = detail.request.body,
                            auth = detail.request.auth,
                            sourceSavedRequestId = null,
                            isDirty = true,
                        )
                    }
                } else {
                    collectionsRepository.getRequestDetail(source.historyOrSavedId).map { detail ->
                        RequestDraft(
                            id = UUID.randomUUID().toString(),
                            method = detail.savedRequest.method,
                            url = detail.savedRequest.url,
                            headers = detail.headers,
                            params = detail.params,
                            body = detail.body,
                            auth = detail.auth,
                            sourceSavedRequestId = null,
                            isDirty = true,
                        )
                    }
                }
            }
        }
}
