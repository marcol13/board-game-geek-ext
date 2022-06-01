package com.example.boardgamegeekext.api

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "user", strict = false)
data class UserApi @JvmOverloads constructor(

    @field:Element(name = "firstname")
    @param:Element(name = "firstname")
    var name: Name? = null,

    @field:Attribute(name = "name", required = true)
    var nickname: String? = "",

)

@Root(name = "firstname", strict = false)
class Name {
    @field:Attribute(name = "value", required = false) var name: String = ""
}