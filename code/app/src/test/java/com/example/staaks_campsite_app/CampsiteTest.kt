package com.example.staaks_campsite_app

import com.example.staaks_campsite_app.data.model.Campsite
import com.example.staaks_campsite_app.data.model.User
import org.junit.Before
import org.junit.Test

class CampsiteTest {
    private val owner = User("123")
    var campsite = Campsite(
        id = "123",
        owner = owner,
        name = "Test Campsite",
        location = "Test Location",
        capacity = 10,
        price = 100.0,
        date = mutableListOf("2023-07-01", "2023-07-02")
    )
    @Before
    fun setUp(){
        campsite = Campsite(
            id = "123",
            owner = owner,
            name = "Test Campsite",
            location = "Test Location",
            capacity = 10,
            price = 100.0,
            date = mutableListOf("2023-07-01", "2023-07-02")
        )
    }

    @Test
    fun test_addImage(){
        campsite.addImage("image1.jpg")
        campsite.addImage("image2.jpg")
        assert(campsite.images.size == 2)
        assert(campsite.images.contains("image1.jpg"))
        assert(campsite.images.contains("image2.jpg"))
    }

    @Test
    fun test_removeImage() {
        campsite.addImage("image1.jpg")
        campsite.addImage("image2.jpg")
        campsite.removeImage("image1.jpg")
        assert(campsite.images.size == 1)
    }

    @Test
    fun test_addDate() {
        campsite.addDate("2023-07-03")
        campsite.addDate("2023-07-04")
        assert(campsite.date.size == 4)
        assert(campsite.date.contains("2023-07-03"))
        assert(campsite.date.contains("2023-07-04"))
    }

    @Test
    fun test_removeDate() {
        campsite.addDate("2023-07-03")
        campsite.addDate("2023-07-04")
        campsite.removeDate("2023-07-03")
        assert(campsite.date.size == 3)
        assert(campsite.date.contains("2023-07-04"))
        assert(campsite.date.contains("2023-07-02"))
    }
}