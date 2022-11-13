package com.bios.walkietalkie2.utils

interface ForceInitializable {
    fun forceInit() = this.hashCode()
}