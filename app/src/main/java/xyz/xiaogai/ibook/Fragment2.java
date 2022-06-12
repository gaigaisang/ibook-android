package xyz.xiaogai.ibook;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import xyz.xiaogai.ibook.bean.Book;
import xyz.xiaogai.ibook.bean.Cart;
import xyz.xiaogai.ibook.bean.CartItem;

public class Fragment2 extends Fragment {
    private ListView listView;
    private List<Map<String, Object>> datas;
    private static final int MISSION_SUCCESS = 0;
    private static final int MISSION_FAILED = -1;
    private Handler handler;
    private TextView cart_title;
    private TextView cartitem_price;
    private CheckBox cartitem_check_all;
    Boolean isCheckAll = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_2, container, false);
        init(view);

        return view;
    }

    private void init(View view) {
        cart_title = view.findViewById(R.id.cart_title);
        cartitem_price = view.findViewById(R.id.cartitem_price);
        cartitem_check_all = view.findViewById(R.id.cartitem_check_all);
//		ListView listView = view.findViewById(R.id.lv_show2);
//		registerForContextMenu(listView);
        handler = new Fragment2.MyHandler();
        RefreshLayout refreshLayout = (RefreshLayout) view.findViewById(R.id.cartitem_refreshLayout);
        refreshLayout.setRefreshHeader(new ClassicsHeader(getActivity()));
        refreshLayout.setRefreshFooter(new ClassicsFooter(getActivity()));
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                Thread t = new Fragment2.WorkerThread();
                t.start();
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
        cartitem_check_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cartitem_check_all.isChecked()){
                    isCheckAll = true;
                }else{
                    isCheckAll = false;
                }
                Thread t = new Fragment2.WorkerThread();
                t.start();
                if(isCheckAll){
                    Double price = 0.0;
                    for (int i = 0; i < datas.size(); i++) {
                        price+= (double)datas.get(i).get("cartitem_book_price")*(int)datas.get(i).get("cartitem_num");
                    }
                    cartitem_price.setText(price+"");
                }else {
                    cartitem_price.setText("0.00");
                }


            }
        });
        listView = view.findViewById(R.id.cartitem_list);
        Thread t = new Fragment2.WorkerThread();
        t.start();


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
                if (data.equals("null")) {
                    data = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Message msg = handler.obtainMessage();
            if (!TextUtils.isEmpty(data)) {
                msg.what = MISSION_SUCCESS;
                Gson gson = new Gson();
                List<CartItem> cartitems = new ArrayList<>();
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    JSONArray jsonArray = jsonObject.getJSONArray("cartitems");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        CartItem cartItem = gson.fromJson(jsonArray.get(i).toString(), CartItem.class);
                        cartitems.add(cartItem);
                    }
                    msg.obj = cartitems;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                Log.i("Fragment2", "cartitems:" + cartitems.size());
            } else {
                msg.what = MISSION_FAILED;
            }

            handler.sendMessage(msg);
        }

        private String doWork() throws IOException {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .get()
                    .url("http://192.168.0.14:8080/ibook/GetCartServlet?userid=1f125f95-e52f-4ff8-bf3d-432f5844ed14")
//                    .url("https://raw.github.com/square/okhttp/master/README.md")

                    .build();
            Call call = client.newCall(request);
            Response response = call.execute();
//            Log.i("Fragment2", "response.body()" + response.body().string());
            return response.body().string();
        }
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
           List<CartItem> cartItems=(List<CartItem>) msg.obj;
            switch (msg.what) {
                case MISSION_SUCCESS:
                    List<Map<String, Object>> newdata = new ArrayList<>();
                    for (CartItem cartItem : cartItems) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("check_cartitem", isCheckAll);
                        item.put("cartitem_book_image", cartItem.getBook().getImage());
                        item.put("cartitem_book_name", cartItem.getBook().getName());
                        item.put("cartitem_book_description", cartItem.getBook().getDescription());
                        item.put("cartitem_book_price", cartItem.getBook().getPrice());
                        item.put("cartitem_num", cartItem.getNum());
                        newdata.add(item);
                    }
                    datas=newdata;
                    listView.setAdapter(new MyAdapter(getActivity(), datas));
                    cart_title.setText("购物车(" + cartItems.size() + ")");

                    break;
                case MISSION_FAILED:
                    Toast.makeText(getActivity(), "获取数据失败", Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    }

    private List<Map<String, Object>> getDatas() {
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> item1 = new HashMap<>();
        item1.put("check_cartitem", true);
        item1.put("cartitem_book_image", "static/img/bookcover/106.jpg");
        item1.put("cartitem_book_name", "《第一行代码》");
        item1.put("cartitem_book_description", "《第一行代码》是由作者付林游推出的一本免费的编程书，旨在帮助读者快速入门编程，并且让读者可以在学习过程中获得更多的知识。");
        item1.put("cartitem_book_price", 30.00);
        item1.put("cartitem_num", 23);
        data.add(item1);
        Map<String, Object> item2 = new HashMap<>();
        item2.put("check_cartitem", true);
        item2.put("cartitem_book_image", "static/img/bookcover/102.jpg");
        item2.put("cartitem_book_name", "123");
        item2.put("cartitem_book_description", "《第一行代码》是由作dsadadwad并且让读者可以在学习过程中获得更多的知识。");
        item2.put("cartitem_book_price", 19.00);
        item2.put("cartitem_num", 66);
        data.add(item2);

        return data;
    }

    public final class ViewHolder {
        public CheckBox check_cartitem;
        public ImageView cartitem_book_image;
        public TextView cartitem_book_name;
        public TextView cartitem_book_description;
        public TextView cartitem_book_price;
        public TextView cartitem_num;
        public Button num_dash;
        public Button num_plus;
    }

    class MyAdapter extends BaseAdapter {
        LayoutInflater inflater;
        List<Map<String, Object>> data;

        MyAdapter(Context context, List<Map<String, Object>> data) {
            this.inflater = LayoutInflater.from(context);
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.cartitem_list, null);
                /*
                 * 实例化activity_list_item_image3布局文件中的控件
                 */
                holder.check_cartitem = convertView.findViewById(R.id.check_cartitem);
                holder.cartitem_book_image = convertView.findViewById(R.id.cartitem_book_image);
                holder.cartitem_book_name = convertView.findViewById(R.id.cartitem_book_name);
                holder.cartitem_book_description = convertView.findViewById(R.id.cartitem_book_description);
                holder.cartitem_book_price = convertView.findViewById(R.id.cartitem_book_price);
                holder.cartitem_num = convertView.findViewById(R.id.cartitem_num);
                holder.num_dash = convertView.findViewById(R.id.num_dash);
                holder.num_plus = convertView.findViewById(R.id.num_plus);

                convertView.setTag(holder);
            } else
                holder = (ViewHolder) convertView.getTag();
            /*
             * 设置布局文件中控件要显示的值及事件处理函数
             */
            holder.check_cartitem.setChecked((boolean) data.get(position).get("check_cartitem"));
            if ((boolean) data.get(position).get("check_cartitem")){

            }
            holder.check_cartitem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    double price = Double.parseDouble(cartitem_price.getText().toString()) ;
                    CheckBox check_cartitem = (CheckBox) view;
                    if (check_cartitem.isChecked()) {
                        Toast.makeText(getActivity(),
                                "选中",
                                Toast.LENGTH_SHORT).show();
                        cartitem_price.setText((price+(double)data.get(position).get("cartitem_book_price")*(int)data.get(position).get("cartitem_num"))+"");

                    } else {
                        Toast.makeText(getActivity(),
                                "取消选中",
                                Toast.LENGTH_SHORT).show();
                        cartitem_price.setText((price-(double)data.get(position).get("cartitem_book_price")*(int)data.get(position).get("cartitem_num"))+"");
                        cartitem_check_all.setChecked(false);

                    }
                }
            });

            Glide.with(getActivity())
                    .load("http://192.168.0.14:8080/ibook/" + data.get(position).get("cartitem_book_image"))
                    .into(holder.cartitem_book_image);
            holder.cartitem_book_name.setText(data.get(position).get("cartitem_book_name").toString());
            holder.cartitem_book_description.setText(data.get(position).get("cartitem_book_description").toString());
            holder.cartitem_book_price.setText(data.get(position).get("cartitem_book_price").toString());
            holder.cartitem_num.setText(data.get(position).get("cartitem_num").toString());
            holder.num_dash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Button num_dash = (Button) view;
//					int num=(int)data.get(position).get("cartitem_num");
//					holder.cartitem_num.setText(num-1+"");
                    Toast.makeText(getActivity(),
                            "减少",
                            Toast.LENGTH_SHORT).show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            OkHttpClient client = new OkHttpClient();
                            Request request = new Request.Builder()
                                    .get()
                                    .url("http://192.168.0.14:8080/ibook/UpdateCartItemNumServlet")
                                    .build();
                            Call call = client.newCall(request);
                            try {
                                Response response = call.execute();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
//                            response.body().string();
                        }
                    }).start();
                }
            });

            holder.num_plus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Button num_plus = (Button) view;
                    Toast.makeText(getActivity(),
                            "增加",
                            Toast.LENGTH_SHORT).show();
                }
            });

            return convertView;
        }
    }
}
