package com.example.boardgamegeekext.api

import org.simpleframework.xml.*

@Root(name="items", strict = false)
class CollectionApi @JvmOverloads constructor(
    @field: ElementList(inline = true)
    var itemList: List<Item>? = null,

    @field:Attribute(name="totalitems")
    var amount: String = "",

)

@Root(name="item", strict = false)
class Item @JvmOverloads constructor(
    @field:Attribute(name = "objectid")
    var objectid: String = "",

    @field:Element(name = "name")
    var name : String = "",

    @field:Element(name = "yearpublished", required = false)
    var year : String? = null,

    @field:Element(name = "stats")
    var stats : Stats? = null
)

@Root(name = "stats", strict = false)
class Stats @JvmOverloads constructor(
    @field:Element(name = "rating")
    var rating : Rating? = null,
)

@Root(name = "rating", strict = false)
class Rating @JvmOverloads constructor(
    @field:Element(name = "ranks")
    var ranks : Ranks? = null
)

@Root(name = "ranks", strict = false)
class Ranks @JvmOverloads constructor(
    @field:ElementList(inline = true)
    var rank : List<RankInstance>? = null
)

@Root(name = "rank", strict = false)
class RankInstance @JvmOverloads constructor(
    @field:Attribute(name = "value")
    var value : String = "",

    @field:Attribute(name = "type")
    var type : String = ""
)
