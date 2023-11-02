package com.mineinabyss.components.lootcrates

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Roll {
    abstract fun roll(): Int

    @Serializable
    @SerialName("minecraft:uniform")
    data class Uniform(val min: Int, val max: Int) : Roll() {
        override fun roll(): Int {
            return (min..max).random()
        }
    }

    @Serializable
    @SerialName("minecraft:constant")
    data class Constant(val value: Int) : Roll() {
        override fun roll() = value
    }

    @Serializable
    @SerialName("minecraft:binomial")
    data class Binomial(val n: Int, val p: Double) : Roll() {
        override fun roll(): Int {
            var successes = 0
            repeat(n) {
                if (Math.random() < p) successes++
            }
            return successes
        }
    }
}
