package com.benny.bookapp

class ModelCategory {

    //Variables should match as in Firebase
    var id: String = ""
    var category: String = ""
    var timestamp: Long = 0
    var uid: String = ""

    //Empty constructor, needed by Firebase
    constructor()

    //Parameterised constructor
    constructor(id: String, category: String, timestamp: Long, uid: String) {
        this.id = id
        this.category = category
        this.timestamp = timestamp
        this.uid = uid
    }





}