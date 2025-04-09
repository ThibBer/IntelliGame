package de.uni_passau.fim.se2.intelligame.command

import de.uni_passau.fim.se2.intelligame.util.CoverageInfo

data class UpdateCoverageCommand(val payload: UpdateCoverageCommandData){
    val action = "updateCoverage"
}

data class UpdateCoverageCommandData(
    val id: String, val coverageInfo: CoverageInfo, val testedClass: String, val testName: String, val gameMode: Int
)