package com.daily.new_amime.for_my.networking.daily_anime

data class DailyDto(
    val avod_status: String,
    val content_rating: String,
    val cropped_img: String,
    val distributed_air_time: String,
    val distributed_air_time_sequence: Int,
    val distributed_air_times: List<String>,
    val genres: List<String>,
    val highlight_video: HighlightVideo?,
    val id: Int,
    val images: List<Image>,
    val img: String,
    val is_adult: Boolean,
    val is_avod: Boolean,
    val is_dubbed: Boolean,
    val is_episode_existed: Boolean,
    val is_expired: Boolean,
    val is_laftel_only: Boolean,
    val is_uncensored: Boolean,
    val is_viewing: Boolean,
    val latest_episode_created: String?,
    val latest_published_datetime: String?,
    val medium: String,
    val name: String,
    val rating: Int,
    val rating_type: String
)