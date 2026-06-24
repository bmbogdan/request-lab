@file:OptIn(ExperimentalMaterial3Api::class)

package eu.mihaibadea.requestlab.feature.docs.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.mihaibadea.requestlab.R
import eu.mihaibadea.requestlab.core.designsystem.theme.AppTheme
import eu.mihaibadea.requestlab.core.designsystem.theme.spacing
import eu.mihaibadea.requestlab.feature.docs.domain.model.Article
import eu.mihaibadea.requestlab.feature.docs.domain.model.ArticleSummary
import eu.mihaibadea.requestlab.feature.docs.domain.model.DocCategory

// ─── Index screen ────────────────────────────────────────────────────────────────

@Composable
fun DocsScreen(
    onNavigateToArticle: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DocsIndexViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DocsIndexContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onArticleClicked = onNavigateToArticle,
        modifier = modifier,
    )
}

@Composable
fun DocsIndexContent(
    uiState: DocsIndexUiState,
    onEvent: (DocsIndexEvent) -> Unit,
    onArticleClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(title = { Text(stringResource(R.string.docs_title)) })
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            DockedSearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = uiState.query,
                        onQueryChange = { onEvent(DocsIndexEvent.OnQueryChanged(it)) },
                        onSearch = {},
                        expanded = false,
                        onExpandedChange = {},
                        placeholder = { Text(stringResource(R.string.docs_search_hint)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    )
                },
                expanded = false,
                onExpandedChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
            ) {}

            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                uiState.error != null -> DocsErrorState(
                    message = uiState.error,
                    onRetry = { onEvent(DocsIndexEvent.OnRetry) },
                    modifier = Modifier.fillMaxSize(),
                )
                uiState.filtered.isEmpty() -> DocsEmptyState(modifier = Modifier.fillMaxSize())
                else -> LazyColumn(
                    contentPadding = PaddingValues(
                        start = MaterialTheme.spacing.md,
                        end = MaterialTheme.spacing.md,
                        bottom = MaterialTheme.spacing.md,
                    ),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                ) {
                    uiState.filtered.forEach { (category, articles) ->
                        item(key = "header_${category.name}") {
                            CategoryHeader(category = category)
                        }
                        items(articles, key = { it.id }) { article ->
                            ArticleRow(
                                summary = article,
                                onClick = { onArticleClicked(article.id) },
                            )
                        }
                        item(key = "spacer_${category.name}") {
                            Spacer(Modifier.height(MaterialTheme.spacing.sm))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryHeader(category: DocCategory, modifier: Modifier = Modifier) {
    val label = when (category) {
        DocCategory.METHODS -> stringResource(R.string.docs_category_methods)
        DocCategory.HEADERS -> stringResource(R.string.docs_category_headers)
        DocCategory.STATUS_CODES -> stringResource(R.string.docs_category_status_codes)
        DocCategory.AUTH -> stringResource(R.string.docs_category_auth)
        DocCategory.PARAMS -> stringResource(R.string.docs_category_params)
    }
    Column(modifier = modifier.padding(top = MaterialTheme.spacing.md)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = MaterialTheme.spacing.xs),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

@Composable
private fun ArticleRow(
    summary: ArticleSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = MaterialTheme.spacing.xs),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = summary.title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DocsEmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
            modifier = Modifier.padding(MaterialTheme.spacing.xl),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = stringResource(R.string.docs_empty_search),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DocsErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = message, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(MaterialTheme.spacing.sm))
            TextButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
        }
    }
}

// ─── Article screen ──────────────────────────────────────────────────────────────

@Composable
fun DocsArticleScreen(
    articleId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DocsArticleViewModel = hiltViewModel(),
) {
    LaunchedEffect(articleId) { viewModel.init(articleId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DocsArticleContent(uiState = uiState, onBack = onBack, modifier = modifier)
}

@Composable
fun DocsArticleContent(
    uiState: DocsArticleUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = (uiState as? DocsArticleUiState.Content)?.article?.summary?.title
        ?: stringResource(R.string.docs_article_title)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_navigate_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when (uiState) {
            DocsArticleUiState.Loading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            is DocsArticleUiState.Error -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = uiState.message, color = MaterialTheme.colorScheme.error)
            }

            is DocsArticleUiState.Content -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    horizontal = MaterialTheme.spacing.md,
                    vertical = MaterialTheme.spacing.md,
                ),
            ) {
                item {
                    MarkdownContent(
                        markdown = uiState.article.markdown,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

// ─── Previews ────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Docs index — loaded")
@Composable
private fun DocsIndexContent_Loaded() {
    AppTheme {
        DocsIndexContent(
            uiState = DocsIndexUiState(
                isLoading = false,
                grouped = mapOf(
                    DocCategory.METHODS to listOf(
                        ArticleSummary("http-methods", "HTTP Methods", DocCategory.METHODS, "http-methods.md"),
                    ),
                    DocCategory.HEADERS to listOf(
                        ArticleSummary("request-headers", "Request Headers", DocCategory.HEADERS, "request-headers.md"),
                        ArticleSummary("response-headers", "Response Headers", DocCategory.HEADERS, "response-headers.md"),
                    ),
                ),
            ),
            onEvent = {},
            onArticleClicked = {},
        )
    }
}

@Preview(showBackground = true, name = "Docs article — content")
@Composable
private fun DocsArticleContent_Content() {
    AppTheme {
        val summary = ArticleSummary("http-methods", "HTTP Methods", DocCategory.METHODS, "http-methods.md")
        DocsArticleContent(
            uiState = DocsArticleUiState.Content(
                Article(
                    summary = summary,
                    markdown = """
                        # HTTP Methods

                        HTTP defines a set of request methods.

                        ## GET

                        Retrieves a resource. Should have **no side effects**.

                        - Use for: fetching data
                        - Body: not recommended

                        ```
                        GET /users/42 HTTP/1.1
                        Host: api.example.com
                        ```
                    """.trimIndent(),
                ),
            ),
            onBack = {},
        )
    }
}
