package com.zetarapower.monitor.ui.adapter

/**
 * Created by Evgenii Doronin
 * LinkedIn: https://www.linkedin.com/in/evgeniydoronin
 * 
 * ItemDecoration для добавления отступов между рядами в Cell Voltage сетке
 */

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * ItemDecoration для добавления дополнительных отступов между рядами
 * в сетке Cell Voltage (4 колонки)
 */
class CellVoltageItemDecoration(
    private val spanCount: Int = 4, // Количество колонок в сетке
    private val rowSpacing: Int = 6 // Дополнительный отступ между рядами в dp (2dp)
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        val totalItems = state.itemCount
        if (totalItems == 0) return

        val row = position / spanCount // Номер ряда (0, 1, 2, 3...)
        val totalRows = (totalItems + spanCount - 1) / spanCount // Общее количество рядов
        
        // Добавляем дополнительный отступ снизу для всех рядов кроме последнего
        if (row < totalRows - 1) { // Не последний ряд
            outRect.bottom = rowSpacing
        }
    }
}
