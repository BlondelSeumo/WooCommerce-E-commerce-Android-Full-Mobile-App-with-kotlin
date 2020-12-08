package com.iqonic.store.models

import java.io.Serializable


class SearchProduct : Serializable {
    var description: String = ""
    var height: String = ""
    var id: Int = 0
    var images: Images = Images()
    var length: String = ""
    var name: String = ""
    var price: String = ""
    var regular_price: String = ""
    var sale_price: String = ""
    var status: String = ""
    var stock_status: String = ""
    var width: String = ""
}

class Images {
    var gallery: List<String> = listOf()
    var image: List<String> = listOf()
}

