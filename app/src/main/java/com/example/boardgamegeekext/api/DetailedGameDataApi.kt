package com.example.boardgamegeekext.api

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name="items", strict = false)
class DetailedGameDataApi @JvmOverloads constructor(
    @field:Element(name = "item")
    var name : ItemElement? = null,
)

@Root(name="item", strict = false)
class ItemElement @JvmOverloads constructor(
    @field:Element(name = "thumbnail")
    var thumbnail : String = "",

    @field:Attribute(name = "type")
    var type : String = ""
)

//@Root(name="error", strict = false)
//class Error @JvmOverloads  constructor(
//    @field:Element(name = "message", required = false)
//    var message : String = ""
//)