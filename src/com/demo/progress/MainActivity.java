package com.demo.progress;

import java.io.File;

import com.example.imagewithprogress.R;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {

	// 要上传的文件路径，放在SD卡根目录下
	private String uploadFile = Environment.getExternalStorageDirectory()
			.getPath() + "/1.jpg";
	private TextView file;
	private ImageView image;
	private Button upload;
	private Button download;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		file = (TextView) findViewById(R.id.file);
		file.setText(uploadFile);
		image = (ImageView) findViewById(R.id.image);
		upload = (Button) findViewById(R.id.upload);
		upload.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				File file = new File(uploadFile);
				new FileUploadAsyncTask(MainActivity.this).execute(file);
			}
		});
		download = (Button) findViewById(R.id.download);
		download.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				new FileDownLoadAsyncTask(MainActivity.this).execute(image);
			}
		});
	}

}
