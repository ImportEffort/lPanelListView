package com.wangshijia.www.hvscrollabelpanel;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;

import com.wangshijia.www.panellistviewlibrary.PanelListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private StockListAdapter mAdapter;
    private PanelListView panelListView;
    private ArrayList<StockDataInfo> stockDataInfoList;
    private int sortType = 2;
    private int currentTabIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        panelListView = findViewById(R.id.hv_scrollview);
        stockDataInfoList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            StockDataInfo stockDataInfo = new StockDataInfo();
            stockDataInfo.setStockName("浦发银行");
            stockDataInfo.setStockCode("600000");
            stockDataInfo.setPriceLastest("13.08");
            stockDataInfo.setPriceOffsetRate("0.10");
            stockDataInfo.setPriceHigh("13.10");
            stockDataInfo.setPriceLow("12.80");
            stockDataInfo.setPriceOpen("12.90");
            stockDataInfo.setPricePreClose("12.90");
            stockDataInfo.setTradVulumes("12.90");
            stockDataInfo.setTotalMarketValue("12.90");
            stockDataInfoList.add(stockDataInfo);
        }
        panelListView.setNeedShortTitle(true);
        //定义顶部栏
        panelListView.setHeaderListData(new String[]{"最新价", "涨跌幅", "最高价", "最低价", "开盘价", "收盘价", "成交量", "总市值"});
        mAdapter = new StockListAdapter(this, stockDataInfoList, R.layout.item_layout);
        panelListView.setAdapter(mAdapter);

        //点击列表item
        panelListView.setOnItemClick((parent, view, position, id) -> Toast.makeText(MainActivity.this, position + "", Toast.LENGTH_SHORT).show());
        //点击头部按钮
        panelListView.setOnHeaderClickedListener(string -> Toast.makeText(MainActivity.this, string, Toast.LENGTH_SHORT).show());

        //排序的头部按钮
        panelListView.setOnHeaderSortClickListener((cIndex, imageView) -> {
            sortType = changeShortType(cIndex, sortType);
            currentTabIndex = cIndex;
            setTitleSortImg(cIndex, imageView);
            //执行排序操作
        });

        panelListView.setOnRefreshListener(() -> panelListView.postDelayed(() -> {
            //执行你的网络请求
            panelListView.setRefreshCompleted();
        }, 1000));

        panelListView.setOnLoadMoreListener(() -> panelListView.postDelayed(() -> {
            //执行你的网络请求
            panelListView.onLoadingComplete();
        }, 1000));
    }

    private void setTitleSortImg(int cIndex, ImageView imageView) {
        imageView.setImageResource(getSortImg(sortType));
        if (sortType != 2) {
            panelListView.initTitleImageRight(cIndex, true);
        } else {
            panelListView.initTitleImageRight(cIndex, false);
        }
    }


    private int changeShortType(int cIndex, int sortType) {
        if (cIndex != currentTabIndex) {
            sortType = 2;
        }
        currentTabIndex = cIndex;
        if (sortType == 2) {
            return 1;
        } else if (sortType == 1) {
            return 0;
        } else {
            return 2;
        }
    }


    private int getSortImg(int sortType) {
        int riseImgId;
        if (sortType == 0) {
            riseImgId = R.mipmap.rise_img;
        } else if (sortType == 1) {
            riseImgId = R.mipmap.fall_img;
        } else {
            riseImgId = R.mipmap.stocksign;
        }

        return riseImgId;
    }
}
