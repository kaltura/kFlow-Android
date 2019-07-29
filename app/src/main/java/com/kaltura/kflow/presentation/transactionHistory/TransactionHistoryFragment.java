package com.kaltura.kflow.presentation.transactionHistory;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kaltura.client.enums.EntityReferenceBy;
import com.kaltura.client.services.TransactionHistoryService;
import com.kaltura.client.types.BillingTransaction;
import com.kaltura.client.types.TransactionHistoryFilter;
import com.kaltura.client.utils.request.RequestBuilder;
import com.kaltura.kflow.R;
import com.kaltura.kflow.manager.PhoenixApiManager;
import com.kaltura.kflow.presentation.debug.DebugFragment;
import com.kaltura.kflow.presentation.main.MainActivity;
import com.kaltura.kflow.utils.Utils;

import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
public class TransactionHistoryFragment extends DebugFragment implements View.OnClickListener {

    private AppCompatButton mShowTransactionsButton;
    private RecyclerView mTransactionsList;
    private ArrayList<BillingTransaction> mTransactions = new ArrayList<>();
    private TransactionHistoryListAdapter mTransactionHistoryListAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transaction_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) requireActivity()).getSupportActionBar().setTitle("Transaction history");

        mShowTransactionsButton = getView().findViewById(R.id.show_transactions);
        initList();

        mShowTransactionsButton.setOnClickListener(this);
        getView().findViewById(R.id.get).setOnClickListener(this);

        mShowTransactionsButton.setVisibility(mTransactions.isEmpty() ? View.GONE : View.VISIBLE);
        mShowTransactionsButton.setText(getResources().getQuantityString(R.plurals.show_transactions,
                mTransactions.size(), NumberFormat.getInstance().format(mTransactions.size())));
    }

    private void initList() {
        mTransactionsList = getView().findViewById(R.id.transactions_list);
        mTransactionsList.setNestedScrollingEnabled(false);
        mTransactionsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        mTransactionsList.addItemDecoration(new DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL));
        mTransactionHistoryListAdapter = new TransactionHistoryListAdapter(new ArrayList<>());
        mTransactionsList.setAdapter(mTransactionHistoryListAdapter);
    }

    @Override
    public void onClick(View view) {
        Utils.hideKeyboard(getView());

        switch (view.getId()) {
            case R.id.show_transactions: {
                showTransactions();
                break;
            }
            case R.id.get: {
                makeGetTransactionHistoryRequest();
                break;
            }
        }
    }

    private void makeGetTransactionHistoryRequest() {
        if (Utils.hasInternetConnection(requireContext())) {

            TransactionHistoryFilter transactionHistoryFilter = new TransactionHistoryFilter();
            transactionHistoryFilter.setEntityReferenceEqual(EntityReferenceBy.HOUSEHOLD);
            transactionHistoryFilter.setEndDateLessThanOrEqual((int) ((System.currentTimeMillis() + DateUtils.YEAR_IN_MILLIS) / 1000));

            RequestBuilder requestBuilder = TransactionHistoryService.list(transactionHistoryFilter)
                    .setCompletion(result -> {
                        if (result.isSuccess() && result.results != null) {
                            if (result.results.getObjects() != null)
                                mTransactions.addAll(result.results.getObjects());

                            mShowTransactionsButton.setText(getResources().getQuantityString(R.plurals.show_transactions,
                                    mTransactions.size(), NumberFormat.getInstance().format(mTransactions.size())));
                            mShowTransactionsButton.setVisibility(View.VISIBLE);
                        }
                    });
            clearDebugView();
            PhoenixApiManager.execute(requestBuilder);
        } else {
            Toast.makeText(requireContext(), "No Internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void showTransactions() {
        mTransactionsList.setVisibility(View.VISIBLE);
        mShowTransactionsButton.setVisibility(View.GONE);
        mTransactionHistoryListAdapter = new TransactionHistoryListAdapter(mTransactions);
        mTransactionsList.setAdapter(mTransactionHistoryListAdapter);
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
