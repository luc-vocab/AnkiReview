package com.luc.ankireview.style;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import static androidx.recyclerview.widget.ItemTouchHelper.Callback.makeMovementFlags;

public class ItemTouchCallback extends ItemTouchHelper.Callback {

    private ActionCompletionContract m_contract;

    public ItemTouchCallback(ActionCompletionContract contract) {
        this.m_contract = contract;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            /*
            if (viewHolder instanceof SectionHeaderViewHolder) {
                return 0;
            }
            */
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = 0;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        m_contract.onViewMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
    }
}
