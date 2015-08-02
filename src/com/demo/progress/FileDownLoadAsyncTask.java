package com.demo.progress;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.ByteArrayBuffer;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

public class FileDownLoadAsyncTask extends
		AsyncTask<ImageView, Integer, Bitmap> {

	// 图片下载地址
	private String url = "http://img0.bdstatic.com/img/image/shouye/leimu/mingxing.jpg";
	private Context context;
	private ProgressDialog pd;
	private ImageView image;
	private int width = 150;
	private int height = 150;

	public FileDownLoadAsyncTask(Context context) {
		this.context = context;
	}

	@Override
	protected void onPreExecute() {
		pd = new ProgressDialog(context);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setMessage("下载中....");
		pd.setCancelable(false);
		pd.show();
	}

	/**
	 * 下载图片，并按指定高度和宽度压缩
	 */
	@Override
	protected Bitmap doInBackground(ImageView... params) {
		this.image = params[0];
		Bitmap bitmap = null;
		HttpClient httpClient = new DefaultHttpClient();
		try {
			httpClient.getParams().setParameter(
					CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
			HttpGet httpGet = new HttpGet(url);
			HttpResponse httpResponse = httpClient.execute(httpGet);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity entity = httpResponse.getEntity();
				final long size = entity.getContentLength();
				CountingInputStream cis = new CountingInputStream(
						entity.getContent(), new ProgressListener() {

							@Override
							public void transferred(long transferedBytes) {
								Log.i("FileDownLoadAsyncTask", "总字节数：" + size
										+ " 已下载字节数：" + transferedBytes);
								publishProgress((int) (100 * transferedBytes / size));
							}
						});
				// 需将Inputstream转化为byte数组，以备decodeByteArray用
				// 如直接使用decodeStream会将stream破坏，然后第二次decodeStream时，会出现SkImageDecoder::Factory
				// returned null错误
				// 我试过将获得的Inputstream转化为BufferedInputStream，然后使用mark、reset方法，但是我试了试没成功，不知道为啥，还请成功的各位告知
				byte[] byteIn = toByteArray(cis, (int) size);
				BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
				// 第一次decode时，需设置inJustDecodeBounds属性为true,这样系统就会只读取下载图片的属性而不分配空间，并将属性存储在Options中
				bmpFactoryOptions.inJustDecodeBounds = true;
				// 第一次decode，获取图片高宽度等属性
				BitmapFactory.decodeByteArray(byteIn, 0, byteIn.length,
						bmpFactoryOptions);
				// 根据显示控件大小获取压缩比率，有效避免OOM
				int heightRatio = (int) Math.ceil(bmpFactoryOptions.outHeight
						/ height);
				int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth
						/ width);
				if (heightRatio > 1 && widthRatio > 1) {
					bmpFactoryOptions.inSampleSize = heightRatio > widthRatio ? heightRatio
							: widthRatio;
				}
				// 第二次decode时，需设置inJustDecodeBounds属性为fasle,系统才会根据传入的BitmapFactory.Options真正的压缩图片并返回
				bmpFactoryOptions.inJustDecodeBounds = false;
				bitmap = BitmapFactory.decodeByteArray(byteIn, 0,
						byteIn.length, bmpFactoryOptions);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (httpClient != null && httpClient.getConnectionManager() != null) {
				httpClient.getConnectionManager().shutdown();
			}
		}
		return bitmap;
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		pd.setProgress((int) (progress[0]));
	}

	@Override
	protected void onPostExecute(Bitmap bm) {
		pd.dismiss();
		if (bm != null) {
			image.setImageBitmap(bm);
		} else {
			Toast.makeText(context, "图片下载失败", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * InputStream转化为Byte数组
	 * 
	 * @param instream
	 * @param contentLength
	 * @return
	 * @throws IOException
	 */
	public byte[] toByteArray(InputStream instream, int contentLength)
			throws IOException {
		if (instream == null) {
			return null;
		}
		try {
			if (contentLength < 0) {
				contentLength = 4096;
			}
			final ByteArrayBuffer buffer = new ByteArrayBuffer(contentLength);
			final byte[] tmp = new byte[4096];
			int l;
			while ((l = instream.read(tmp)) != -1) {
				buffer.append(tmp, 0, l);
			}
			return buffer.toByteArray();
		} finally {
			instream.close();
		}
	}

}
