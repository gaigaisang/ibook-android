package xyz.xiaogai.ibook;

import android.Manifest;
import android.annotation.SuppressLint;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import xyz.xiaogai.ibook.bean.Book;
import xyz.xiaogai.ibook.view.BookInfoActivity;
import xyz.xiaogai.ibook.view.OcrBookActivity;

public class Fragment1 extends Fragment {
    private static final int MISSION_SUCCESS = 0;
    private static final int MISSION_FAILED = -1;
    private TextView book_search;
    private Handler handler;
    private ListView listView;
    private TextView book_id;
    private ImageView book_ocr;
    private List<Map<String, Object>> datas;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_1, container, false);

        init(view);

        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init(View view) {
        book_search = view.findViewById(R.id.book_search);
        book_ocr = view.findViewById(R.id.book_ocr);
        book_ocr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), OcrBookActivity.class);
                startActivity(intent);
            }
        });

        handler = new MyHandler();
        RefreshLayout refreshLayout = (RefreshLayout) view.findViewById(R.id.refreshLayout);
        refreshLayout.setRefreshHeader(new ClassicsHeader(getActivity()));
        refreshLayout.setRefreshFooter(new ClassicsFooter(getActivity()));
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                Thread t = new WorkerThread();
                t.start();
                refreshlayout.finishRefresh(1000/*,false*/);//??????false??????????????????
//                refreshLayout.finishRefresh(false);
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {

                refreshlayout.finishLoadMore(2000/*,false*/);//??????false??????????????????
            }
        });

        listView = view.findViewById(R.id.book_list);
        Thread t = new WorkerThread();
        t.start();

    }

    private void showDatas(List<Map<String, Object>> data) {

        if (data.size() > 0) {
            SimpleAdapter adapter = new SimpleAdapter(
                    getActivity(),
                    data,
                    R.layout.book_list,
                    new String[]{"book_image", "book_name", "book_description", "book_id", "book_price", "book_category_name", "book_author"},
                    new int[]{R.id.book_image, R.id.book_name, R.id.book_description, R.id.book_id, R.id.book_price, R.id.book_category_name, R.id.book_author});
            adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Object data, String textRepresentation) {
                    if (view instanceof ImageView && data instanceof String) {
                        ImageView iv = (ImageView) view;
                        Glide.with(getActivity())
                                .load("http://192.168.0.14:8080/ibook/" + data)
                                .into(iv);
                        return true;
                    }
                    return false;
                }
            });
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    book_id = view.findViewById(R.id.book_id);
//                    Toast.makeText(getContext(),book_id.getText().toString(),Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), BookInfoActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("bookId", book_id.getText().toString());
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });

        } else {
            listView.setAdapter(null);
            Toast.makeText(getActivity(),
                    "???????????????????????????",
                    Toast.LENGTH_SHORT).show();
        }
    }


    /*
     * ????????????
     */
    private class WorkerThread extends Thread {
        @Override
        public void run() {
            String data = null;
            try {
                data = doWork();
                if (data.equals("false")) {
                    data = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Message msg = handler.obtainMessage();
            if (!TextUtils.isEmpty(data)) {
                ObjectMapper mapper = new ObjectMapper();


                msg.what = MISSION_SUCCESS;
                try {
                    msg.obj = mapper.readValue(data, new TypeReference<List<Book>>() {
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                msg.what = MISSION_FAILED;
            }

            handler.sendMessage(msg);
        }

        private String doWork() throws IOException {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .get()
                    .url("http://192.168.0.14:8080/ibook/GetAllBookServlet")
                    .build();
            Call call = client.newCall(request);
            Response response = call.execute();
            return response.body().string();
        }
    }

    @SuppressLint("HandlerLeak")
    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            List<Book> books = (List<Book>) msg.obj;
            switch (msg.what) {
                case MISSION_SUCCESS:
                    List<Map<String, Object>> newdata = new ArrayList<>();
                    for (Book book : books) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("book_image", book.getImage());
                        item.put("book_name", book.getName());
                        item.put("book_description", book.getDescription());
                        item.put("book_id", book.getId());
                        item.put("book_price", "???" + book.getPrice());
                        item.put("book_category_name", book.getCategory_name());
                        item.put("book_author", book.getAuthor());
                        newdata.add(item);

                    }
                    showDatas(newdata);
                    break;
                case MISSION_FAILED:
                    List<Map<String, Object>> newdata1 = new ArrayList<>();
                    Map<String, Object> item = new HashMap<>();
                    item.put("book_image", R.drawable.book_logo_test);
                    item.put("book_name", "?????????????????????");
                    item.put("book_description", "????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????");
                    item.put("book_id", "1");
                    item.put("book_price", "???0.00");
                    item.put("book_category_name", "??????");
                    item.put("book_author", "?????????");
                    newdata1.add(item);
                    showDatas(newdata1);
                    break;
            }
        }
    }


}
