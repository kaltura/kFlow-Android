package com.kaltura.kflow.entity;

import com.kaltura.kflow.presentation.ui.ParentListItem;

import java.util.List;

public class ParentRecyclerViewItem<P, C> implements ParentListItem {

    private P mParent;
    private List<C> mChildren;

    public ParentRecyclerViewItem(P parent, List<C> children) {
        mParent = parent;
        mChildren = children;
    }

    public P getParent() {
        return mParent;
    }

    @Override
    public List<C> getChildItemList() {
        return mChildren;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }
}
