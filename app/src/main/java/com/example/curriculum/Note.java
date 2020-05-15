package com.example.curriculum;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.curriculum.utils.GetPhotoFromPhotoAlbum;
import com.example.curriculum.utils.MyDBHelper;
import com.example.curriculum.utils.SingleImageLayout;
import com.example.curriculum.utils.SingleNoteLayout;
import com.example.curriculum.utils.SingleRecordLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

// /data/user/0/com.example.curriculum

/**
 * 便签部分
 */

public class Note extends AppCompatActivity {

    private String TAG = "Note";
    private int course_id;
    private int record_id = 0;
    private int image_id = 1000;
    private final int NOTE_TYPE = 1;
    private final int RECORD_TYPE = 2;
    private final int IMAGE_TYPE = 3;
    private LinearLayout linearLayout;
    private ImageView audio;
    private ImageView picture;
    private MyDBHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_with_fragment);

        // title设置
        Intent intent = getIntent();
        String course_name = intent.getStringExtra("course_name");
        course_id = intent.getIntExtra("course_id", 0);
        Log.d(TAG, "onCreate: course_id: " + course_id);
        if (course_name.equals("")) {
            setTitle("便签");
        } else {
            setTitle(course_name);
        }
        linearLayout = (LinearLayout) findViewById(R.id.note_linearlayout);
        // TODO: completed note store failed.
        // 打开数据库，读入内容
        dbHelper = new MyDBHelper(this, "Course.db", null, 3);
        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from Notes where course_id = ? order by id",
                new String[]{""+course_id});
        int index = -1;
        if(cursor.moveToFirst()) {
            do{
                String content = cursor.getString(cursor.getColumnIndex("content"));
                int type = cursor.getInt(cursor.getColumnIndex("type"));
                switch (type) {
                    case NOTE_TYPE:
                        int completed = cursor.getInt(cursor.getColumnIndex("completed"));
                        create_note(index++, content, false, completed==1);
                        break;
                    case RECORD_TYPE:
                        create_record(index++, content);
                        break;
                    case IMAGE_TYPE:
                        // create_image(index++, Uri.parse(content));
                        create_image(index++, content);
                        break;
                    default:
                        break;
                }
            } while(cursor.moveToNext());
        }
        cursor.close();


