package xyz.xiaogai.ibook.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;

import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.TextView;
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
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.ocr.v20181119.OcrClient;
import com.tencentcloudapi.ocr.v20181119.models.GeneralBasicOCRRequest;
import com.tencentcloudapi.ocr.v20181119.models.GeneralBasicOCRResponse;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
import okhttp3.MediaType;
import okhttp3.OkHttp;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import xyz.xiaogai.ibook.R;

import xyz.xiaogai.ibook.bean.Book;
import xyz.xiaogai.ibook.util.ImageUtil;

public class OcrBookActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static final int TAKE_PHOTO = 1;
    private static final int CHOOSE_PHOTO = 2;
    private static final int CROP_PHOTO = 3;
    private static final int MISSION_SUCCESS = 0;
    private static final int MISSION_FAILED = -1;
    private ListView listView;
    private TextView book_id;
    private ImageView ocr_top_image;
    private Handler Ocrhandler;
    private String keyword;
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
        Ocrhandler = new OcrHandler();
        listView = findViewById(R.id.ocr_search_list);
        ocr_top_image = findViewById(R.id.ocr_top_image);
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
//                        bash64image = s;
                        getResultAsyncTask resultAsyncTask = new getResultAsyncTask();
                        Log.i("TAG", "onActivityResult: data:image/jpeg;base64," + s);
                        resultAsyncTask.execute("data:image/jpeg;base64,"+s);
//                        resultAsyncTask.execute("data:image/jpeg;base64,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");
//                        Thread t = new WorkerThread();
//                        t.start();

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

    private class getResultAsyncTask extends AsyncTask<String, Integer, String> {
        //执行前的操作
        @Override
        protected void onPreExecute() {
            Toast.makeText(getApplicationContext(), "正在查询请稍等。。", Toast.LENGTH_SHORT).show();
        }

        //执行异步任务
        @Override
        protected String doInBackground(String... params) {


            String s = params[0];
            s= getOcrResult(s);
            if (s==null){
                return null;
            }
//            Log.i("result-s", s);
            try {

                JSONObject  jsonObject = new JSONObject(s);
                JSONArray jsonArray = jsonObject.getJSONArray("TextDetections");
//                String[] result = new String[jsonArray.length()];
                String result = "";
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    String str = jsonObject1.getString("DetectedText");
//                    result[i] = str;
                    result+= str;
                }

//                Gson gson = new Gson();
//                String json = gson.toJson(result);
                keyword = result;
//                Log.i("result-json", json);
                return result;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }

        }

        //结果 返回主线程
        @Override
        protected void onPostExecute(String result) {
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            ocr_top_image.setImageBitmap(bitmap);

            if (result != null) {
                Log.i("result", result);
                Thread t = new OcrThread();
                t.start();
            } else {
                Toast.makeText(getApplicationContext(), "未找到相似图书 换个角度再试试", Toast.LENGTH_SHORT).show();
            }


        }

        public String getOcrResult(String base64Image) {
            try {
                // 实例化一个认证对象，入参需要传入腾讯云账户secretId，secretKey,此处还需注意密钥对的保密
                // 密钥可前往https://console.cloud.tencent.com/cam/capi网站进行获取
                Credential cred = new Credential("AKIDjHacNr40g5C2buwhBOKzNVMwFH2WjfLk", "91cwzlHWUBbleGbgUNzLRGo55oGQJvGK");
                // 实例化一个http选项，可选的，没有特殊需求可以跳过
                HttpProfile httpProfile = new HttpProfile();
                httpProfile.setEndpoint("ocr.tencentcloudapi.com");
                // 实例化一个client选项，可选的，没有特殊需求可以跳过
                ClientProfile clientProfile = new ClientProfile();
                clientProfile.setHttpProfile(httpProfile);
                // 实例化要请求产品的client对象,clientProfile是可选的
                OcrClient client = new OcrClient(cred, "ap-beijing", clientProfile);
                // 实例化一个请求对象,每个接口都会对应一个request对象
                GeneralBasicOCRRequest req = new GeneralBasicOCRRequest();
                req.setImageBase64(base64Image);
                // 返回的resp是一个GeneralBasicOCRResponse的实例，与请求对象对应
                GeneralBasicOCRResponse resp = client.GeneralBasicOCR(req);
                // 输出json格式的字符串回包
                return (GeneralBasicOCRResponse.toJsonString(resp));
            } catch (TencentCloudSDKException e) {
                return null;
            }

        }

    }

    @SuppressLint("HandlerLeak")
    private class OcrHandler extends Handler {
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
                    book_id = findViewById(R.id.book_id);
//                    Toast.makeText(getContext(),book_id.getText().toString(),Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), BookInfoActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("bookId", book_id.getText().toString());
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });

        } else {
            listView.setAdapter(null);
            Toast.makeText(this,
                    "未查询到相应的数据",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private class OcrThread extends Thread {
        @Override
        public void run() {

            String data = null;
            try {
                data = doWork();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (data.equals("false")) {
                data = null;
            }


            Message msg = Ocrhandler.obtainMessage();
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

            Ocrhandler.sendMessage(msg);
        }


    }

    private String doWork() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "{userid:123}"))
                .url("http://192.168.0.14:8080/ibook/ParseBookServlet?keyword=" + keyword)
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        return response.body().string();
    }

}
