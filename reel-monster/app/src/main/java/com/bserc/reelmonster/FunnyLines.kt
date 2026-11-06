package com.bserc.reelmonster

/** Funny lines the monster says as the count rises. Rotates with the count. */
object FunnyLines {

    private val lines = listOf(
        "Yum! Your evening tastes great.",
        "Burp. Your homework says hi.",
        "One more? The monster is still hungry.",
        "That reel was 30 seconds you'll never get back.",
        "Your thumb is getting a workout.",
        "The monster grows. Your free time shrinks.",
        "Just one more… that's what you said 10 reels ago.",
        "Reel fuel: delicious. Productivity: zero.",
        "Your screen time called. It wants a raise.",
        "The monster is full. Are you?",
        "Somewhere, a textbook is crying.",
        "Feed me reels. I feed on your evening."
    )

    private val milestones = mapOf(
        10 to "10 reels! The monster just grew a little.",
        25 to "25 reels! The monster is now officially chubby.",
        50 to "50 reels! The monster needs a bigger screen.",
        100 to "100 reels! The monster has eaten your whole evening."
    )

    fun forCount(count: Int): String {
        milestones[count]?.let { return it }
        return lines[count % lines.size]
    }
}
