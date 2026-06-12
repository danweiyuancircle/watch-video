package com.watchvideo

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.watchvideo.ui.search.SearchScreen
import com.watchvideo.ui.detail.DetailScreen
import io.ktor.http.encodeURLParameter
import io.ktor.http.decodeURLPart

@Composable
fun App() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "search") {
        composable("search") {
            SearchScreen(
                onResultClick = { result ->
                    val encodedTitle = result.title.encodeURLParameter()
                    navController.navigate("detail/${result.siteKey}/${result.id}/$encodedTitle")
                }
            )
        }
        composable(
            route = "detail/{siteKey}/{id}/{title}",
            arguments = listOf(
                navArgument("siteKey") { type = NavType.StringType },
                navArgument("id") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val siteKey = backStackEntry.arguments?.getString("siteKey") ?: return@composable
            val id = backStackEntry.arguments?.getString("id") ?: return@composable
            val title = (backStackEntry.arguments?.getString("title") ?: "").decodeURLPart()
            DetailScreen(
                siteKey = siteKey,
                vodId = id,
                title = title,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
