package xyz.xiaogai.ibook.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import xyz.xiaogai.ibook.Fragment1;
import xyz.xiaogai.ibook.R;
import xyz.xiaogai.ibook.bean.Book;
import xyz.xiaogai.ibook.util.GeneralBasicOCR;
import xyz.xiaogai.ibook.util.ImageUtil;

public class OcrBookActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static final int TAKE_PHOTO = 1;
    private static final int CHOOSE_PHOTO = 2;
    private static final int CROP_PHOTO = 3;
    private static final int MISSION_SUCCESS = 0;
    private static final int MISSION_FAILED = -1;
    private ListView listView;
    private String bash64image;
    private Handler handler;
    GeneralBasicOCR generalBasicOCR;
    private final String[] permissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ocr_book_activity);
        initOcrBook();
    }

    public void initOcrBook() {
        handler = new MyHandler();
        listView = findViewById(R.id.ocr_search_list);

        getPermissions();
        showImagePickDialog();
    }

    public void showImagePickDialog() {


        String title = "图片识别";
        String[] items = new String[]{"拍照上传", "相册选择"};

        AlertDialog.Builder builder = new AlertDialog.Builder(OcrBookActivity.this);

        builder.setIcon(R.drawable.book_logo)
                .setTitle(title)
                .setItems(items, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        switch (which) {
                            case 0:
                                //选择拍照
                                takePhoto();
                                break;
                            case 1:
                                //选择相册
                                choosePhoto();
                                break;
                            default:
                                break;
                        }
                    }
                });
        builder.create().show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    crop(imageUri);
                }
                break;
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        Uri uri = data.getData();
                        crop(uri);
                    }
                }
                break;
            case CROP_PHOTO:
                if (resultCode == RESULT_OK) {

                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        String s = ImageUtil.inputStream2Base64(inputStream);
                        Log.i("TAG", "onActivityResult: " + s);
                        bash64image = s;
                        Thread t = new WorkerThread();
                        t.start();

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

//                        Log.i("inputStream",inputStream.toString());
//                        user_logo.setImageBitmap(bitmap);

                }
                break;
        }
    }

    // 裁剪图片
    private void crop(Uri uri) {
        // 创建一个裁剪图片的路径
        String fileName = "crop.jpg";
        File cropPhoto = new File(this.getExternalMediaDirs()[0].getAbsolutePath() + File.separator + fileName);
//        Log.i("crop", cropPhoto.toString());
        try {
            if (cropPhoto.exists()) {
                cropPhoto.delete();
            }
            cropPhoto.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageUri = Uri.fromFile(cropPhoto);
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        if (Build.VERSION.SDK_INT >= 24) {
            // 对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.putExtra("crop", true);
        intent.putExtra("scale", true);
        intent.putExtra("aspectX", 2);
        intent.putExtra("aspectY", 3);
//        intent.putExtra("outputX", 300);
//        intent.putExtra("outputY", 300);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intent.putExtra("circleCrop", true);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, CROP_PHOTO);
    }

    private void takePhoto() {
        // 创建一个保存图片的路径
        String fileName = System.currentTimeMillis() + ".jpg";
        File outputImage = new File(this.getExternalMediaDirs()[0].getAbsolutePath() + File.separator + fileName);
//        Log.i("takePhoto", outputImage.toString());
        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 打开系统相机
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        // android7.0及以上的系统使用相机时，文件的URI若使用"file://URI"的形式会导致FileUriExposedException
        if (Build.VERSION.SDK_INT >= 24) {
            imageUri = FileProvider.getUriForFile(OcrBookActivity.this,
                    "xyz.xiaogai.ibook.fileprovider", outputImage);
        } else {
            imageUri = Uri.fromFile(outputImage);
        }


        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    private void choosePhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    private void getPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!EasyPermissions.hasPermissions(this, permissions)) {
                EasyPermissions.requestPermissions(this, "需要获取您的相机、相册使用权限", 1, permissions);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog
                    .Builder(this)
                    .setTitle("提示")
                    .setRationale("需要获取您的相机、相册使用权限，不开启将无法正常工作！")
                    .build()
                    .show();
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
                        item.put("book_price", "￥" + book.getPrice());
                        item.put("book_category_name", book.getCategory_name());
                        item.put("book_author", book.getAuthor());
                        newdata.add(item);

                    }
                    showDatas(newdata);
                    break;
                case MISSION_FAILED:
                    List<Map<String, Object>> newdata1 = new ArrayList<>();
                    Map<String, Object> item = new HashMap<>();
                    item.put("book_image", R.drawable.ic_launcher_background);
                    item.put("book_name", "无");
                    item.put("book_description", "无");
                    item.put("book_id", "0");
                    item.put("book_price", "￥0.00");
                    item.put("book_category_name", "无");
                    item.put("book_author", "无");
                    newdata1.add(item);
                    showDatas(newdata1);
                    break;
            }
        }
    }

    private void showDatas(List<Map<String, Object>> data) {

        if (data.size() > 0) {
            SimpleAdapter adapter = new SimpleAdapter(
                    this,
                    data,
                    R.layout.book_list,
                    new String[]{"book_image", "book_name", "book_description", "book_id", "book_price", "book_category_name", "book_author"},
                    new int[]{R.id.book_image, R.id.book_name, R.id.book_description, R.id.book_id, R.id.book_price, R.id.book_category_name, R.id.book_author});
            adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Object data, String textRepresentation) {
                    if (view instanceof ImageView && data instanceof String) {
                        ImageView iv = (ImageView) view;
                        Glide.with(getApplicationContext())
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
//                    book_id = view.findViewById(R.id.book_id);
////                    Toast.makeText(getContext(),book_id.getText().toString(),Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(getActivity(), BookInfoActivity.class);
//                    Bundle bundle = new Bundle();
//                    bundle.putString("bookId", book_id.getText().toString());
//                    intent.putExtras(bundle);
//                    startActivity(intent);
                }
            });

        } else {
            listView.setAdapter(null);
            Toast.makeText(this,
                    "未查询到相应的数据",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private class WorkerThread extends Thread {
        String dataa;
        @Override
        public void run() {

            String data = null;
//            Thread t= new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        dataa = doWork(getResult());
//                    } catch (IOException | JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//            t.start();
//            try {
//                t.join();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

            try {
                data= doWork(getResult());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            if (data.equals("false")) {
                    data = null;
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

        private String getResult() throws JSONException {
            String s = generalBasicOCR.getOcrResult(bash64image);
            if (s == null) {
                return null;
            }
            JSONObject jsonObject = new JSONObject(s);
            JSONArray jsonArray = jsonObject.getJSONArray("TextDetections");
            String result = "";
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                String words = jsonObject1.getString("DetectedText");
                result += words;
            }
            Log.i("result:", result);
            return result;
        }

    }

    private String doWork(String rs) throws IOException {
        if (rs == null) {
            return null;
        }
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .get()
                .url("http://192.168.0.14:8080/ibook/ParseBookServlet?text=" + rs)
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        return response.body().string();
    }

}
