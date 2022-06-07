package com.example.boardgamegeekext.api

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "user", strict = false)
data class UserApi @JvmOverloads constructor(

    @field:Element(name = "firstname")
    @param:Element(name = "firstname")
    var name: Name? = null,

    @field:Element(name = "avatarlink")
    @param:Element(name = "avatarlink")
    var avatar: Avatar? = null,

    @field:Attribute(name = "name", required = true)
    var nickname: String? = "",
)

@Root(name = "firstname", strict = false)
class Name {
    @field:Attribute(name = "value", required = false) var name: String = ""
}

@Root(name = "avatarlink", strict = false)
class Avatar {
    @field:Attribute(name = "value", required = false) var avatar: String = ""
}