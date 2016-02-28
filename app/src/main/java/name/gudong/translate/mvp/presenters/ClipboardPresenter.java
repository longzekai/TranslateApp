/*
 *  Copyright (C) 2015 GuDong <gudong.name@gmail.com>
 *
 *  This file is part of GdTranslate
 *
 *  GdTranslate is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  GdTranslate is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with GdTranslate.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package name.gudong.translate.mvp.presenters;

import android.app.Service;
import android.os.CountDownTimer;
import android.text.TextUtils;

import com.litesuits.orm.LiteOrm;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import name.gudong.translate.GDApplication;
import name.gudong.translate.listener.clipboard.ClipboardManagerCompat;
import name.gudong.translate.listener.view.TipViewController;
import name.gudong.translate.mvp.model.WarpAipService;
import name.gudong.translate.mvp.model.entity.AbsResult;
import name.gudong.translate.mvp.model.entity.Result;
import name.gudong.translate.mvp.model.type.EIntervalTipTime;
import name.gudong.translate.mvp.views.IClipboardService;
import name.gudong.translate.util.SpUtils;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by GuDong on 2/28/16 20:48.
 * Contact with gudong.name@gmail.com.
 */
public class ClipboardPresenter extends BasePresenter<IClipboardService> implements TipViewController.ViewDismissHandler {
    @Inject
    ClipboardManagerCompat mClipboardWatcher;
    @Inject
    TipViewController mTipViewController;
    CountDownTimer countDownTimer;
    @Inject
    public ClipboardPresenter(LiteOrm liteOrm, WarpAipService apiService, Service service) {
        super(liteOrm, apiService, service);

    }

    /**
     * 循环显示生词本中的内容
     */
    public void controlShowTipCyclic(){
        boolean reciteFlag = SpUtils.getReciteOpenOrNot(mService);
        EIntervalTipTime tipTime = SpUtils.getIntervalTimeWay(GDApplication.mContext);
        int time = tipTime.getIntervalTime();
        countDownTimer = new CountDownTimer(Integer.MAX_VALUE,time*60*1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Result result = getResult();
                if(result != null){
                    prepareShow(getResult());
                    show(false);
                }
            }
            @Override
            public void onFinish() {}
        };
        if(reciteFlag){
            countDownTimer.start();
            Logger.i("启动");
        }else{
            countDownTimer.cancel();
            Logger.i("取消");
        }

//        if(reciteFlag){
//            Observable.interval(time, TimeUnit.MINUTES)
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(observerShowTipCyclic);
//        }

    }

    private Result getResult(){
        List<Result> results = mLiteOrm.query(Result.class);
        if(results.isEmpty()){
            return null;
        }
        int index = new Random().nextInt(results.size());
        return results.get(index);
    }

    public void searchContent(final String content) {
        mWarpApiService.translate(SpUtils.getTranslateEngineWay(mService), content)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(new Func1<AbsResult, Boolean>() {
                    @Override
                    public Boolean call(AbsResult youDaoResult) {
                        return youDaoResult.wrapErrorCode() == 0;
                    }
                })
                .subscribe(new Subscriber<AbsResult>() {
                    @Override
                    public void onCompleted() {
                        //显示顶部悬浮框
                        show(true);
                        //清空缓存
                        listQuery.clear();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(AbsResult result) {
                        prepareShow(result.getResult());
                    }
                });
    }

    public void prepareShow(Result result) {
        mTipViewController.setResultContent(result);
    }

    private void show(boolean isShowFavorite) {
        mTipViewController.show(isShowFavorite);
    }

    private ClipboardManagerCompat.OnPrimaryClipChangedListener mListener = () -> performClipboardCheck();
    /**
     * 添加粘贴板变化监听方法
     */
    public void addListener() {
        //添加粘贴板变化监听方法
        mClipboardWatcher.addPrimaryClipChangedListener(mListener);
        //添加顶部提示框监听方法
        mTipViewController.setViewDismissHandler(this);
    }

    /**
     * 首先说明一个情况，这里的系统粘贴板在监听到粘贴板内容发生变化时
     * 对应的监听方法会被执行多次 也就是说用户的复制操作 会引起这里发送多次数据请求
     * 这是不合理的，所以设置一个缓存用于解决c重复请求问题
     */

    /**
     * 定义一个查询集合，用于缓存当前的查询队列，当队列中存在已经复制的关键字就不会继续去发起查询操作了
     * 注意要在合适的时候清空它
     */
    private List<String> listQuery = new ArrayList<>();

    private void performClipboardCheck() {
        CharSequence content = mClipboardWatcher.getText();
        if (TextUtils.isEmpty(content)) return;

        //处理缓存
        String query = content.toString();
        if (listQuery.contains(query)) return;
        listQuery.add(query);

        //查询数据
        searchContent(query);
    }

    public void onDestroy() {
        super.onDestroy();
        mClipboardWatcher.removePrimaryClipChangedListener(mListener);
        if (mTipViewController != null) {
            mTipViewController.setViewDismissHandler(null);
            mTipViewController = null;
        }
    }

    @Override
    public void onViewDismiss() {

    }
}
