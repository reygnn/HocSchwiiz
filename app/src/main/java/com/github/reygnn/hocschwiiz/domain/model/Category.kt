package com.github.reygnn.hocschwiiz.domain.model

enum class Category(
    val displayNameDe: String,
    val displayNameVi: String,
    val icon: String
) {
    GREETINGS("Begrüssungen", "Lời chào", "waving_hand"),
    FOOD_DRINK("Essen & Trinken", "Ăn uống", "restaurant"),
    DAILY_LIFE("Alltag", "Cuộc sống", "home"),
    WORK("Arbeit", "Công việc", "work"),
    NUMBERS_TIME("Zahlen & Zeit", "Số & Thời gian", "schedule"),
    NATURE("Natur & Wetter", "Thiên nhiên", "park"),
    FAMILY("Familie", "Gia đình", "family_restroom"),
    TRANSPORT("Verkehr", "Giao thông", "directions_car"),
    EXPRESSIONS("Redewendungen", "Thành ngữ", "chat_bubble"),
    SWEAR_WORDS("Fluchen", "Chửi thề", "sentiment_very_dissatisfied")
}