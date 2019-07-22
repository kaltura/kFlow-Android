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
import com.kaltura.client.types.BaseChannel;
import com.kaltura.client.types.DoubleValue;
import com.kaltura.client.types.Subscription;
import com.kaltura.client.types.Value;
import com.kaltura.kflow.R;
import com.kaltura.kflow.entity.ParentRecyclerViewItem;
import com.kaltura.kflow.presentation.ui.expandableRecyclerView.ChildViewHolder;
import com.kaltura.kflow.presentation.ui.expandableRecyclerView.ExpandableRecyclerAdapter;
import com.kaltura.kflow.presentation.ui.expandableRecyclerView.ParentListItem;
import com.kaltura.kflow.presentation.ui.expandableRecyclerView.ParentViewHolder;

import java.util.ArrayList;
import java.util.List;

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
        return new SubscriptionViewHolder(LayoutInflater.from(childViewGroup.getContext()).inflate(R.layout.item_subscription, childViewGroup, false));

    }

    @Override
    public void onBindParentViewHolder(PackageViewHolder packageViewHolder, int position, ParentListItem parentListItem) {
        ParentRecyclerViewItem item = (ParentRecyclerViewItem) parentListItem;
        packageViewHolder.bind((Asset) item.getParent(), !item.getChildItemList().isEmpty(), mListener);
    }

    @Override
    public void onBindChildViewHolder(SubscriptionViewHolder subscriptionViewHolder, int position, Object childListItem) {
        Subscription subscription = (Subscription) childListItem;
        subscriptionViewHolder.bind(subscription, mListener);
    }

    void addSubscriptionToPackage(Double packageBaseId, List<Subscription> subscriptions) {
        for (ParentListItem item : getParentItemList()) {
            ParentRecyclerViewItem parentRecyclerViewItem = (ParentRecyclerViewItem) item;
            Value baseId = ((Asset) parentRecyclerViewItem.getParent()).getMetas().get("Base ID");
            if (baseId != null && ((DoubleValue) baseId).getValue().equals(packageBaseId)) {
                expandParent(item);
                parentRecyclerViewItem.getChildItemList().addAll(subscriptions);
                notifyChildItemRangeInserted(getParentItemList().indexOf(item), 0, subscriptions.size());
            }
        }
    }

    public interface OnPackageClickListener {
        void onPackageGetSubscriptionClicked(Double packageBaseId);

        void onSubscriptionClicked(ArrayList<Long> subscriptionChannelsId);
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

        void bind(final Asset asset, boolean isExpandable, final SubscriptionListAdapter.OnPackageClickListener clickListener) {
            setExpanded(isExpandable);
            mName.setText(asset.getName());
            mId.setText("Package ID: " + asset.getId());
            Value baseId = asset.getMetas().get("Base ID");
            mBaseId.setText("Base ID: " + (baseId != null ? ((DoubleValue) baseId).getValue().intValue() : "No ID"));
            mGetSubscription.setVisibility(baseId != null ? View.VISIBLE : View.GONE);
            mGetSubscription.setOnClickListener(view -> {
                if (clickListener != null) {
                    clickListener.onPackageGetSubscriptionClicked(((DoubleValue) baseId).getValue());
                    mGetSubscription.setVisibility(View.GONE);
                    mArrowView.setVisibility(View.VISIBLE);
                }
            });
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

    class SubscriptionViewHolder extends ChildViewHolder {

        private AppCompatTextView mName;
        private AppCompatTextView mId;
        private AppCompatTextView mChannelIds;
        private AppCompatImageView mArrow;

        SubscriptionViewHolder(View itemView) {
            super(itemView);
            mName = itemView.findViewById(R.id.asset_name);
            mId = itemView.findViewById(R.id.asset_id);
            mChannelIds = itemView.findViewById(R.id.channel_ids);
            mArrow = itemView.findViewById(R.id.arrow);
        }

        void bind(Subscription subscription, final SubscriptionListAdapter.OnPackageClickListener clickListener) {
            mName.setText(subscription.getName());
            mId.setText("Subscription ID: " + subscription.getId());
            StringBuilder stringBuilder = new StringBuilder("Channels ID: [");
            if (subscription.getChannels() != null) {
                for (BaseChannel baseChannel : subscription.getChannels()) {
                    if (subscription.getChannels().indexOf(baseChannel) != 0)
                        stringBuilder.append(", ");
                    stringBuilder.append(baseChannel.getId().toString());
                }
            }
            stringBuilder.append("]");
            mChannelIds.setText(stringBuilder);
            mArrow.setVisibility(subscription.getChannels().isEmpty() ? View.GONE : View.VISIBLE);
            itemView.setOnClickListener(view -> {
                if (clickListener != null) {
                    ArrayList<Long> channelsId = new ArrayList<>();
                    for (BaseChannel baseChannel : subscription.getChannels())
                        channelsId.add(baseChannel.getId());

                    clickListener.onSubscriptionClicked(channelsId);
                }
            });
        }
    }
}