//        // 建立一个标签，
//        // TODO: 数据库后修改
//        create_note(-1);
//        EditText editText = (EditText) linearLayout.getChildAt(0).findViewById(R.id.note_edittext);
//        editText.clearFocus();

        // 待办键设置
        write_button_set();

        // 录音键设置
        audio_button_set();

        // 图片键设置
        image_button_set();

    }

    // 待办键设置，末尾添加新待办
    private void write_button_set() {
        ImageView write_button = (ImageView) findViewById(R.id.write);
        write_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                create_note(linearLayout.getChildCount()-1, null, false, false);
            }
        });
    }

    // 创建便签，填充note页面
    private void create_note(int above_index, String content, boolean requestFocus, boolean completed) {
        Log.d(TAG, "create_note: func get completed: " + completed);
        final SingleNoteLayout singleNoteLayout = new SingleNoteLayout(Note.this, null);
        final ImageView imageView = (ImageView) singleNoteLayout.findViewById(R.id.note_complete);
        final EditText editText = (EditText) singleNoteLayout.findViewById(R.id.note_edittext);
        editText.setText(content);
        if (completed) {
            singleNoteLayout.isCompleted = completed;
            imageView.setImageResource(R.mipmap.completed);
            editText.setPaintFlags(editText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        // 左侧图片监听：完成，删除线
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (singleNoteLayout.isCompleted) {
                    imageView.setImageResource(R.mipmap.non_completed);
                    editText.setPaintFlags(editText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    singleNoteLayout.isCompleted = false;
                } else {
                    imageView.setImageResource(R.mipmap.completed);
                    editText.setPaintFlags(editText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    singleNoteLayout.isCompleted = true;
                }
            }
        });
        // 右侧edittext监听：回车，新建note项；退格，删除本项
        if (requestFocus) {
            editText.requestFocus();    // 更新光标
        }
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    // 回车键：在当前note下新建一个note
                    SingleNoteLayout child = (SingleNoteLayout) getCurrentFocus().getParent().getParent();
                    create_note(linearLayout.indexOfChild(child), null, true, false);
                    return true;
                } else if(keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    // 删除键 且 光标在最左侧时，执行删除本项
                    if (((EditText) getCurrentFocus()).getSelectionStart() == 0) {
                        SingleNoteLayout now_note = (SingleNoteLayout) getCurrentFocus().getParent().getParent();
                        int index = linearLayout.indexOfChild(now_note);
                        Log.d(TAG, "onKey: index: " + index);
                        if (index != 0) {
                            linearLayout.removeViewAt(index);
                            Log.d(TAG, "onKey: delete note item");
                            // 调整光标
                            int temp = index - 1;
                            while (temp >= 0) {
                                if (linearLayout.getChildAt(temp) instanceof SingleNoteLayout) {
                                    linearLayout.getChildAt(temp).requestFocus();
                                    break;
                                }
                                temp--;
                            }
                            return true;
                        }
                    }
                }
                return false;
            }
        });
        // 动态添加控件
        linearLayout.addView(singleNoteLayout, above_index + 1);
    }

    // 录音键设置，点击开始录音，再次点击停止录音，并生成文件，添加图标
    private void audio_button_set() {
        audio = (ImageView) findViewById(R.id.audio);
        audio.setOnClickListener(new View.OnClickListener() {
            private MediaRecorder mediaRecorder = null;
            private File recordAudioFile = null;
            private String time = null;
            @Override
            public void onClick(View v) {
                // 权限
                if (!judge_audio_permission()) {
                    request_audio_permission();
                    if (!judge_audio_permission()) {
                        return;
                    }
                }

                // 录音部分，以tag的01标记是否正在录音
                if (audio.getTag().toString().equals("0")) {
                    // 开始录音
                    audio.setImageResource(R.mipmap.stop);
                    audio.setTag("1");
                    time = (String) DateFormat.format("yyyyMMdd_HHmmss", Calendar.getInstance(Locale.CHINA));
                    // file_name = "record_test.amr";

                    String dir_path = Note.this.getFilesDir().getPath() + "/recordings/";
                    Log.d(TAG, "onClick: dir: " + dir_path);
                    File dir = new File(dir_path);
                    if(!dir.exists()){
                        dir.mkdirs();
                    }

                    String file_name = dir_path + "/" + time + ".aac";
                    recordAudioFile = new File(file_name);
                    try {
                        if (!recordAudioFile.createNewFile()) {
                            Log.d(TAG, "onClick: file create failed.");
                        } else {
                            Log.d(TAG, "onClick: file create done.");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // recordAudioFile = File.createTempFile(file_name,".aac");
                    // recordAudioFile = new File(file_name, ".aac");
                    // 录音设置
                    mediaRecorder = new MediaRecorder();
                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC_ELD);
                    mediaRecorder.setOutputFile(recordAudioFile.getAbsolutePath());
                    // /data/user/0/com.example.curriculum/cache/
                    try {
                        mediaRecorder.prepare();
                    } catch (IOException e) {
                        Log.d(TAG, "onClick: prepare failed.");
                    }
                    mediaRecorder.start();

                } else {
                    // 停止录音
                    audio.setImageResource(R.mipmap.audio);
                    audio.setTag("0");

                    if (mediaRecorder != null){
                        Log.d(TAG, "onClick: record stop: " + recordAudioFile.getPath());
                        mediaRecorder.stop();
                        mediaRecorder.release();
                        mediaRecorder = null;
                        if (getCurrentFocus() != null) {
                            // 当前项下插入一个singlerecord
                            SingleNoteLayout child = (SingleNoteLayout) getCurrentFocus().getParent().getParent();
                            create_record(linearLayout.indexOfChild(child), recordAudioFile.getAbsolutePath());
                        } else {
                            // 若无焦点，添加至末尾
                            create_record(linearLayout.getChildCount()-1, recordAudioFile.getAbsolutePath());
                        }

                    }
                }
            }
        });
    }

    // 创建录音项
    private void create_record(int above_index, final String file_path) {
        Log.d(TAG, "create_record: " + file_path);
        SingleRecordLayout singleRecordLayout = new SingleRecordLayout(Note.this, null, file_path);
        singleRecordLayout.setId(record_id++);

        final ImageView record = (ImageView) singleRecordLayout.findViewById(R.id.record);
        final ImageView delete = (ImageView) singleRecordLayout.findViewById(R.id.delete_record);
        // 左侧图片监听：播放录音
        record.setOnClickListener(new View.OnClickListener() {
            boolean paused = false;
            boolean playing = false;
            int position;
            MediaPlayer mediaPlayer;
            @Override
            public void onClick(View v) {
                // 通过getParent()拿到真正的SingleRecordLayout
                SingleRecordLayout now_record_layout = (SingleRecordLayout) v.getParent().getParent();
                String path = now_record_layout.path;
                Log.d(TAG, "onClick: click audio. MP: " + mediaPlayer);
                try {
                    if (mediaPlayer == null) {
                        // 开始播放
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setDataSource(path);
                        mediaPlayer.prepare();
                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                mp.stop();
                                mp.release();
                                mp = null;
                                mediaPlayer = null;
                                playing = false;
                                record.setImageResource(R.mipmap.recording_nonplay);
                                Log.d(TAG, "onCompletion: play complete.");
                            }
                        });
                        mediaPlayer.start();
                        playing = true;
                        record.setImageResource(R.mipmap.recording);
                        Log.d(TAG, "onClick: play start: " + path);
                    } else if (playing && mediaPlayer.isPlaying()) {
                        // 正在播放，暂停
                        mediaPlayer.pause();
                        position = mediaPlayer.getCurrentPosition();
                        paused = true;
                        Log.d(TAG, "onClick: play paused.");
                    } else if (paused) {
                        // 暂停，重新播放
                        mediaPlayer.start();
                        mediaPlayer.seekTo(position);
                        paused = false;
                        Log.d(TAG, "onClick: play restart.");
                    }
                } catch (Exception e){
                    e.printStackTrace();
                    Log.d(TAG, "onClick: play crashed. path: " + path);
                }
            }
        });
        // 右侧监听：删除录音 及 本项
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SingleRecordLayout now_record_layout = (SingleRecordLayout) v.getParent().getParent();
                String path = now_record_layout.path;
                Log.d(TAG, "onClick: click id: " + v.getId());
                File file = new File(path);
