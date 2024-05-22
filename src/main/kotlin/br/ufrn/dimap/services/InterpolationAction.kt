package br.ufrn.dimap.services

import br.ufrn.dimap.entities.UnknownPoint
import br.ufrn.dimap.repositories.LocationRepository
import br.ufrn.dimap.services.InterpolationService.assignTemperatureToUnknownPoints
import java.util.concurrent.RecursiveAction
import java.util.logging.Logger

class InterpolationAction : RecursiveAction {
    private val unknownPoints: List<UnknownPoint?>
    private val subListsQuantity: Int

    private val THRESHOLD = Runtime.getRuntime().availableProcessors()

    constructor() {
        this.unknownPoints = LocationRepository.instance.unknownPointsAsAList
        this.subListsQuantity = 0
    }

    constructor(unknownPoints: List<UnknownPoint?>, subListsQuantity: Int) {
        this.unknownPoints = unknownPoints
        this.subListsQuantity = subListsQuantity
    }

    public override fun compute() {
        if (subListsQuantity < THRESHOLD) {
            invokeAll(createSubtasks())
        } else {
            processing(unknownPoints)
        }
    }

    private fun createSubtasks(): List<InterpolationAction> {
        val subtasks: MutableList<InterpolationAction> = ArrayList()

        subtasks.add(InterpolationAction(unknownPoints.subList(0, unknownPoints.size / 2), subListsQuantity + 2))
        subtasks.add(InterpolationAction(unknownPoints.subList(unknownPoints.size / 2, unknownPoints.size), subListsQuantity + 2))

        return subtasks
    }

    private fun processing(unknownPoints: List<UnknownPoint?>) {
        assignTemperatureToUnknownPoints(unknownPoints)
        logger.info("This result was processed by " + Thread.currentThread().name)
    }

    companion object {
        private val logger: Logger = Logger.getAnonymousLogger()
    }
}