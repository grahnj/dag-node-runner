package org.jgrahn.pattern.domain

import org.jgrahn.pattern.CommandStop
import org.jgrahn.pattern.InteractionHooks
import org.jgrahn.pattern.PassengerList
import org.jgrahn.pattern.executeStop

class Scratch(
    val hooks: InteractionHooks,
) {
    fun <T> execute(stop: DomainCommandStop, manager: PassengerList) {
        executeStop(stop, hooks, manager, )
    }
}