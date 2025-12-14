package com.github.reygnn.hocschwiiz.domain.model

enum class Gender(
    val german: String,
    val swiss: String
) {
    MASCULINE("der", "de"),
    FEMININE("die", "d'"),
    NEUTER("das", "s'"),
    PLURAL("die", "d'")
}