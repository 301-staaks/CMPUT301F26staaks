package com.example.staaks_campsite_app

import com.example.staaks_campsite_app.data.CampsiteRepository
import com.example.staaks_campsite_app.data.model.Campsite
import com.example.staaks_campsite_app.data.model.User
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

class CampsiteRepositoryTest {
    private val db = mockk<FirebaseFirestore>()
    private val collectionRef = mockk<CollectionReference>()

    private val listenerSlot = slot<EventListener<QuerySnapshot>>()
    private val inMemoryCampsites = mutableListOf<Campsite>()
    private lateinit var campsiteRepository: CampsiteRepository

    private val owner = User("123")
    private val campsite1 = Campsite(
        id = "123",
        owner = owner,
        name = "Test Campsite",
        location = "Test Location",
        capacity = 10,
        price = 100.0,
        date = mutableListOf("2023-07-01", "2023-07-02")
    )
    private val campsite2 = Campsite(
        id = "456",
        owner = owner,
        name = "Test Campsite 2",
        location = "Test Location 2",
        capacity = 20,
        price = 200.0,
        date = mutableListOf("2023-07-03", "2023-07-04"),
        images = mutableListOf("image1.jpg", "image2.jpg"),
        description = "Test description",
        rating = 4.5
    )

    @Before
    fun setUp() {
        inMemoryCampsites.clear()

        // 1. Stub collection reference
        every { db.collection("campsites") } returns collectionRef

        // 2. Capture the snapshot listener registered in CampsiteRepository's init {} block
        every { collectionRef.addSnapshotListener(capture(listenerSlot)) } returns mockk<ListenerRegistration>(relaxed = true)

        // 3. Handle document() calls for generating auto IDs
        val autoDocRef = mockk<DocumentReference>()
        every { autoDocRef.id } returns "generated_auto_id_123"
        every { collectionRef.document() } returns autoDocRef

        // 4. Intercept document(id) calls to handle set() and delete()
        every { collectionRef.document(any()) } answers {
            val docId = firstArg<String>()
            val docRef = mockk<DocumentReference>()
            every { docRef.id } returns docId

            // When set() is called, update mock list and trigger snapshot callback
            every { docRef.set(any()) } answers {
                val campsite = firstArg<Campsite>()
                inMemoryCampsites.removeAll { it.id == campsite.id }
                inMemoryCampsites.add(campsite)
                notifySnapshotListener()
                mockk(relaxed = true)
            }

            // When delete() is called, remove from mock list and trigger snapshot callback
            every { docRef.delete() } answers {
                inMemoryCampsites.removeAll { it.id == docId }
                notifySnapshotListener()
                mockk(relaxed = true)
            }

            docRef
        }

        // Initialize repository (registers listener via captured slot)
        campsiteRepository = CampsiteRepository(db)
    }

    /**
     * Simulates Firestore firing an updated snapshot to the snapshot listener.
     */
    private fun notifySnapshotListener() {
        val querySnapshot = mockk<QuerySnapshot>()
        val docSnapshots = inMemoryCampsites.map { campsite ->
            val docSnap = mockk<DocumentSnapshot>()
            every { docSnap.toObject(Campsite::class.java) } returns campsite
            docSnap
        }
        every { querySnapshot.documents } returns docSnapshots

        // Fire the listener captured from CampsiteRepository init {}
        if (listenerSlot.isCaptured) {
            listenerSlot.captured.onEvent(querySnapshot, null)
        }
    }

    @Test
    fun testCreateAndAddCampsite() {
        campsiteRepository.createAndAddCampsite(
            owner = owner,
            name = "Test Campsite",
            location = "Test Location",
            capacity = 2,
            price = 100.0,
            date = mutableListOf("2023-07-01", "2023-07-02")
        )

        assertEquals(1, campsiteRepository.count())
        val created = campsiteRepository.campsites[0]
        assertEquals("Test Campsite", created.name)
        assertEquals("Test Location", created.location)
    }

    @Test
    fun testGenerateUniqueID() {
        val uniqueID = campsiteRepository.generateUniqueID()
        assertTrue(uniqueID.isNotEmpty())
    }

    @Test
    fun testAddOrUpdateCampsite() {
        campsiteRepository.addOrUpdateCampsite(campsite1)
        assertEquals(1, campsiteRepository.count())

        campsiteRepository.addOrUpdateCampsite(campsite2)
        assertEquals(2, campsiteRepository.count())

        val updated = Campsite(campsite1.id, owner, "Updated Campsite", campsite1.location, campsite1.capacity, price = campsite1.price, date =campsite1.date)
        campsiteRepository.addOrUpdateCampsite(updated)

        assertEquals(2, campsiteRepository.count())
        assertEquals("Updated Campsite", campsiteRepository.campsites.find { it.id == "123" }?.name)
    }

    @Test
    fun testGetCampsitesByOwner() {
        campsiteRepository.addOrUpdateCampsite(campsite1)
        campsiteRepository.addOrUpdateCampsite(campsite2)

        val ownerCampsites = campsiteRepository.getCampsitesByOwner(owner)
        assertEquals(2, ownerCampsites.size)
    }

    @Test
    fun testDeleteCampsite() {
        campsiteRepository.addOrUpdateCampsite(campsite1)
        campsiteRepository.deleteCampsite(campsite1)

        assertEquals(0, campsiteRepository.count())
    }

    @Test
    fun testFilterCampsites() {
        campsiteRepository.addOrUpdateCampsite(campsite1)
        campsiteRepository.addOrUpdateCampsite(campsite2)

        val filteredCampsites = campsiteRepository.filterCampsites(
            keyword = "Test",
            location = "Test Location",
            date = listOf("2023-07-01", "2023-07-02"),
            capacity = 10
        )

        assertEquals(1, filteredCampsites.size)
        assertEquals("123", filteredCampsites[0].id)
    }
}