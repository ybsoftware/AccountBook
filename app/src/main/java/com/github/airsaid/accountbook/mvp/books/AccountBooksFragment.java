package com.github.airsaid.accountbook.mvp.books;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.airsaid.accountbook.R;
import com.github.airsaid.accountbook.adapter.AccountBooksAdapter;
import com.github.airsaid.accountbook.base.BaseFragment;
import com.github.airsaid.accountbook.constants.AppConstants;
import com.github.airsaid.accountbook.constants.MsgConstants;
import com.github.airsaid.accountbook.data.AccountBook;
import com.github.airsaid.accountbook.data.Error;
import com.github.airsaid.accountbook.ui.activity.AddShareUserActivity;
import com.github.airsaid.accountbook.util.DimenUtils;
import com.github.airsaid.accountbook.util.ProgressUtils;
import com.github.airsaid.accountbook.util.ToastUtils;
import com.github.airsaid.accountbook.util.UiUtils;
import com.github.airsaid.accountbook.util.UserUtils;
import com.github.airsaid.accountbook.widget.recycler.OnSimpleClickListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * @author Airsaid
 * @github https://github.com/airsaid
 * @date 2017/4/14
 * @desc 帐薄 Fragment
 */
public class AccountBooksFragment extends BaseFragment implements AccountBooksContract.View {

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    private AccountBooksContract.Presenter mPresenter;
    private AccountBooksAdapter mAdapter;

    public static AccountBooksFragment newInstance() {
        return new AccountBooksFragment();
    }

    @Override
    public void setPresenter(AccountBooksContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public View getLayout(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account_books, null);
    }

    @Override
    public void onCreateFragment(@Nullable Bundle savedInstanceState) {
        initAdapter();
        refreshData();
    }

    private void initAdapter() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new AccountBooksAdapter(R.layout.item_account_books_list, new ArrayList<AccountBook>());
        mAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_LEFT);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new OnSimpleClickListener() {

            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                // 设置当前帐薄
                AccountBook book = (AccountBook) baseQuickAdapter.getData().get(i);
                mPresenter.setCurrentBook(UserUtils.getUser(), book.getBid());
            }

            @Override
            public void onItemChildClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                switch (view.getId()){
                    case R.id.img_add_user: // 进入邀请好友页
                        AccountBook book = (AccountBook) baseQuickAdapter.getData().get(i);
                        Intent intent = new Intent(mContext, AddShareUserActivity.class);
                        intent.putExtra(AppConstants.EXTRA_DATA, book);
                        startActivity(intent);
                        break;
                }
            }
        });
    }

    private void refreshData(){
        mPresenter.queryBooks(UserUtils.getUser());
    }

    @Override
    public void queryBooksSuccess(List<AccountBook> books) {
        mAdapter.setNewData(books);
    }

    @Override
    public void queryBooksFail(Error e) {
        ToastUtils.show(mContext, e.getMessage());
    }

    @Override
    public void setCurrentBookSuccess() {
        Message msg = new Message();
        msg.what = MsgConstants.MSG_SET_CUR_BOOK_SUCCESS;
        EventBus.getDefault().post(msg);
        finish();
    }

    @Override
    public void setCurrentBookFail(Error e) {
        ToastUtils.show(mContext, e.getMessage());
    }

    @Override
    public void addShareBook() {
        showInputBookIdDialog();
    }

    @Override
    public void addShareBookSuccess() {
        ProgressUtils.dismiss();
        ToastUtils.show(mContext, UiUtils.getString(R.string.toast_add_share_book_success));
        refreshData();
    }

    @Override
    public void addShareBookFail(Error e) {
        ProgressUtils.dismiss();
        ToastUtils.show(mContext, e.getMessage());
    }

    /**
     * 显示输入帐薄 ID Dialog
     */
    private void showInputBookIdDialog() {
        final AppCompatEditText edtBookId = new AppCompatEditText(mContext);
        edtBookId.setInputType(InputType.TYPE_CLASS_NUMBER);
        edtBookId.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
        AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(UiUtils.getString(R.string.dialog_title_input_bid))
                .setNegativeButton(UiUtils.getString(R.string.dialog_cancel), null)
                .setPositiveButton(UiUtils.getString(R.string.dialog_affirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String bid = edtBookId.getText().toString();
                        if(TextUtils.isEmpty(bid)){
                            ToastUtils.show(mContext, UiUtils.getString(R.string.toast_input_bid_error));
                        }else{
                            ProgressUtils.show(mContext);
                            mPresenter.addShareBook(UserUtils.getUser(), Long.valueOf(bid));
                        }
                    }
                })
                .create();
        dialog.setView(edtBookId, DimenUtils.dp2px(16), DimenUtils.dp2px(16), DimenUtils.dp2px(16), DimenUtils.dp2px(16));
        dialog.show();
    }
}