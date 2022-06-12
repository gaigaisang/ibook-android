package xyz.xiaogai.ibook.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.io.IOException;


import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import xyz.xiaogai.ibook.Fragment1;
import xyz.xiaogai.ibook.R;
import xyz.xiaogai.ibook.bean.Book;

public class BookInfoActivity extends AppCompatActivity {
    private static final int MISSION_SUCCESS = 0;
    private static final int MISSION_FAILED = -1;

    private String bookId;
    private Handler handler;
    private Handler isincarthandler;

    private ImageView bookinfo_book_image;
    private TextView bookinfo_book_price;
    private TextView bookinfo_book_description;
    private TextView bookinfo_book_name;
    private TextView bookinfo_book_author;
    private TextView bookinfo_book_category_name;
    private TextView bookinfo_book_num;
    private Button add_to_cart;
    private Button pay_now;
    private Boolean isInCart = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getIntent().getExtras();
        bookId = bundle.getString("bookId");
        setContentView(R.layout.book_info_activity);
        initBookInfo();
    }

    private void initBookInfo() {
        bookinfo_book_image = findViewById(R.id.bookinfo_book_image);
        bookinfo_book_price = findViewById(R.id.bookinfo_book_price);
        bookinfo_book_description = findViewById(R.id.bookinfo_book_description);
        bookinfo_book_name = findViewById(R.id.bookinfo_book_name);
        bookinfo_book_author = findViewById(R.id.bookinfo_book_author);
        bookinfo_book_category_name = findViewById(R.id.bookinfo_book_category_name);
        bookinfo_book_num = findViewById(R.id.bookinfo_book_num);
        add_to_cart = findViewById(R.id.add_to_cart);
        pay_now = findViewById(R.id.pay_now);
        handler = new MyHandler();
        isincarthandler = new isincart();

        Thread t = new WorkerThread();
        t.start();
        add_to_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message msg = isincarthandler.obtainMessage();
                        try {
                            if (isInCart()){
                                msg.what=MISSION_SUCCESS;
                            }else {
                                msg.what=MISSION_FAILED;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        isincarthandler.sendMessage(msg);
                    }
                }).start();

            }

        });
    }
    private Boolean isInCart() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .get()
                .url("http://192.168.0.14:8080/ibook/AddToCartServlet-android?bookId=" + bookId + "&userId=1f125f95-e52f-4ff8-bf3d-432f5844ed14")
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        String responseData = response.body().string();
        Gson gson = new Gson();
        return gson.fromJson(responseData, Boolean.class);
    }
    @SuppressLint("HandlerLeak")
    private class isincart extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MISSION_SUCCESS:
                    Toast.makeText(BookInfoActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                    break;
                case MISSION_FAILED:
                    Toast.makeText(BookInfoActivity.this, "已经在购物车了", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }


    /*
     * 工作线程
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
                    msg.obj = mapper.readValue(data, Book.class);
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
                    .url("http://192.168.0.14:8080/ibook/GetBookByIdServlet?bookId=" + bookId)
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
            Book book = (Book) msg.obj;
            switch (msg.what) {
                case MISSION_SUCCESS:
                    Glide.with(getApplicationContext())
                            .load("http://192.168.0.14:8080/ibook/" + book.getImage())
                            .into(bookinfo_book_image);
                    bookinfo_book_price.setText("￥" + book.getPrice() + "");
                    bookinfo_book_description.setText(book.getDescription());
                    bookinfo_book_name.setText(book.getName());
                    bookinfo_book_author.setText(book.getAuthor());
                    bookinfo_book_category_name.setText(book.getCategory_name());
                    break;
                case MISSION_FAILED:
                    Toast.makeText(BookInfoActivity.this, "获取失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