//                    int index = linearLayout.indexOfChild(singleRecordLayout);
//                    linearLayout.removeViewAt(index);
                linearLayout.removeView(now_record_layout);

                if (file.delete()) {
                    Log.d(TAG, "onClick: delete file successed: " + path);
                } else {
                    Log.d(TAG, "onClick: delete file failed: " + path);
                }
            }
        });

        // 动态添加控件
        linearLayout.addView(singleRecordLayout, above_index + 1);
    }

    // 判断录音权限
    private boolean judge_audio_permission() {
        return(ContextCompat.checkSelfPermission(com.example.curriculum.Note.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(com.example.curriculum.Note.this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(com.example.curriculum.Note.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED);
    }

    // 申请录音权限
    private void request_audio_permission() {
        ActivityCompat.requestPermissions(Note.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO},
                1);
    }

    // 图片键设置
    private void image_button_set() {
        picture = (ImageView) findViewById(R.id.picture);
        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 权限
                if (!judge_image_permission()) {
                    request_image_permission();
                    if (!judge_image_permission()) {
                        return;
                    }
                }

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);

//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                    // Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//                    // intent.addCategory(Intent.CATEGORY_OPENABLE);
//                    Intent intent = new Intent(Intent.ACTION_PICK);
//                    intent.setType("image/*");
//                    startActivityForResult(intent, 1);
//                }
                // /storage/emulated/0/DCIM/Camera/IMG_20200507_123034.jpg
                // content://com.miui.gallery.open/raw/%2Fstorage%2Femulated%2F0%2FDCIM%2FCamera%2FIMG_20200507_123034.jpg
                // create_image(-1, Uri.parse("content://com.miui.gallery.open/raw/%2Fstorage%2Femulated%2F0%2FDCIM%2FCamera%2FIMG_20200507_123034.jpg"));
            }
        });
    }

    // 获取图片与处理
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        String path;
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // data.getData() returns Uri
            path = GetPhotoFromPhotoAlbum.getRealPathFromUri(this, data.getData());
            Log.d(TAG, "onActivityResult: get image path: " + path);
            Log.d(TAG, "onActivityResult: get_uri: " + data.getData().toString());
            Log.d(TAG, "onActivityResult: image exist: " + new File(path).exists());
            if (getCurrentFocus() != null) {
                SingleNoteLayout child = (SingleNoteLayout) getCurrentFocus().getParent().getParent();
                create_image(linearLayout.indexOfChild(child), path);
            } else {
                create_image(linearLayout.getChildCount()-1, path);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // 创建图片项
    // private void create_image(int above_index, final Uri uri) {
    private void create_image(int above_index, final String path) {
        final Uri uri;
        if (path == null) {
            return;
        }
        Log.d(TAG, "create_image: start create image item after " + above_index);
        final SingleImageLayout singleImageLayout = new SingleImageLayout(Note.this, null, path);
        singleImageLayout.setId(image_id++);
        uri = Uri.fromFile(new File(path));
        // 点击小图，选择操作
        final ImageView image = (ImageView) singleImageLayout.findViewById(R.id.image);
        Log.d(TAG, "create_image: path: " + path);
        image.setImageURI(uri);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SingleImageLayout now_image_layout = (SingleImageLayout) v.getParent().getParent();
                ImageView small_image = now_image_layout.findViewById(R.id.image);
                if (small_image.getTag().equals("unclicked")){
                    // 首次点击，显现操作
                    ConstraintLayout operations = (ConstraintLayout) now_image_layout.findViewById(R.id.image_operations);
                    operations.setVisibility(View.VISIBLE);
                    small_image.setTag("clicked");
                    // Log.d(TAG, "onClick: click small image, unclicked->clicked");
                } else {
                    // 再次点击，隐藏操作
                    small_image.setTag("unclicked");
                    ConstraintLayout operations = (ConstraintLayout) now_image_layout.findViewById(R.id.image_operations);
                    operations.setVisibility(View.GONE);
                    // Log.d(TAG, "onClick: click small image, clicked->unclicked");
                }
            }
        });

        // 点击删除，删除本项
        ImageView delete = (ImageView) singleImageLayout.findViewById(R.id.delete_image);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: click delete image.");
                SingleImageLayout now_image_layout = (SingleImageLayout) v.getParent().getParent().getParent();
                linearLayout.removeView(now_image_layout);
            }
        });

        // 点击放缩，切换大图
        ImageView scale = (ImageView) singleImageLayout.findViewById(R.id.scale_image);
        scale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SingleImageLayout now_image_layout = (SingleImageLayout) v.getParent().getParent().getParent();
                ImageView small_image = now_image_layout.findViewById(R.id.image);
                ImageView large_image = now_image_layout.findViewById(R.id.large_image);
                ConstraintLayout operations = (ConstraintLayout) now_image_layout.findViewById(R.id.image_operations);
                operations.setVisibility(View.GONE);
                small_image.setVisibility(View.GONE);
                large_image.setVisibility(View.VISIBLE);
                // Log.d(TAG, "onClick: click scale, now: " + small_image.getTag());
            }
        });

