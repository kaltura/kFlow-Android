package com.kaltura.kflow.presentation;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.kaltura.client.enums.TransactionType;
import com.kaltura.client.services.TransactionService;
import com.kaltura.client.types.ExternalReceipt;
import com.kaltura.client.utils.request.RequestBuilder;
import com.kaltura.kflow.R;
import com.kaltura.kflow.manager.PhoenixApiManager;
import com.kaltura.kflow.presentation.debug.DebugFragment;
import com.kaltura.kflow.presentation.main.MainActivity;
import com.kaltura.kflow.utils.Utils;

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
public class CheckReceiptFragment extends DebugFragment implements View.OnClickListener {

    private TextInputEditText mReceiptId;
    private TextInputEditText mProductType;
    private TextInputEditText mProductId;
    private TextInputEditText mContentId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_check_receipt, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) requireActivity()).getSupportActionBar().setTitle("Check receipt");

        mReceiptId = getView().findViewById(R.id.receipt_id);
        mProductType = getView().findViewById(R.id.product_type);
        mProductId = getView().findViewById(R.id.product_id);
        mContentId = getView().findViewById(R.id.content_id);

        getView().findViewById(R.id.validate).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Utils.hideKeyboard(getView());

        switch (view.getId()) {
            case R.id.validate: {
                checkReceiptRequest(mReceiptId.getText().toString(), mProductType.getText().toString(),
                        mProductId.getText().toString(), mContentId.getText().toString());
                break;
            }
        }
    }

    private void checkReceiptRequest(String receiptId, String productType, String productId, String contentId) {
        if (TextUtils.isEmpty(receiptId) || TextUtils.isEmpty(productType) ||
                TextUtils.isEmpty(productId) || TextUtils.isEmpty(contentId)) {
            Toast.makeText(requireContext(), "Wrong input, please fill in all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Utils.hasInternetConnection(requireContext())) {

            if (TextUtils.isDigitsOnly(productId) && TextUtils.isDigitsOnly(contentId)) {
                ExternalReceipt externalReceipt = new ExternalReceipt();
                externalReceipt.setReceiptId(receiptId);

                TransactionType transactionType = null;
                if (productType.equalsIgnoreCase(TransactionType.PPV.getValue()) ||
                        productType.equalsIgnoreCase(TransactionType.SUBSCRIPTION.getValue()) ||
                        productType.equalsIgnoreCase(TransactionType.COLLECTION.getValue())) {
                    transactionType = TransactionType.get(productType.toLowerCase());
                }
                externalReceipt.setProductType(transactionType);
                externalReceipt.setProductId(Integer.parseInt(productId));
                externalReceipt.setContentId(Integer.parseInt(contentId));
                externalReceipt.setPaymentGatewayName("PGAdapterGoogle");

                RequestBuilder requestBuilder = TransactionService.validateReceipt(externalReceipt);
                clearDebugView();
                PhoenixApiManager.execute(requestBuilder);
            } else {
                Toast.makeText(requireContext(), "Wrong input", Toast.LENGTH_SHORT).show();
            }
        } else {
            Snackbar.make(getView(), "No Internet connection", Snackbar.LENGTH_LONG)
                    .setAction("Dismiss",view -> {})
                    .show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Utils.hideKeyboard(getView());
        PhoenixApiManager.cancelAll();
    }

    @Override
    protected int getDebugViewId() {
        return R.id.debug_view;
    }
}
