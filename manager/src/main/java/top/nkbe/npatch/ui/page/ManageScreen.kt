package top.nkbe.npatch.ui.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultRecipient
import kotlinx.coroutines.launch
import top.nkbe.npatch.R
import top.nkbe.npatch.ui.component.CenterTopBar
import top.nkbe.npatch.ui.util.bouncyClickable
import com.ramcosta.composedestinations.generated.destinations.SelectAppsScreenDestination
import top.nkbe.npatch.ui.page.manage.AppManageBody
import top.nkbe.npatch.ui.page.manage.AppManageFab
import top.nkbe.npatch.ui.page.manage.ModuleManageBody

import androidx.compose.ui.input.nestedscroll.nestedScroll

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Destination<RootGraph>
@Composable
fun ManageScreen(
    navigator: DestinationsNavigator,
    resultRecipient: ResultRecipient<SelectAppsScreenDestination, SelectAppsResult>
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState()
    val tabTitles = listOf(stringResource(R.string.apps), stringResource(R.string.modules))
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { CenterTopBar(stringResource(BottomBarDestination.Manage.label), scrollBehavior = scrollBehavior) },
        floatingActionButton = {
            if (pagerState.currentPage == 0) AppManageFab(navigator)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                divider = {},
                indicator = { tabPositions ->
                    Box(
                        Modifier
                            .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                            .fillMaxHeight()
                            .padding(vertical = 6.dp, horizontal = 12.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(100.dp)
                            )
                    )
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(100.dp)
                    )
            ) {
                tabTitles.forEachIndexed { index, title ->
                    val selected = pagerState.currentPage == index
                    Tab(
                        selected = selected,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (selected) FontWeight.Black else FontWeight.Medium,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .bouncyClickable {
                                scope.launch { pagerState.animateScrollToPage(index) }
                            }
                    )
                }
            }

            HorizontalPager(
                count = tabTitles.size,
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> AppManageBody(navigator, resultRecipient)
                    1 -> ModuleManageBody()
                }
            }
        }
    }
}
