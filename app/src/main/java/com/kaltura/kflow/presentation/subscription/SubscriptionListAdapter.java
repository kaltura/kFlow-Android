package com.kaltura.kflow.presentation.subscription;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.kaltura.client.types.Asset;
import com.kaltura.client.types.DoubleValue;
import com.kaltura.client.types.Subscription;
import com.kaltura.client.types.Value;
import com.kaltura.kflow.R;
import com.kaltura.kflow.entity.ParentRecyclerViewItem;
import com.kaltura.kflow.presentation.ui.ChildViewHolder;
import com.kaltura.kflow.presentation.ui.ExpandableRecyclerAdapter;
import com.kaltura.kflow.presentation.ui.ParentListItem;
import com.kaltura.kflow.presentation.ui.ParentViewHolder;

import java.util.ArrayList;

/**
 * Created by alex_lytvynenko on 30.11.2018.
 */
public class SubscriptionListAdapter extends ExpandableRecyclerAdapter<SubscriptionListAdapter.PackageViewHolder, SubscriptionListAdapter.SubscriptionViewHolder> {
    private SubscriptionListAdapter.OnPackageClickListener mListener;

    SubscriptionListAdapter(ArrayList<ParentRecyclerViewItem> parentItemList, SubscriptionListAdapter.OnPackageClickListener listener) {
        super(parentItemList);
        mListener = listener;
    }

    @NonNull
    @Override
    public PackageViewHolder onCreateParentViewHolder(@NonNull ViewGroup parent) {
        return new PackageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_package, parent, false));
    }

    @Override
    public SubscriptionViewHolder onCreateChildViewHolder(ViewGroup childViewGroup) {
        return new SubscriptionViewHolder(LayoutInflater.from(childViewGroup.getContext()).inflate(R.layout.item_package, childViewGroup, false));

    }

    @Override
    public void onBindParentViewHolder(PackageViewHolder packageViewHolder, int position, ParentListItem parentListItem) {
        ParentRecyclerViewItem item = (ParentRecyclerViewItem) parentListItem;
        packageViewHolder.bind((Asset) item.getParent(), mListener);
    }

    @Override
    public void onBindChildViewHolder(SubscriptionViewHolder subscriptionViewHolder, int position, Object childListItem) {
        Subscription subscription = (Subscription) childListItem;
        subscriptionViewHolder.bind(subscription);
    }

    public interface OnPackageClickListener {
        void onPackageGetSubscriptionClicked(Double packageBaseId);
    }

    public class PackageViewHolder extends ParentViewHolder {

        private static final float INITIAL_POSITION = 0.0f;
        private static final float ROTATED_POSITION = 180f;

        private AppCompatImageView mArrowView;
        private AppCompatTextView mName;
        private AppCompatTextView mId;
        private AppCompatTextView mBaseId;
        private AppCompatButton mGetSubscription;

        PackageViewHolder(View v) {
            super(v);
            mName = v.findViewById(R.id.asset_name);
            mId = v.findViewById(R.id.asset_id);
            mBaseId = v.findViewById(R.id.base_id);
            mGetSubscription = v.findViewById(R.id.get);
            mArrowView = v.findViewById(R.id.arrow_icon);
        }

        void bind(final Asset asset, final SubscriptionListAdapter.OnPackageClickListener clickListener) {
            mName.setText(asset.getName());
            mId.setText("Asset ID: " + asset.getId());
            Value baseId = asset.getMetas().get("Base ID");
            mBaseId.setText("Base ID: " + (baseId != null ? ((DoubleValue) baseId).getValue().intValue() : "No ID"));
            mGetSubscription.setVisibility(baseId != null ? View.VISIBLE : View.GONE);
            mGetSubscription.setOnClickListener(view -> clickListener.onPackageGetSubscriptionClicked(((DoubleValue) baseId).getValue()));
        }

        @Override
        public void setExpanded(boolean expanded) {
            super.setExpanded(expanded);
            if (expanded) mArrowView.setRotation(ROTATED_POSITION);
            else mArrowView.setRotation(INITIAL_POSITION);
        }

        @Override
        public void onExpansionToggled(boolean expanded) {
            super.onExpansionToggled(expanded);

            RotateAnimation rotateAnimation;
            if (expanded) { // rotate clockwise
                rotateAnimation = new RotateAnimation(ROTATED_POSITION,
                        INITIAL_POSITION,
                        RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                        RotateAnimation.RELATIVE_TO_SELF, 0.5f);
            } else { // rotate counterclockwise
                rotateAnimation = new RotateAnimation(-1 * ROTATED_POSITION,
                        INITIAL_POSITION,
                        RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                        RotateAnimation.RELATIVE_TO_SELF, 0.5f);
            }

            rotateAnimation.setDuration(200);
            rotateAnimation.setFillAfter(true);
            mArrowView.startAnimation(rotateAnimation);

        }
    }

    public class SubscriptionViewHolder extends ChildViewHolder {

        public SubscriptionViewHolder(View itemView) {
            super(itemView);
        }

        public void bind(Subscription subscription) {
        }
    }
}
