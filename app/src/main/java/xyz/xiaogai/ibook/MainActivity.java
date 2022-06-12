package xyz.xiaogai.ibook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTabHost;
import androidx.viewpager.widget.ViewPager;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener,
        TabHost.OnTabChangeListener {
    private FragmentTabHost mTabHost;
    private LayoutInflater layoutInflater;
    private Class<?>[] fragmentArray = {Fragment1.class, Fragment2.class,
            Fragment3.class};

    private int[] mImageViewArray = {
            R.drawable.selector_tab1_book,
            R.drawable.selector_tab2_cart,
            R.drawable.selector_tab3_home};
    // 选项卡上显示的文字
    private String[] mTextViewArray = {"首页", "购物车", "我的"};

    private ViewPager viewPager;
    private List<Fragment> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        initView();
        initPage();
    }

    private void initView() {
        viewPager = findViewById(R.id.pager);
        /*
         * 监听Tab的变化并通知ViewPager适配器切换页面，即当ViewPager滑动时能够带着Tab标签页一起动
         */
//        viewPager.setOnPageChangeListener(this);
        viewPager.addOnPageChangeListener(this);
        /*
         * 加载布局管理器
         */
        layoutInflater = LayoutInflater.from(this);
        mTabHost = findViewById(android.R.id.tabhost);
        /*
         * 改变了绑定对象，用ViewPager代替了原来的实际FrameLayout
         */
        mTabHost.setup(this, getSupportFragmentManager(), R.id.pager);
        /*
         * 在点击Tab标签页时能够让ViewPager滑动到对应的Fragment
         */
        mTabHost.setOnTabChangedListener(this);

        for (int i = 0; i < fragmentArray.length; i++) {
            // 为每一个Tab页设置图标、文字和内容
            TabHost.TabSpec tabSpec = mTabHost.newTabSpec(mTextViewArray[i])
                    .setIndicator(getTabItemView(i));
            mTabHost.addTab(tabSpec, fragmentArray[i], null);
        }

        //去掉分割线
        mTabHost.getTabWidget().setDividerDrawable(android.R.color.transparent);
    }

    private void initPage() {
        Fragment1 fragment1 = new Fragment1();
        Fragment2 fragment2 = new Fragment2();
        Fragment3 fragment3 = new Fragment3();
        list.add(fragment1);
        list.add(fragment2);
        list.add(fragment3);

        viewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager(), list));
    }

    //给Tab按钮设置图标和文字
    private View getTabItemView(int index) {
        View view = layoutInflater.inflate(R.layout.item_tab, null);

        ImageView imageView = view.findViewById(R.id.imageView1);
        imageView.setImageResource(mImageViewArray[index]);

        TextView textView = view.findViewById(R.id.textView1);
        textView.setText(mTextViewArray[index]);

        return view;
    }
    /*
     * OnPageChangeListener中的方法
     */
    @Override
    public void onPageScrollStateChanged(int arg0) {
        /*
         * arg0 == 0：表示什么都没做，即停止中
         * arg0 == 1：表示正在滑动
         * arg0 == 2：表示滑动完毕
         */
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        /*
         *  在前一个页面滑动到后一个页面时，在前一个页面滑动前调用该方法
         */
    }

    @Override
    public void onPageSelected(int arg0) {
        /*
         * arg0表示当前选中的页面的位置
         */
        mTabHost.setCurrentTab(arg0);
    }
    /*
     * OnTabChangeListener中的方法
     * 当Tab页改变时调用
     */
    @Override
    public void onTabChanged(String tabId) {
        int position = mTabHost.getCurrentTab();
        /*
         * 把选中的Tab页交给ViewPager，让它来控制页面切换
         */
        viewPager.setCurrentItem(position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_option, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_about:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
