package com.shiftstudio.workflow.sample.tictactoe.gameworkflow

import android.view.ViewGroup
import android.widget.TextView

/**
 * Shared code for painting a 3 x 3 set of [TextView] cells with the values
 * of a [Board].
 */
internal fun Board.render(viewGroup: ViewGroup) {
    for (i in 0..8) {
        val row = i / 3
        val col = i % 3

        val cell = viewGroup.getChildAt(i) as TextView
        val box = this[row][col]
        cell.text = box?.symbol ?: ""
    }
}

val Player.symbol: String
    get() = when (this) {
        Player.X -> "🙅"
        Player.O -> "🙆"
    }