//        // 点击图库，开启图库
//        ImageView album = (ImageView) singleImageLayout.findViewById(R.id.album_image);
//        album.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Uri contentUri = FileProvider.getUriForFile(Note.this, "com.example.curriculum", new File(path));
//                Log.d(TAG, "onClick: contenturi: " + contentUri);
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.setDataAndType(contentUri, "image/*");
//                startActivity(intent);
//                SingleImageLayout now_image_layout = (SingleImageLayout) v.getParent().getParent().getParent();
//                ConstraintLayout operations = (ConstraintLayout) now_image_layout.findViewById(R.id.image_operations);
//                operations.setVisibility(View.GONE);
//                ImageView small_image = now_image_layout.findViewById(R.id.image);
//                small_image.setTag("clicked");
//                // Log.d(TAG, "onClick: click album, now: " + small_image.getTag());
//            }
//        });

        // 点击大图，切换小图
        ImageView large_image = (ImageView) singleImageLayout.findViewById(R.id.large_image);
        large_image.setImageURI(uri);
        large_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SingleImageLayout now_image_layout = (SingleImageLayout) v.getParent().getParent();
                ImageView small_image = now_image_layout.findViewById(R.id.image);
                ImageView large_image = now_image_layout.findViewById(R.id.large_image);
                small_image.setTag("unclicked");
                small_image.setVisibility(View.VISIBLE);
                large_image.setVisibility(View.GONE);
                // Log.d(TAG, "onClick: click large image, clicked->unclicked");
            }
        });

        linearLayout.addView(singleImageLayout, above_index+1);
        Log.d(TAG, "create_image: create image item at " + (above_index + 1));
    }

    // 判断读图权限
    private boolean judge_image_permission() {
        return(ContextCompat.checkSelfPermission(com.example.curriculum.Note.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(com.example.curriculum.Note.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED);
    }

    // 申请读图权限
    private void request_image_permission() {
        ActivityCompat.requestPermissions(Note.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                2);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        // 清空本课程便签记录
        db.delete("Notes", "course_id = ?", new String[]{""+course_id});
        // 添加新便签记录
        int count = linearLayout.getChildCount();
        for(int index = 0; index < count; index++) {
            // Log.d(TAG, "onDestroy: " + index + " class: " + linearLayout.getChildAt(index).getClass());
            if (linearLayout.getChildAt(index) instanceof SingleNoteLayout) {
                int completed = ((SingleNoteLayout)linearLayout.getChildAt(index)).isCompleted? 1: 0;
                ContentValues contentValues = new ContentValues();
                contentValues.put("course_id", course_id);
                contentValues.put("type", NOTE_TYPE);
                contentValues.put("completed", completed);
                contentValues.put("content",
                        ((EditText)(linearLayout.getChildAt(index).findViewById(R.id.note_edittext)))
                                .getText().toString());
                db.insert("Notes", null, contentValues);
                // Log.d(TAG, "onDestroy: " + index + ": " + "");
            } else if (linearLayout.getChildAt(index) instanceof SingleRecordLayout) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("course_id", course_id);
                contentValues.put("type", RECORD_TYPE);
                contentValues.put("content",
                        ((SingleRecordLayout) linearLayout.getChildAt(index)).path);
                db.insert("Notes", null, contentValues);
            } else {
                ContentValues contentValues = new ContentValues();
                contentValues.put("course_id", course_id);
                contentValues.put("type", IMAGE_TYPE);
                contentValues.put("content",
                        ((SingleImageLayout) linearLayout.getChildAt(index)).path);
                db.insert("Notes", null, contentValues);
            }
        }
        Log.d(TAG, "onDestroy: SQL stored " + count + " items.");
    }
}
