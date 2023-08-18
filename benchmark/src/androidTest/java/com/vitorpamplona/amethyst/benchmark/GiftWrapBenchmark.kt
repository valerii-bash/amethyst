package com.vitorpamplona.amethyst.benchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vitorpamplona.quartz.encoders.toHexKey
import com.vitorpamplona.quartz.crypto.KeyPair
import com.vitorpamplona.quartz.events.NIP24Factory
import com.vitorpamplona.quartz.events.SealedGossipEvent
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Benchmark, which will execute on an Android device.
 *
 * The body of [BenchmarkRule.measureRepeated] is measured in a loop, and Studio will
 * output the result. Modify your code to see how it affects performance.
 */
@RunWith(AndroidJUnit4::class)
class GiftWrapBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    fun basePerformanceTest(message: String, expectedLength: Int) {
        val sender = KeyPair()
        val receiver = KeyPair()

        val events = NIP24Factory().createMsgNIP24(
            message,
            listOf(receiver.pubKey.toHexKey()),
            sender.privKey!!
        )

        Assert.assertEquals(expectedLength, events.map { it.toJson() }.joinToString("").length)

        // Simulate Receiver
        events.forEach {
            it.checkSignature()

            val keyToUse = if (it.recipientPubKey() == sender.pubKey.toHexKey()) sender.privKey!! else receiver.privKey!!
            val event = it.unwrap(keyToUse)
            event?.checkSignature()

            if (event is SealedGossipEvent) {
                val innerData = event.unseal(keyToUse)
                Assert.assertEquals(message, innerData?.content)
            } else {
                Assert.fail("Wrong Event")
            }
        }
    }


    @Test
    fun tinyMessageHardCoded() {
        benchmarkRule.measureRepeated {
            basePerformanceTest("Hola, que tal?", 2946)
        }
    }

    @Test
    fun regularMessageHardCoded() {
        benchmarkRule.measureRepeated {
            basePerformanceTest("Hi, honey, can you drop by the market and get some bread?", 3098)
        }
    }

    @Test
    fun longMessageHardCoded() {
        benchmarkRule.measureRepeated {
            basePerformanceTest(
                "My queen, you are nothing short of royalty to me. You possess more beauty in the nail of your pinkie toe than everything else in this world combined. I am astounded by your grace, generosity, and graciousness. I am so lucky to know you. ",
                3738
            )
        }
    }
/*
    @Test
    fun tinyMessageHardCodedCompressed() {
        benchmarkRule.measureRepeated {
            basePerformanceTest("Hola, que tal?", 2318)
        }
    }

    @Test
    fun regularMessageHardCodedCompressed() {
        benchmarkRule.measureRepeated {
            basePerformanceTest("Hi, honey, can you drop by the market and get some bread?", 2406)
        }
    }

    @Test
    fun longMessageHardCodedCompressed() {
        benchmarkRule.measureRepeated {
            basePerformanceTest(
                "My queen, you are nothing short of royalty to me. You possess more beauty in the nail of your pinkie toe than everything else in this world combined. I am astounded by your grace, generosity, and graciousness. I am so lucky to know you. ",
                2722
            )
        }
    }*/

/*
    @Test
    fun tinyMessageJSONCompressed() {
        benchmarkRule.measureRepeated {
            basePerformanceTest("Hola, que tal?", 2318)
        }
    }

    @Test
    fun regularMessageJSONCompressed() {
        benchmarkRule.measureRepeated {
            basePerformanceTest("Hi, honey, can you drop by the market and get some bread?", 2394)
        }
    }

    @Test
    fun longMessageJSONCompressed() {
        benchmarkRule.measureRepeated {
            basePerformanceTest(
                "My queen, you are nothing short of royalty to me. You possess more beauty in the nail of your pinkie toe than everything else in this world combined. I am astounded by your grace, generosity, and graciousness. I am so lucky to know you. ",
                2714
            )
        }
    }*/

/*
    @Test
    fun tinyMessageJSON() {
        benchmarkRule.measureRepeated {
            basePerformanceTest("Hola, que tal?", 3154)
        }
    }

    @Test
    fun regularMessageJSON() {
        benchmarkRule.measureRepeated {
            basePerformanceTest("Hi, honey, can you drop by the market and get some bread?", 3298)
        }
    }

    @Test
    fun longMessageJSON() {
        benchmarkRule.measureRepeated {
            basePerformanceTest(
                "My queen, you are nothing short of royalty to me. You possess more beauty in the nail of your pinkie toe than everything else in this world combined. I am astounded by your grace, generosity, and graciousness. I am so lucky to know you. ",
                3938
            )
        }
    }*/

/*
    @Test
    fun tinyMessageJackson() {
        benchmarkRule.measureRepeated {
            basePerformanceTest("Hola, que tal?", 3154)
        }
    }

    @Test
    fun regularMessageJackson() {
        benchmarkRule.measureRepeated {
            basePerformanceTest("Hi, honey, can you drop by the market and get some bread?", 3298)
        }
    }

    @Test
    fun longMessageJackson() {
        benchmarkRule.measureRepeated {
            basePerformanceTest(
                "My queen, you are nothing short of royalty to me. You possess more beauty in the nail of your pinkie toe than everything else in this world combined. I am astounded by your grace, generosity, and graciousness. I am so lucky to know you. ",
                3938
            )
        }
    } */
/*
    @Test
    fun tinyMessageKotlin() {
        benchmarkRule.measureRepeated {
            basePerformanceTest("Hola, que tal?", 3154)
        }
    }

    @Test
    fun regularMessageKotlin() {
        benchmarkRule.measureRepeated {
            basePerformanceTest("Hi, honey, can you drop by the market and get some bread?", 3298)
        }
    }

    @Test
    fun longMessageKotlin() {
        benchmarkRule.measureRepeated {
            basePerformanceTest(
                "My queen, you are nothing short of royalty to me. You possess more beauty in the nail of your pinkie toe than everything else in this world combined. I am astounded by your grace, generosity, and graciousness. I am so lucky to know you. ",
                3938
            )
        }
    }*/
/*
    @Test
    fun tinyMessageCSV() {
        benchmarkRule.measureRepeated {
            basePerformanceTest("Hola, que tal?", 2960)
        }
    }

    @Test
    fun regularMessageCSV() {
        benchmarkRule.measureRepeated {
            basePerformanceTest("Hi, honey, can you drop by the market and get some bread?", 3112)
        }
    }

    @Test
    fun longMessageCSV() {
        benchmarkRule.measureRepeated {
            basePerformanceTest(
                "My queen, you are nothing short of royalty to me. You possess more beauty in the nail of your pinkie toe than everything else in this world combined. I am astounded by your grace, generosity, and graciousness. I am so lucky to know you. ",
                3752
            )
        }
    }*/
}