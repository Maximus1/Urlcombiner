package com.maxxtools.urlcombiner // Bitte durch deinen echten Paketnamen ersetzen

import java.util.UUID

data class BaseUrlItem(
    val id: String = UUID.randomUUID().toString(), // Eine eindeutige ID für jeden Eintrag
    val name: String,
    val url: String
)