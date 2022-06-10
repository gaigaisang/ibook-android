package xyz.xiaogai.ibook;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Fragment1 extends Fragment {
    private static final int MISSION_SUCCESS = 0;
    private static final int MISSION_FAILED = -1;

    private Handler handler;
    private ListView listView;
    private List<Map<String, Object>> datas;
    int i =0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_1, container, false);
        init(view);

        return view;
    }

    private void init(View view) {

        RefreshLayout refreshLayout = (RefreshLayout)view.findViewById(R.id.refreshLayout);
        refreshLayout.setRefreshHeader(new ClassicsHeader(getActivity()));
        refreshLayout.setRefreshFooter(new ClassicsFooter(getActivity()));
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                TextView tv_1 = view.findViewById(R.id.tv_1);
                tv_1.setText(""+(++i));
                refreshlayout.finishRefresh(1000/*,false*/);//传入false表示刷新失败
//                refreshLayout.finishRefresh(false);
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                refreshlayout.finishLoadMore(2000/*,false*/);//传入false表示加载失败
            }
        });

        listView = view.findViewById(R.id.book_list);
        datas = getDatas();
        showDatas(datas);
        //注册快捷菜单
        registerForContextMenu(listView);

    }
    private void showDatas(List<Map<String, Object>> data) {

        if (data.size() > 0) {
            SimpleAdapter adapter = new SimpleAdapter(
                    getActivity(),
                    data,
                    R.layout.book_list,
                    new String[]{"book_image", "book_name", "book_description", "book_id", "book_price","book_category_name","book_author"},
                    new int[]{R.id.book_image, R.id.book_name, R.id.book_description, R.id.book_id, R.id.book_price,R.id.book_category_name,R.id.book_author});
            listView.setAdapter(adapter);
//            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//                }
//            });

        } else {
            listView.setAdapter(null);
            Toast.makeText(getActivity(),
                    "未查询到相应的数据",
                    Toast.LENGTH_SHORT).show();
        }
    }
    private List<Map<String, Object>> getDatas() {
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("book_image",R.drawable.book_logo_test);
        item.put("book_name","《第一行代码》");
        item.put("book_description","《第一行代码》是由作者付林游推出的一本免费的编程书，旨在帮助读者快速入门编程，并且让读者可以在学习过程中获得更多的知识。");
        item.put("book_id","1");
        item.put("book_price","￥0.00");
        item.put("book_category_name","编程");
        item.put("book_author","付林游");
        data.add(item);
        Map<String, Object> item1 = new HashMap<>();
        item1.put("book_image",R.drawable.book_logo_test);
        item1.put("book_name","《第一行代码》");
        item1.put("book_description","《第一行代码》是由作者付林游推出的一本免费的编程书，旨在帮助读者快速入门编程，并且让读者可以在学习过程中获得更多的知识。");
        item1.put("book_id","1");
        item1.put("book_price","￥0.00");
        item1.put("book_category_name","编程");
        item1.put("book_author","付林游");
        data.add(item1);
        Map<String, Object> item2 = new HashMap<>();
        item2.put("book_image",R.drawable.book_logo_test);
        item2.put("book_name","《第一行代码》");
        item2.put("book_description","《第一行代码》是由作者付林游推出的一本免费的编程书，旨在帮助读者快速入门编程，并且让读者可以在学习过程中获得更多的知识。");
        item2.put("book_id","1");
        item2.put("book_price","￥0.00");
        item2.put("book_category_name","编程");
        item2.put("book_author","付林游");
        data.add(item2);

        return data;
    }
    /*
     * 工作线程
     */
    private class WorkerThread extends Thread {
        @Override
        public void run() {
            String data = doWork();

            Message msg = handler.obtainMessage();
            if (!TextUtils.isEmpty(data)) {
                msg.what = MISSION_SUCCESS;
                msg.obj = data;
            } else {
                msg.what = MISSION_FAILED;
            }

            handler.sendMessage(msg);
        }

        private String doWork() {
            // 模拟网络访问
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "从网络中获取的新数据：\n1 4 6 4 1\n1 3 3 1\n1 2 1\n1 1\n1";
        }
    }
    @SuppressLint("HandlerLeak")
    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            String s = (String) msg.obj;
            switch (msg.what) {
                case MISSION_SUCCESS:
                    s = (String) msg.obj;
                    break;
                case MISSION_FAILED:
                    s = "任务失败!";
                    break;
            }
//            tvShowInfo.setText(s);
        }
    }

}
