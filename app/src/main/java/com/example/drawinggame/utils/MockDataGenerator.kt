package com.example.drawinggame.utils

import android.content.Context
import com.example.drawinggame.R
import kotlin.random.Random

object MockDataGenerator {
    
    fun getDailyPrompt(context: Context): String {
        val prompts = context.resources.getStringArray(R.array.daily_prompts)
        return prompts[Random.nextInt(prompts.size)]
    }
    
    fun getRandomUserName(context: Context): String {
        val names = context.resources.getStringArray(R.array.sample_user_names)
        return names[Random.nextInt(names.size)]
    }
    
    fun getRandomUserBio(context: Context): String {
        val bios = context.resources.getStringArray(R.array.sample_user_bios)
        return bios[Random.nextInt(bios.size)]
    }
    
    fun getRandomDrawingTitle(context: Context): String {
        val titles = context.resources.getStringArray(R.array.sample_drawing_titles)
        return titles[Random.nextInt(titles.size)]
    }
    
    fun getRandomDrawingDescription(context: Context): String {
        val descriptions = context.resources.getStringArray(R.array.sample_drawing_descriptions)
        return descriptions[Random.nextInt(descriptions.size)]
    }
    
    fun getRandomLikeCount(): Int = Random.nextInt(1, 100)
    
    fun getRandomCommentCount(): Int = Random.nextInt(0, 25)
    
    fun getRandomTimeAgo(): String {
        val times = listOf(
            "2m ago", "15m ago", "1h ago", "3h ago", "1d ago", 
            "2d ago", "1w ago", "2w ago", "1mo ago"
        )
        return times[Random.nextInt(times.size)]
    }
    
    fun getDrawingTip(context: Context): String {
        val tips = context.resources.getStringArray(R.array.drawing_tips)
        return tips[Random.nextInt(tips.size)]
    }
    
    // Generate mock user stats
    data class UserStats(
        val drawingsCount: Int,
        val likesReceived: Int,
        val dayStreak: Int,
        val followersCount: Int
    )
    
    fun generateUserStats(): UserStats {
        return UserStats(
            drawingsCount = Random.nextInt(5, 150),
            likesReceived = Random.nextInt(10, 500),
            dayStreak = Random.nextInt(1, 30),
            followersCount = Random.nextInt(0, 100)
        )
    }
    
    // Generate mock gallery data
    data class MockArtwork(
        val title: String,
        val description: String,
        val authorName: String,
        val likesCount: Int,
        val commentsCount: Int,
        val timeAgo: String,
        val isLiked: Boolean = false,
        val promptBased: Boolean = Random.nextBoolean()
    )
    
    fun generateMockArtworks(context: Context, count: Int): List<MockArtwork> {
        return (1..count).map {
            MockArtwork(
                title = getRandomDrawingTitle(context),
                description = getRandomDrawingDescription(context),
                authorName = getRandomUserName(context),
                likesCount = getRandomLikeCount(),
                commentsCount = getRandomCommentCount(),
                timeAgo = getRandomTimeAgo(),
                isLiked = Random.nextBoolean(),
                promptBased = Random.nextBoolean()
            )
        }
    }
}